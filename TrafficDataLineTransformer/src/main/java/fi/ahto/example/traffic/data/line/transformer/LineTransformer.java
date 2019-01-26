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
import com.github.jahto.utils.CommonFSTConfiguration;
import com.github.jahto.utils.FSTSerde;
import fi.ahto.example.traffic.data.contracts.internal.GTFSLocalTime;
import fi.ahto.example.traffic.data.contracts.internal.StreamServiceData;
import fi.ahto.example.traffic.data.contracts.internal.ServiceList;
import fi.ahto.example.traffic.data.contracts.internal.StartTimesToTrips;
import fi.ahto.example.traffic.data.contracts.internal.TripStop;
import fi.ahto.example.traffic.data.contracts.internal.TripStopSet;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import fi.ahto.example.traffic.data.contracts.internal.VehicleAtStop;
import fi.ahto.example.traffic.data.contracts.internal.VehicleDataList;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Initializer;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.nustaq.serialization.FSTConfiguration;
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

    void VehicleActivityTransformers() {
        LOG.debug("VehicleActivityTransformers created");
    }

    @Bean
    public KStream<String, VehicleActivity> kStream(StreamsBuilder builder) {
        LOG.debug("Constructing stream from data-by-lineid");
        FSTConfiguration conf = CommonFSTConfiguration.getCommonFSTConfiguration();
        final JsonSerde<VehicleActivity> vafserde = new JsonSerde<>(VehicleActivity.class, objectMapper);

        final FSTSerde<VehicleActivity> fstvafserde = new FSTSerde<>(VehicleActivity.class, conf);
        final FSTSerde<VehicleDataList> fstvaflistserde = new FSTSerde<>(VehicleDataList.class, conf);
        final FSTSerde<ServiceList> fstServiceListSerde = new FSTSerde<>(ServiceList.class, conf);
        final FSTSerde<TripStopSet> fsttripsserde = new FSTSerde<>(TripStopSet.class, conf);
        final FSTSerde<StartTimesToTrips> fstserviceserde = new FSTSerde<>(StartTimesToTrips.class, conf);
        final FSTSerde<VehicleAtStop> fstvasserde = new FSTSerde<>(VehicleAtStop.class, conf);

        KStream<String, VehicleActivity> streamin = builder.stream("data-by-lineid", Consumed.with(Serdes.String(), fstvafserde));

        GlobalKTable<String, ServiceList> routesToServices
                = builder.globalTable("routes-to-services",
                        Consumed.with(Serdes.String(), fstServiceListSerde),
                        Materialized.<String, ServiceList, KeyValueStore<Bytes, byte[]>>as("routes-to-services-store")
                );

        GlobalKTable<String, StartTimesToTrips> serviceToTrips
                = builder.globalTable("services-to-trips",
                        Consumed.with(Serdes.String(), fstserviceserde),
                        Materialized.<String, StartTimesToTrips, KeyValueStore<Bytes, byte[]>>as("services-to-trips-store")
                );

        GlobalKTable<String, TripStopSet> trips
                = builder.globalTable("trips",
                        Consumed.with(Serdes.String(), fsttripsserde),
                        Materialized.<String, TripStopSet, KeyValueStore<Bytes, byte[]>>as("fsttrips")
                );

        // Map to correct trip id in several steps, another approach.
        // Some feeds do not have any information about the correct timetable
        // and exact trip, so we'll have to find it based on other information
        // available.
        KStream<String, VehicleActivity> hasNoId = streamin
                .filter((k, v) -> {
                    return (v.getBlockId() == null || v.getBlockId().isEmpty())
                            && (v.getTripID() == null || v.getTripID().isEmpty());
                })
                // At least some Z3 trains don't have this in incoming data.
                .filter((k, v) -> v.getNextStopId() != null && !v.getNextStopId().isEmpty())
                // .filterNot((k, v) -> v.LineHasChanged())
                .leftJoin(routesToServices, (String key, VehicleActivity value) -> value.getInternalLineId(),
                        (VehicleActivity value, ServiceList right) -> {
                            if (value.getInternalLineId().equals("FI:TKL:90")) {
                                if (value.getStartTime().toSecondOfDay() == 21900) {
                                    int i = 0;
                                }
                            }
                            if (right == null) {
                                LOG.warn("Didn't find correct servicelist for route {}, line {}", value.getInternalLineId(), value.getLineId());
                            } else {
                                LOG.debug("Found correct servicelist for route {}, line {}", value.getInternalLineId(), value.getLineId());
                            }
                            VehicleActivity rval = findPossibleServices(value, right);
                            return rval;
                        });

        KStream<String, VehicleActivity> hasNoIdSecondStep = hasNoId
                .flatMapValues((k, v) -> {
                    List<VehicleActivity> rval = new ArrayList<>();
                    if (v == null) {
                        // Happens when I forget to feed static GTFS-data, must check later...
                        return rval;
                    }
                    for (String s : v.getServicePossibilities()) {
                        VehicleActivity va = new VehicleActivity(v);
                        va.setServiceID(s);
                        String key = va.getServiceID();// + ":" + va.getInternalLineId();
                        rval.add(va);
                    }
                    return rval;
                });

        KStream<String, VehicleActivity> hasNoIdThirdStep = hasNoIdSecondStep
                .leftJoin(serviceToTrips,
                        (String key, VehicleActivity value) -> {
                            String newkey = value.getServiceID() + ":" + value.getInternalLineId() + ":" + value.getDirection();
                            return newkey;
                        },
                        (VehicleActivity value, StartTimesToTrips right) -> {
                            String newkey = value.getServiceID() + ":" + value.getInternalLineId() + ":" + value.getDirection();
                            if (right == null) {
                                LOG.warn("Didn't find correct service {} for route {}, line {}, time {}, dir {}",
                                        newkey, value.getInternalLineId(), value.getLineId(), value.getStartTime(), value.getDirection());
                            } else {
                                LOG.info("Found correct service {} for route {}, line {}, time {}, dir {}",
                                        newkey, value.getInternalLineId(), value.getLineId(), value.getStartTime(), value.getDirection());
                            }
                            if (newkey.equals("FI:TKL:TAL_MATO_K35_2019:FI:TKL:90:1")) {
                                int i = 1;
                            }
                            if (newkey.equals("FI:TKL:TAL_MATO_K35_2019:FI:TKL:90:2")) {
                                int i = 1;
                            }
                            value = findTrip(value, right);
                            if (value.getTripID() == null) {
                                LOG.warn("Didn't find correct trip for service {}, route {}, line {}, time {}",
                                        value.getServiceID(), value.getInternalLineId(), value.getLineId(), value.getStartTime());
                                return null;
                            }

                            LOG.info("Found correct trip for service {}, route {}, line {}, time {}",
                                    value.getServiceID(), value.getInternalLineId(), value.getLineId(), value.getStartTime());
                            return value;
                        }
                )
                .filter((k, v) -> v != null);

        // Some feeds do have some information that helps finding the correct trip and timetable.
        KStream<String, VehicleActivity> hasBlockId = streamin
                .filter((k, v) -> {
                    return v.getBlockId() != null && !v.getBlockId().isEmpty()
                            && (v.getTripID() == null || v.getTripID().isEmpty());
                })
                //.filterNot((k, v) -> v.LineHasChanged())
                .leftJoin(serviceToTrips,
                        (String key, VehicleActivity value) -> {
                            String retkey = value.getBlockId() + ":" + value.getInternalLineId(); // + ":" + value.getDirection();
                            return retkey;
                        },
                        (VehicleActivity value, StartTimesToTrips right) -> {
                            String retkey = value.getBlockId() + ":" + value.getInternalLineId(); // + ":" + value.getDirection();
                            if (right == null) {
                                LOG.warn("Didn't find correct block {} for route {}, line {}, dir {}",
                                        retkey, value.getInternalLineId(), value.getLineId(), value.getDirection());
                            } else {
                                LOG.info("Found correct block {} for route {}, line {}, dir {}",
                                        retkey, value.getInternalLineId(), value.getLineId(), value.getDirection());
                            }
                            value = findTrip(value, right);
                            if (value == null) {
                                LOG.warn("Didn't find correct trip, block {} for route {}, line {}, dir {}",
                                        retkey, value.getInternalLineId(), value.getLineId(), value.getDirection());
                            }
                            return value;
                        });

        // And them, some feeds already have a reference to the correct trip and timetable.
        KStream<String, VehicleActivity> hasTripId = streamin
                .filter((k, v) -> v.getTripID() != null && !v.getTripID().isEmpty());
        
        // Merge the streams.
        KStream<String, VehicleActivity> finaltripstopstream = hasNoIdThirdStep.merge(hasBlockId).merge(hasTripId);

        // Add possibly missing remaining stops 
        KStream<String, VehicleActivity> reallyfinaltripstopstream = finaltripstopstream
                .leftJoin(trips,
                        (String key, VehicleActivity value) -> value.getTripID(),
                        (VehicleActivity left, TripStopSet right) -> addMissingStopTimes(left, right)
                );

        // Compare current and previous estimated stop times, react if they differ
        TimeTableComparerSupplier transformer = new TimeTableComparerSupplier(builder, Serdes.String(), fstvafserde, "stop-times");
        KStream<String, VehicleAtStop> stopchanges = reallyfinaltripstopstream
                .map((String key, VehicleActivity va) -> KeyValue.pair(va.getVehicleId(), va))
                .transform(transformer, "stop-times");

        // Get a table of all vehicles currently operating on the line.
        Initializer<VehicleDataList> lineinitializer = () -> {
            VehicleDataList valist = new VehicleDataList();
            return valist;
        };

        Aggregator<String, VehicleActivity, VehicleDataList> lineaggregator
                = (String key, VehicleActivity value, VehicleDataList aggregate) -> {
                    return aggregateLine(aggregate, value, key);
                };

        KTable<String, VehicleDataList> lines = streamin
                .groupByKey(Serialized.with(Serdes.String(), vafserde))
                .aggregate(lineinitializer, lineaggregator,
                        Materialized.<String, VehicleDataList, KeyValueStore<Bytes, byte[]>>as("line-aggregation-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(fstvaflistserde)
                );

        lines.toStream().to("data-by-lineid-enhanced", Produced.with(Serdes.String(), fstvaflistserde));
        stopchanges.to("changes-by-stopid", Produced.with(Serdes.String(), fstvasserde));
        return streamin;
    }

    private VehicleActivity findPossibleServices(VehicleActivity va, ServiceList sd) {
        List<String> possibilities = new ArrayList<>();
        if (sd == null) {
            return null;
        }

        LocalDate date = va.getOperatingDate();
        DayOfWeek dow = date.getDayOfWeek();

        for (StreamServiceData sdb : sd) {
            LOG.debug("Checking service {} for route {}, line {}", sdb.getServiceId(), va.getInternalLineId(), va.getLineId());

            // The date was specifically added to timetables, so it should override anything else.
            if (sdb.getInUse().contains(date)) {
                possibilities.add(sdb.getServiceId());
                continue;
            }

            // The date was specifically removed from timetables, so it should override anything else.
            if (sdb.getNotInUse().contains(date)) {
                continue;
            }

            // Check if the service is one direction only, and doesn't match current direction.
            byte extra = sdb.getExtra();
            if (va.getDirection().equals("1")) {
                if ((extra &= 0x1) == 0) {
                    continue;
                }
            }
            if (va.getDirection().equals("2")) {
                if ((extra &= 0x2) == 0) {
                    continue;
                }
            }

            byte result = 0x0;
            switch (dow) {
                case MONDAY:
                    result = (byte) (sdb.getWeekdays() & 0x1);
                    break;
                case TUESDAY:
                    result = (byte) (sdb.getWeekdays() & 0x2);
                    break;
                case WEDNESDAY:
                    result = (byte) (sdb.getWeekdays() & 0x4);
                    break;
                case THURSDAY:
                    result = (byte) (sdb.getWeekdays() & 0x8);
                    break;
                case FRIDAY:
                    result = (byte) (sdb.getWeekdays() & 0x10);
                    break;
                case SATURDAY:
                    result = (byte) (sdb.getWeekdays() & 0x20);
                    break;
                case SUNDAY:
                    result = (byte) (sdb.getWeekdays() & 0x40);
                    break;
            }

            if (result == 0x0 && sdb.getWeekdays() != 0x0) {
                continue;
            }

            if (sdb.getValidFrom().isAfter(date)) {
                continue;
            }
            if (sdb.getValidUntil().isBefore(date)) {
                continue;
            }

            possibilities.add(sdb.getServiceId());
            LOG.debug("Service {} matches for route {}, line {}, time {}",
                    sdb.getServiceId(), va.getInternalLineId(), va.getLineId(), va.getStartTime());
        }
        va.setServicePossibilities(possibilities);
        return va;
    }

    private VehicleActivity findTrip(VehicleActivity va, StartTimesToTrips sd) {
        String tripId = null;

        if (sd == null) {
            return va;
        }

        tripId = sd.starttimes.get(va.getStartTime().toSecondOfDay());
        if (tripId == null) {
            LOG.warn("Time {} not found in maps, route {}, line {}, service {}",
                    va.getStartTime(), va.getInternalLineId(), va.getLineId(), va.getServiceID());
            return va;
        }
        LOG.info("Time {} was found in maps, route {}, line {}, service {}, trip {}",
                va.getStartTime(), va.getInternalLineId(), va.getLineId(), va.getServiceID(), tripId);

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
            LOG.debug("Removed vehicle {} from line {}", value.getVehicleId(), key);
        }
        return aggregate;
    }

    VehicleActivity addMissingStopTimes(VehicleActivity left, TripStopSet right) {
        if (right == null) {
            return left;
        }

        NavigableSet<TripStop> missing = null;
        TripStop stop = findNextStop(left.getNextStopId(), left.getNextStopSequence(), right);
        if (stop != null) {
            missing = right.tailSet(stop, true);
        }

        if (missing != null && missing.size() > 0) {
            // Assume that the vehicle's driver will try to keep the timetable and
            // will try to adjust driving speed accordingly, so we add or subtract
            // a moderate amount, max +-10 seconds per stop pair along the route.
            if (left.getDelay() == null) {
                // Quick fix. In reality, we could calculate it ourselves.
                left.setDelay(0);
            }
            int delay = left.getDelay();
            int adjust = delay / missing.size();
            if (adjust < -10) {
                adjust = -10;
            }
            if (adjust > 10) {
                adjust = 10;
            }
            for (TripStop miss : missing) {
                delay -= adjust;
                // Use Siri and GTFS-RT definition of the meaning of delay.
                GTFSLocalTime newtime = miss.arrivalTime.plusSeconds(delay);
                TripStop toadd = new TripStop();
                toadd.seq = miss.seq;
                toadd.stopid = miss.stopid;
                toadd.arrivalTime = newtime;
                left.getOnwardCalls().add(toadd);
            }

            LOG.debug("Added missing stops.");
        }

        return left;
    }

    TripStop findNextStop(String name, Integer seq, TripStopSet set) {
        for (TripStop stop : set) {
            if (seq != null) {
                if (seq.equals(stop.seq)) {
                    return stop;
                }
            }
            else if (stop.stopid.equals(name)) {
                return stop;
            }
        }
        return null;
    }
}
