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
import fi.ahto.kafka.streams.state.utils.SimpleTransformerSupplierWithStore;
import fi.ahto.kafka.streams.state.utils.TransformerSupplierWithStore;
import fi.ahto.kafka.streams.state.utils.TransformerWithStore;
import fi.ahto.kafka.streams.state.utils.ValueTransformerSupplierWithStore;
import fi.ahto.kafka.streams.state.utils.ValueTransformerWithStore;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
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

        // Map to correct trip id in several steps
        // We do currently not use these streams for anything other than their side effects.
        // I.e. mapping to possible services. Check if peek() could work in some cases.
        KStream<String, VehicleActivity> servicestream = streamin.leftJoin(routeservices,
                (String key, VehicleActivity value) -> {
                    String newkey = value.getInternalLineId();
                    return newkey;
                },
                (VehicleActivity value, ServiceList right) -> {
                    if (right == null) {
                        String newkey = value.getInternalLineId();
                        // LOG.info("Didn't find correct servicelist for route " + newkey);
                    }
                    value = findService(value, right);
                    return value;
                });

        KStream<String, VehicleActivity> tripstream = streamin.leftJoin(servicetrips,
                // KStream<String, VehicleActivity> tripstream = servicestream.leftJoin(servicetrips,
                (String key, VehicleActivity value) -> {
                    String newkey = value.getServiceID();
                    return newkey;
                },
                (VehicleActivity value, ServiceTrips right) -> {
                    if (right == null) {
                        String newkey = value.getServiceID();
                        // LOG.info("Didn't find correct service " + newkey);
                    }
                    value = findTrip(value, right);
                    return value;
                });

        // Add possibly missing remaining stops 
        KStream<String, VehicleActivity> tripstopstream = streamin
                // KStream<String, VehicleActivity> tripstopstream = tripstream
                .leftJoin(trips,
                        (String key, VehicleActivity value) -> {
                            return value.getTripID();
                        },
                        (VehicleActivity left, TripStopSet right) -> {
                            return addMissingStopTimes(left, right);
                        }
                );

        // Compare current and previous estimated stop times, react (how?) if they differ
        TimeTableComparerSupplier transformer = new TimeTableComparerSupplier(builder, Serdes.String(), vafserde, "stop-times");
        KStream<String, VehicleAtStop> stopchanges = streamin
                // KStream<String, VehicleAtStop> stopchanges = tripstopstream
                .map((String key, VehicleActivity va) -> KeyValue.pair(va.getVehicleId(), va))
                .transform(transformer, "stop-times");

        lines.toStream().to("data-by-lineid-enhanced", Produced.with(Serdes.String(), vaflistserde));
        stopchanges.to("changes-by-stopid", Produced.with(Serdes.String(), vasserde));
        return streamin;
    }

    private VehicleActivity findService(VehicleActivity va, ServiceList sd) {
        // FOLI GTFS data does not contain any valid dates, but we
        // get the right (?) trip id already from realtime feed.
        // Chek if it matches, an if so, return immediately.
        if (va.getTripID() != null) {
            // Didn't actually check...
            return va;
        }

        va.setServiceID(null);

        if (sd == null) {
            return va;
        }

        LocalDate date = va.getOperatingDate();
        DayOfWeek dow = date.getDayOfWeek();

        for (ServiceData sdb : sd) {
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

            if (result == 0x0) {
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

            va.setServiceID(sdb.serviceId);
        }

        return va;
    }

    private VehicleActivity findTrip(VehicleActivity va, ServiceTrips sd) {
        // FOLI GTFS data does not contain any valid dates, but we
        // get the right (?) trip id already from realtime feed.
        // Chek if it matches, an if so, return immediately.
        if (va.getTripID() != null) {
            // Didn't actually check...
            return va;
        }

        String tripId = null;

        if (sd == null) {
            return va;
        }

        if (va.getDirection().equals("1")) {
            tripId = sd.timesforward.get(va.getStartTime());
            if (tripId == null) {
                return va;
            }
        }

        if (va.getDirection().equals("2")) {
            tripId = sd.timesbackward.get(va.getStartTime());
            if (tripId == null) {
                return va;
            }
        }

        va.setTripID(tripId);
        return va;
    }

    private VehicleDataList aggregateLine(VehicleDataList aggregate, VehicleActivity value, String key) {
        // LOG.debug("Aggregating line " + key);
        boolean remove = false;
        List<VehicleActivity> list = aggregate.getVehicleActivities();

        ListIterator<VehicleActivity> iter = list.listIterator();
        long time1 = value.getRecordTime().getEpochSecond();

        // Remove entries older than 90 seconds or value itself. Not a safe
        // way to detect when a vehicle has changed line or gone out of traffic.
        while (iter.hasNext()) {
            VehicleActivity vaf = iter.next();
            long time2 = vaf.getRecordTime().getEpochSecond();
            if (vaf.getVehicleId().equals(value.getVehicleId())) {
                remove = true;
            }
            if (time1 - time2 > 90) {
                remove = true;
            }
            if (remove) {
                iter.remove();
            }
        }

        if (value.LineHasChanged() == false) {
            list.add(value);
        } else {
            LOG.info("Removed vehicle " + value.getVehicleId() + " from line " + key);
            if (value.getOnwardCalls().size() > 0) {
                LOG.info("Stops should be removed!");
            }
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
                missing = right.tailSet(stop, true);
            }
        }

        if (missing != null && missing.size() > 0) {
            for (TripStop miss : missing) {
                LocalTime newtime = miss.arrivalTime.plusSeconds(left.getDelay());
                ServiceStop toadd = new ServiceStop();
                toadd.seq = miss.seq;
                toadd.stopid = miss.stopid;
                toadd.arrivalTime = newtime;
                left.getOnwardCalls().add(toadd);
            }
            // LOG.info("Added missing stops.");
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
                // LOG.info("Comparing timetables.");
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

                int i = previous.getOnwardCalls().size();
                int j = current.getOnwardCalls().size();

                if (i != j) {
                    LOG.debug("Onwardcalls.size differ.");
                }

                Iterator<ServiceStop> iter = current.getOnwardCalls().descendingIterator();
                ServiceStop curstop = null;
                while (iter.hasNext()) {
                    curstop = iter.next();
                    ServiceStop prevstop = previous.getOnwardCalls().floor(curstop);
                    if (prevstop != null) {
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
                            LOG.info("Removing vehicle "+ current.getVehicleId() + " from stop " + ss.stopid);
                            VehicleAtStop vas = new VehicleAtStop();
                            vas.remove = true;
                            vas.vehicleId = current.getVehicleId();
                            vas.lineId = current.getLineId();
                            context.forward(ss.stopid, vas);
                        }
                    }
                }

                if (current.LineHasChanged()) {
                    LOG.info("Removing all remaining stops for vehicle " + current.getVehicleId());
                    for (ServiceStop ss : previous.getOnwardCalls()) {
                        LOG.info("Removing vehicle "+ current.getVehicleId() + " from stop " + ss.stopid);
                        VehicleAtStop vas = new VehicleAtStop();
                        vas.remove = true;
                        vas.vehicleId = previous.getVehicleId();
                        vas.lineId = previous.getLineId();
                        context.forward(ss.stopid, vas);
                    }
                }
                return null;
            }
        }
    }
}
