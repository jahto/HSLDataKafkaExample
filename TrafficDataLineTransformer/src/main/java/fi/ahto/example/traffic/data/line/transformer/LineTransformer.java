/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.ahto.example.traffic.data.line.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.ahto.example.traffic.data.contracts.internal.ServiceTrips;
import fi.ahto.example.traffic.data.contracts.internal.ServiceData;
import fi.ahto.example.traffic.data.contracts.internal.ServiceStop;
import fi.ahto.example.traffic.data.contracts.internal.TripStop;
import fi.ahto.example.traffic.data.contracts.internal.TripStopSet;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import fi.ahto.example.traffic.data.contracts.internal.VehicleAtStop;
import fi.ahto.example.traffic.data.contracts.internal.VehicleDataList;
import fi.ahto.kafka.streams.state.utils.TransformerSupplierWithStore;
import fi.ahto.kafka.streams.state.utils.TransformerWithStore;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

/**
 *
 * @author Jouni Ahto
 */
@Component
public class LineTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(LineTransformer.class);

    @Autowired
    private ObjectMapper objectMapper;

    // Must be static when declared here as inner class, otherwise you run into
    // problems with Jackson objectmapper and databinder.
    public static class ServiceList extends ArrayList<ServiceData> {
    }

    void VehicleActivityTransformers() {
        LOG.debug("VehicleActivityTransformers created");
    }

    @Bean
    public KStream<String, VehicleActivity> kStream(StreamsBuilder builder) {
        LOG.debug("Constructing stream from data-by-lineid");
        final JsonSerde<VehicleActivity> vafserde = new JsonSerde<>(VehicleActivity.class, objectMapper);
        final JsonSerde<VehicleDataList> vaflistserde = new JsonSerde<>(VehicleDataList.class, objectMapper);
        final JsonSerde<TripStopSet> tripsserde = new JsonSerde<>(TripStopSet.class, objectMapper);
        final JsonSerde<ServiceTrips> serviceserde = new JsonSerde<>(ServiceTrips.class, objectMapper);
        final JsonSerde<ServiceList> sdbserde = new JsonSerde<>(ServiceList.class, objectMapper);
        final JsonSerde<VehicleAtStop> vasserde = new JsonSerde<>(VehicleAtStop.class, objectMapper);

        KStream<String, VehicleActivity> streamin = builder.stream("data-by-lineid", Consumed.with(Serdes.String(), vafserde));

        GlobalKTable<String, ServiceList> routeservices
                = builder.globalTable("routes-to-services",
                        Consumed.with(Serdes.String(), sdbserde),
                        Materialized.<String, ServiceList, KeyValueStore<Bytes, byte[]>>as("routes-to-services"));

        GlobalKTable<String, ServiceTrips> servicetrips
                = builder.globalTable("services-to-trips",
                        Consumed.with(Serdes.String(), serviceserde),
                        Materialized.<String, ServiceTrips, KeyValueStore<Bytes, byte[]>>as("services-to-trips"));

        GlobalKTable<String, TripStopSet> trips
                = builder.globalTable("trips",
                        Consumed.with(Serdes.String(), tripsserde),
                        Materialized.<String, TripStopSet, KeyValueStore<Bytes, byte[]>>as("trips"));

        Initializer<VehicleDataList> lineinitializer = () -> {
            VehicleDataList valist = new VehicleDataList();
            return valist;
        };

        // Get a table of all vehicles currently operating on the line.
        Aggregator<String, VehicleActivity, VehicleDataList> lineaggregator
                = (String key, VehicleActivity value, VehicleDataList aggregate) -> {
                    return aggregateLine(aggregate, value, key);
                };

        KTable<String, VehicleDataList> lines = streamin
                .groupByKey(Serialized.with(Serdes.String(), vafserde))
                .aggregate(lineinitializer, lineaggregator,
                        Materialized.<String, VehicleDataList, KeyValueStore<Bytes, byte[]>>as("line-aggregation-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(vaflistserde)
                );

        // Map to correct trip id in several steps, another approach
        KStream<String, VehicleActivity> possibilitiesstream = streamin
                // At least some Z3 trains don't have this in incoming data.
                .filter((k, v) -> v.getNextStopId() != null && !v.getNextStopId().isEmpty())
                .filter((k, v) -> v.getBlockId() == null || v.getBlockId().isEmpty())
                .leftJoin(routeservices,
                        (String key, VehicleActivity value) -> {
                            String newkey = value.getInternalLineId();
                            return newkey;
                        },
                        (VehicleActivity value, ServiceList right) -> {
                            if (right == null) {
                                String newkey = value.getInternalLineId();
                                LOG.info("Didn't find correct servicelist for route {}, line {}", newkey, value.getLineId());
                            } else {
                                String newkey = value.getInternalLineId();
                                LOG.info("Found correct servicelist for route {}, line {}", newkey, value.getLineId());
                            }
                            VehicleActivity rval = findPossibleServices(value, right);
                            return rval;
                        });

        KStream<String, VehicleActivity> tripstreamalt = possibilitiesstream
                .flatMapValues((k, v) -> {
                    List<VehicleActivity> rval = new ArrayList<>();
                    for (String s : v.getPossibilities()) {
                        VehicleActivity va = new VehicleActivity(v);
                        va.setServiceID(s);
                        rval.add(va);
                    }
                    return rval;
                });

        KStream<String, VehicleActivity> hasnotblockid = tripstreamalt.leftJoin(servicetrips,
                (String key, VehicleActivity value) -> {
                    String newkey = value.getServiceID() + ":" + value.getInternalLineId();
                    return newkey;
                },
                (VehicleActivity value, ServiceTrips right) -> {
                    String newkey = value.getServiceID() + ":" + value.getInternalLineId();
                    if (right == null) {
                        LOG.info("Didn't find correct service {} for route {}, line {}", newkey, value.getInternalLineId(), value.getLineId());
                    } else {
                        LOG.info("Found correct service {} for route {}, line {}", newkey, value.getInternalLineId(), value.getLineId());
                    }
                    value = findTrip(value, right);
                    if (value.getTripID() == null) {
                        LOG.info("Didn't find correct trip for service {}, route {}, line {}, time {}",
                                value.getServiceID(), value.getInternalLineId(), value.getLineId(), value.getStartTime());
                        return null;
                    }

                    LOG.info("Found correct trip for service {}, route {}, line {}, time {}",
                            value.getServiceID(), value.getInternalLineId(), value.getLineId(), value.getStartTime());
                    return value;
                }).filter((k, v) -> v != null);

        KStream<String, VehicleActivity> hasblockidstream = streamin
                .filter((k, v) -> v.getBlockId() != null && !v.getBlockId().isEmpty())
                .leftJoin(servicetrips,
                        (String key, VehicleActivity value) -> {
                            return value.getBlockId();
                        },
                        (VehicleActivity value, ServiceTrips right) -> {
                            if (right == null) {
                                LOG.info("Didn't find correct block {} for route {}, line {}", value.getBlockId(), value.getInternalLineId(), value.getLineId());
                            } else {
                                LOG.info("Found correct block {} for route {}, line {}", value.getBlockId(), value.getInternalLineId(), value.getLineId());
                            }
                            value = findTrip(value, right);
                            return value;
                        });

        // Add possibly missing remaining stops 
        KStream<String, VehicleActivity> finaltripstopstream = hasnotblockid.merge(hasblockidstream);
        KStream<String, VehicleActivity> reallyfinaltripstopstream = finaltripstopstream
                .leftJoin(trips,
                        (String key, VehicleActivity value) -> {
                            return value.getTripID();
                        },
                        (VehicleActivity left, TripStopSet right) -> {
                            return addMissingStopTimes(left, right);
                        }
                );

        // Compare current and previous estimated stop times, react if they differ
        TimeTableComparerSupplier transformer = new TimeTableComparerSupplier(builder, Serdes.String(), vafserde, "stop-times");
        KStream<String, VehicleAtStop> stopchanges = reallyfinaltripstopstream
                .map((String key, VehicleActivity va) -> KeyValue.pair(va.getVehicleId(), va))
                .transform(transformer, "stop-times");

        lines.toStream().to("data-by-lineid-enhanced", Produced.with(Serdes.String(), vaflistserde));
        stopchanges.to("changes-by-stopid", Produced.with(Serdes.String(), vasserde));
        return streamin;
    }

    private VehicleActivity findPossibleServices(VehicleActivity va, ServiceList sd) {
        List<String> possibilities = new ArrayList<>();
        if (sd == null) {
            return null;
        }

        LocalDate date = va.getOperatingDate();
        DayOfWeek dow = date.getDayOfWeek();
        int cnt = 0;

        for (ServiceData sdb : sd) {
            LOG.info("Checking service {} for route {}, line {}", sdb.serviceId, va.getInternalLineId(), va.getLineId());

            byte result = 0x0;
            switch (dow) {
                case MONDAY:
                    result = (byte) (sdb.weekdays & 0x1);
                    break;
                case TUESDAY:
                    result = (byte) (sdb.weekdays & 0x2);
                    break;
                case WEDNESDAY:
                    result = (byte) (sdb.weekdays & 0x4);
                    break;
                case THURSDAY:
                    result = (byte) (sdb.weekdays & 0x8);
                    break;
                case FRIDAY:
                    result = (byte) (sdb.weekdays & 0x10);
                    break;
                case SATURDAY:
                    result = (byte) (sdb.weekdays & 0x20);
                    break;
                case SUNDAY:
                    result = (byte) (sdb.weekdays & 0x40);
                    break;
            }

            if (result == 0x0 && sdb.weekdays != 0x0) {
                continue;
            }

            if (sdb.validfrom.isAfter(date)) {
                continue;
            }
            if (sdb.validuntil.isBefore(date)) {
                continue;
            }
            if (sdb.notinuse.contains(date)) {
                continue;
            }

            if (sdb.routeIds.contains(va.getInternalLineId()) == false) {
                continue;
            }

            possibilities.add(sdb.serviceId);
            LOG.info("Service {} matches for route {}, line {}", sdb.serviceId, va.getInternalLineId(), va.getLineId());
            cnt++;
        }
        va.setPossibilities(possibilities);
        return va;
    }

    private VehicleActivity findTrip(VehicleActivity va, ServiceTrips sd) {
        String tripId = null;

        if (sd == null) {
            return va;
        }

        if (va.getDirection().equals("1")) {
            tripId = sd.timesforward.get(va.getStartTime());
            if (tripId == null) {
                LOG.info("Time {} not found in maps, route {}, line {}, service {}",
                        va.getStartTime(), va.getInternalLineId(), va.getLineId(), va.getServiceID());
                return va;
            }
        }

        if (va.getDirection().equals("2")) {
            tripId = sd.timesbackward.get(va.getStartTime());
            if (tripId == null) {
                LOG.info("Time {} not found in maps, route {}, line {}, service {}",
                        va.getStartTime(), va.getInternalLineId(), va.getLineId(), va.getServiceID());
                return va;
            }
        }

        LOG.info("Time {} was found in maps, route {}, line {}, service {}",
                va.getStartTime(), va.getInternalLineId(), va.getLineId(), va.getServiceID());

        va.setTripID(tripId);
        return va;
    }

    private VehicleDataList aggregateLine(VehicleDataList aggregate, VehicleActivity value, String key) {
        LOG.debug("Aggregating line {}", key);
        // New implementation. Out-of-date vehicles are now handled already
        // in TrafficDataVehicleTransformer.
        List<VehicleActivity> list = aggregate.getVehicleActivities();
        list.remove(value);
        if (value.LineHasChanged() == false) {
            list.add(value);
        } else {
            LOG.info("Removed vehicle {} from line {}", value.getVehicleId(), key);
        }
        return aggregate;
    }

    VehicleActivity addMissingStopTimes(VehicleActivity left, TripStopSet right) {
        if (right == null) {
            return left;
        }

        NavigableSet<TripStop> missing = null;

        if (left.getOnwardCalls().isEmpty()) {
            TripStop stop = findStopByName(left.getNextStopId(), right);
            if (stop != null) {
                missing = right.tailSet(stop, true);
            }
        } else {
            TripStop stop = findStopByName(left.getOnwardCalls().last().stopid, right);
            if (stop != null) {
                missing = right.tailSet(stop, false);
            }
        }

        if (missing != null && missing.size() > 0) {
            for (TripStop miss : missing) {
                LocalTime newtime = miss.arrivalTime.minusSeconds(left.getDelay());
                ServiceStop toadd = new ServiceStop();
                toadd.seq = miss.seq;
                toadd.stopid = miss.stopid;
                toadd.arrivalTime = newtime;
                left.getOnwardCalls().add(toadd);
            }

            LOG.debug("Added missing stops.");
        }

        return left;
    }

    TripStop findStopByName(String name, TripStopSet set) {
        for (TripStop stop : set) {
            if (stop.stopid.equals(name)) {
                return stop;
            }
        }
        return null;
    }

    static class TimeTableComparerSupplier extends TransformerSupplierWithStore<String, VehicleActivity, KeyValue<String, VehicleAtStop>> {

        public TimeTableComparerSupplier(StreamsBuilder builder, Serde<String> keyserde, Serde<VehicleActivity> valserde, String storeName) {
            super(builder, keyserde, valserde, storeName);
        }

        @Override
        public Transformer<String, VehicleActivity, KeyValue<String, VehicleAtStop>> get() {
            return new TransformerImpl() {
                @Override
                public KeyValue<String, VehicleAtStop> transform(String key, VehicleActivity previous, VehicleActivity current) {
                    return compareTimeTables(previous, current);
                }
            };
        }

        protected class TransformerImpl
                extends TransformerSupplierWithStore<String, VehicleActivity, KeyValue<String, VehicleAtStop>>.TransformerImpl
                implements TransformerWithStore<String, VehicleActivity, KeyValue<String, VehicleAtStop>> {

            @Override
            public KeyValue<String, VehicleAtStop> transform(String key, VehicleActivity previous, VehicleActivity current) {
                return compareTimeTables(previous, current);
            }

            KeyValue<String, VehicleAtStop> compareTimeTables(VehicleActivity previous, VehicleActivity current) {
                boolean fixed = false;
                LOG.debug("Comparing timetables.");
                if (current.getOnwardCalls().isEmpty()) {
                    LOG.info("There should be onwardcalls now");
                }
                if (previous == null) {
                    LOG.debug("Adding stop times first time.");
                    Iterator<ServiceStop> iter = current.getOnwardCalls().descendingIterator();
                    while (iter.hasNext()) {
                        ServiceStop curstop = iter.next();
                        VehicleAtStop vas = new VehicleAtStop();
                        vas.vehicleId = current.getVehicleId();
                        vas.lineId = current.getLineId();
                        vas.arrivalTime = curstop.arrivalTime;
                        context.forward(curstop.stopid, vas);
                    }
                    return null;
                }

                if (current.LineHasChanged()) {
                    LOG.debug("Removing all remaining stops for vehicle " + current.getVehicleId());
                    for (ServiceStop ss : current.getOnwardCalls()) {
                        LOG.debug("Removing vehicle " + current.getVehicleId() + " from stop " + ss.stopid);
                        VehicleAtStop vas = new VehicleAtStop();
                        vas.remove = true;
                        vas.vehicleId = previous.getVehicleId();
                        vas.lineId = previous.getLineId();
                        context.forward(ss.stopid, vas);
                    }
                    return null;
                }

                if (current.getDelay() != previous.getDelay()) {
                    LOG.debug("Delay has changed, stop times should be adjusted!");
                }
                Iterator<ServiceStop> iter = current.getOnwardCalls().descendingIterator();
                ServiceStop curstop = null;
                while (iter.hasNext()) {
                    curstop = iter.next();
                    ServiceStop prevstop = previous.getOnwardCalls().floor(curstop);
                    if (prevstop != null) {
                        // Just skip until the reason has been found...
                        if (curstop.arrivalTime == null) {
                            LOG.info("Current arrival time is null for stop {}", curstop.stopid);
                            continue;
                        }
                        if (prevstop.arrivalTime == null) {
                            LOG.info("Previous arrival time is null for stop {}", curstop.stopid);
                            continue;
                        }
                        if (curstop.arrivalTime.compareTo(prevstop.arrivalTime) != 0) {
                            // Vehicles estimated arriving time to these stops has changed.
                            // Push the information to some queue.
                            VehicleAtStop vas = new VehicleAtStop();
                            vas.vehicleId = current.getVehicleId();
                            vas.lineId = current.getLineId();
                            vas.arrivalTime = curstop.arrivalTime;
                            context.forward(curstop.stopid, vas);
                            fixed = true;
                        }
                    }
                }

                if (fixed) {
                    LOG.debug("Fixed estimated arrival times.");
                }

                if (curstop != null) {
                    NavigableSet<ServiceStop> remove = previous.getOnwardCalls().headSet(curstop, false);
                    if (remove != null && remove.size() > 0) {
                        // Vehicle has gone past these stops, so will not be arriving
                        // to them anymore. Push the information to some queue.
                        LOG.debug("Removing stops.");
                        for (ServiceStop ss : remove) {
                            LOG.debug("Removing vehicle " + current.getVehicleId() + " from stop " + ss.stopid);
                            VehicleAtStop vas = new VehicleAtStop();
                            vas.remove = true;
                            vas.vehicleId = current.getVehicleId();
                            vas.lineId = current.getLineId();
                            context.forward(ss.stopid, vas);
                        }
                    }
                }

                return null;
            }
        }
    }
}
