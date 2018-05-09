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
import fi.ahto.example.traffic.data.contracts.internal.ServiceData;
import fi.ahto.example.traffic.data.contracts.internal.ServiceStop;
import fi.ahto.example.traffic.data.contracts.internal.TripStop;
import fi.ahto.example.traffic.data.contracts.internal.TripStopSet;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import fi.ahto.example.traffic.data.contracts.internal.VehicleDataList;
import fi.ahto.kafka.streams.state.utils.SimpleTransformerSupplierWithStore;
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
import org.apache.kafka.streams.kstream.ValueMapper;
import org.apache.kafka.streams.kstream.ValueTransformer;
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
    static class ServiceList extends ArrayList<String> {
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
        final JsonSerde<ServiceData> serviceserde = new JsonSerde<>(ServiceData.class, objectMapper);
        final JsonSerde<ServiceList> sdbserde = new JsonSerde<>(ServiceList.class, objectMapper);

        KStream<String, VehicleActivity> streamin = builder.stream("data-by-lineid", Consumed.with(Serdes.String(), vafserde));

        GlobalKTable<String, ServiceData> services
                = builder.globalTable("services",
                        Consumed.with(Serdes.String(), serviceserde),
                        Materialized.<String, ServiceData, KeyValueStore<Bytes, byte[]>>as("services"));

        GlobalKTable<String, TripStopSet> trips
                = builder.globalTable("trips",
                        Consumed.with(Serdes.String(), tripsserde),
                        Materialized.<String, TripStopSet, KeyValueStore<Bytes, byte[]>>as("trips"));

        GlobalKTable<String, ServiceList> servicesbase
                = builder.globalTable("trips-to-services",
                        Consumed.with(Serdes.String(), sdbserde),
                        Materialized.<String, ServiceList, KeyValueStore<Bytes, byte[]>>as("trips-to-services"));

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
        // We do currently not use this stream for anything other than its side effects.
        // I.e. mapping to possible services.

        KStream<String, VehicleActivity> tripstream = streamin.leftJoin(servicesbase,
                (String key, VehicleActivity value) -> {
                    String newkey = value.getSource() + ":" + value.getStartTime().toString() + "/" + value.getDirection() + "/" + value.getInternalLineId();
                    return newkey;
                },
                (VehicleActivity value, ServiceList right) -> {
                    if (right == null) {
                        String newkey = value.getSource() + ":" + value.getStartTime().toString() + "/" + value.getDirection() + "/" + value.getInternalLineId();
                        LOG.debug("Didn't find correct servicelist " + newkey);
                    }
                    value.setPossibleServices(right);
                    return value;
                });

        KStream<String, VehicleActivity> checkservice = streamin
                .flatMapValues((VehicleActivity v) -> {
                    List<VehicleActivity> list = new ArrayList<>();
                    for (String serviceid : v.getPossibleServices()) {
                        VehicleActivity add = new VehicleActivity(v);
                        add.setServiceID(serviceid);
                        list.add(add);
                    }
                    return list;
                });

        KStream<String, VehicleActivity> finalstream = checkservice.leftJoin(services,
                (String key, VehicleActivity value) -> {
                    return value.getServiceID();
                },
                (VehicleActivity left, ServiceData right) -> {
                    return checkService(left, right);
                }
        ).filter((k, v) -> v.getTripID() != null);

        // Add possibly missing remaining stops 
        KStream<String, VehicleActivity> foo = finalstream
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
        KStream<String, VehicleActivity> transformed = finalstream
                .map((String key, VehicleActivity va) -> KeyValue.pair(va.getVehicleId(), va))
                .transform(transformer, "stop-times");

        lines.toStream().to("data-by-lineid-enhanced", Produced.with(Serdes.String(), vaflistserde));
        return streamin;
    }

    private VehicleActivity checkService(VehicleActivity va, ServiceData sd) {
        // FOLI GTFS data does not contain any valid dates, but we
        // get the right (?) trip id already from realtime feed.
        // Chek if it matches, an if so, return immediately.
        if (va.getTripID() != null) {
            // Didn't actually check...
            return va;
        }
        
        va.setServiceID(null);
        va.setTripID(null);

        if (sd == null) {
            return va;
        }
        String tripId = null;

        LocalDate date = va.getOperatingDate();
        DayOfWeek dow = date.getDayOfWeek();

        byte result = 0x0;
        switch (dow) {
            case MONDAY:
                result = (byte) (sd.weekdays & 0x1);
                break;
            case TUESDAY:
                result = (byte) (sd.weekdays & 0x2);
                break;
            case WEDNESDAY:
                result = (byte) (sd.weekdays & 0x4);
                break;
            case THURSDAY:
                result = (byte) (sd.weekdays & 0x8);
                break;
            case FRIDAY:
                result = (byte) (sd.weekdays & 0x10);
                break;
            case SATURDAY:
                result = (byte) (sd.weekdays & 0x20);
                break;
            case SUNDAY:
                result = (byte) (sd.weekdays & 0x40);
                break;
        }

        if (result == 0x0) {
            return va;
        }

        if (sd.validfrom.isAfter(date)) {
            return va;
        }
        if (sd.validuntil.isBefore(date)) {
            return va;
        }
        if (sd.notinuse.contains(date)) {
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

        va.setServiceID(sd.serviceId);
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
            LOG.debug("Removed vehicle " + value.getVehicleId() + " from line " + key);
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

    static class TimeTableComparerSupplier extends SimpleTransformerSupplierWithStore<String, VehicleActivity> {

        public TimeTableComparerSupplier(StreamsBuilder builder, Serde<String> keyserde, Serde<VehicleActivity> valserde, String storeName) {
            super(builder, keyserde, valserde, storeName);
        }

        @Override
        public Transformer<String, VehicleActivity, KeyValue<String, VehicleActivity>> get() {
            return new TransformerImpl() {
                @Override
                protected VehicleActivity transformValue(VehicleActivity previous, VehicleActivity current) {
                    return compareTimeTables(previous, current);
                }
            };
        }

        VehicleActivity compareTimeTables(VehicleActivity previous, VehicleActivity current) {
            LOG.debug("Comparing timetables.");
            if (previous == null) {
                return current;
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
                        LOG.debug("Fixing estimated arrival times.");
                    }
                }
            }
            if (curstop != null) {
                NavigableSet<ServiceStop> remove = previous.getOnwardCalls().headSet(curstop, false);
                if (remove != null && remove.size() > 0) {
                    // Vehicle has gone past these stops, so will not be arriving
                    // to them anymore. Push the information to some queue.
                    LOG.debug("Removing stops.");
                }
            }

            return current;
        }
    }
}
