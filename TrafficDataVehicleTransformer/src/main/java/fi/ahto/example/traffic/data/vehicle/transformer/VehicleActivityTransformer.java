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
package fi.ahto.example.traffic.data.vehicle.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.ahto.example.traffic.data.contracts.internal.RouteData;
import fi.ahto.example.traffic.data.contracts.internal.ServiceData;
import fi.ahto.example.traffic.data.contracts.internal.ServiceDataBase;
import fi.ahto.example.traffic.data.contracts.internal.StopData;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import fi.ahto.example.traffic.data.contracts.internal.VehicleDataList;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
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
import org.apache.kafka.streams.kstream.TransformerSupplier;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
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
public class VehicleActivityTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(VehicleActivityTransformer.class);

    @Autowired
    private ObjectMapper objectMapper;

    void VehicleActivityTransformers() {
        LOG.debug("VehicleActivityTransformers created");
    }

    // Must be static when declared here as inner class, otherwise you run into
    // problems with Jackson objectmapper and databinder.
    static class VehicleSet extends TreeSet<VehicleActivity> {

        private static final long serialVersionUID = -5926884652895033023L;

        public VehicleSet() {
            super((VehicleActivity o1, VehicleActivity o2) -> o1.getRecordTime().compareTo(o2.getRecordTime()));
        }
    }

    static class ServiceList extends ArrayList<String> {
    }

    @Bean
    public KStream<String, VehicleActivity> kStream(StreamsBuilder builder) {
        LOG.debug("Constructing stream from data-by-vehicleid");
        final JsonSerde<VehicleActivity> vafserde = new JsonSerde<>(VehicleActivity.class, objectMapper);
        final JsonSerde<VehicleDataList> vaflistserde = new JsonSerde<>(VehicleDataList.class, objectMapper);
        // final JsonSerde<StopData> stopserde = new JsonSerde<>(StopData.class, objectMapper);
        final JsonSerde<RouteData> routeserde = new JsonSerde<>(RouteData.class, objectMapper);
        final JsonSerde<ServiceList> sdbserde = new JsonSerde<>(ServiceList.class, objectMapper);
        // final JsonSerde<ServiceData> serviceserde = new JsonSerde<>(ServiceData.class, objectMapper);

        KStream<String, VehicleActivity> streamin = builder.stream("data-by-vehicleid", Consumed.with(Serdes.String(), vafserde));
        /*
        GlobalKTable<String, StopData> stops
                = builder.globalTable("stops",
                        Consumed.with(Serdes.String(), stopserde),
                        Materialized.<String, StopData, KeyValueStore<Bytes, byte[]>>as("stops"));
         */
        GlobalKTable<String, RouteData> routes
                = builder.globalTable("routes",
                        Consumed.with(Serdes.String(), routeserde),
                        Materialized.<String, RouteData, KeyValueStore<Bytes, byte[]>>as("routes"));

        GlobalKTable<String, ServiceList> servicesbase
                = builder.globalTable("trips-to-services",
                        Consumed.with(Serdes.String(), sdbserde),
                        Materialized.<String, ServiceList, KeyValueStore<Bytes, byte[]>>as("trips-to-services"));

        GlobalKTable<String, String> serviceids
                = builder.globalTable("serviceids",
                        Consumed.with(Serdes.String(), Serdes.String()),
                        Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("serviceids"));

        /*
        GlobalKTable<String, ServiceData> services
                = builder.globalTable("services",
                        Consumed.with(Serdes.String(), serviceserde),
                        Materialized.<String, ServiceData, KeyValueStore<Bytes, byte[]>>as("services"));
         */
        // We do currently not use this stream for anything other than its side effects.
        // I.e. mapping correct service and trip ids.
        KStream<String, VehicleActivity> tripstream = streamin.leftJoin(servicesbase,
                (String key, VehicleActivity value) -> {
                    String newkey = value.getSource() + ":" + value.getStartTime().toString() + "/" + value.getDirection() + "/" + value.getInternalLineId();
                    return newkey;
                    // return value.getInternalLineId();
                },
                (VehicleActivity value, ServiceList right) -> {
                    if (right == null) {
                        String newkey = value.getSource() + ":" + value.getStartTime().toString() + "/" + value.getDirection() + "/" + value.getInternalLineId();
                        LOG.debug("Didn't find correct servicelist " + newkey);
                    }
                    /*
                    else {
                        LOG.debug("Found correct servicelist");
                    }
                     */
                    value.setPossibleServices(right);
                    return value;
                });
        /*
        KStream<String, VehicleActivity> tripstream = streamin.leftJoin(servicesbase,
                (String key, VehicleActivity value) -> {
                    String newkey = value.getSource() + ":" + value.getStartTime().toString() + "/" + value.getDirection() + "/" + value.getInternalLineId();
                    return newkey;
                    // return value.getInternalLineId();
                },
                (VehicleActivity left, ServiceList right) -> {
                    return mapServiceidNew(left, right);
                });
         */
 /*
        KStream<String, VehicleActivity> foostream = streamin.leftJoin(routes,
                (String key, VehicleActivity value) -> {
                    return value.getInternalLineId();
                },
                (VehicleActivity left, RouteData right) -> {
                    return mapServiceid(left, right);
                });
         */
        // final JsonSerde<VehicleSet> treeserde = new JsonSerde<>(VehicleSet.class, objectMapper);
        VehicleTransformer transformer = new VehicleTransformer(builder, Serdes.String(), vafserde, "vehicle-transformer-extended");
        KStream<String, VehicleActivity> transformed = streamin.transform(transformer, "vehicle-transformer-extended");

        // Collect a rough history per vehicle and day.
        KStream<String, VehicleActivity> tohistory
                = transformed.filter((key, value) -> value.AddToHistory());

        Initializer<VehicleDataList> vehicleinitializer = new Initializer<VehicleDataList>() {
            @Override
            public VehicleDataList apply() {
                VehicleDataList valist = new VehicleDataList();
                return valist;
            }
        };

        Aggregator<String, VehicleActivity, VehicleDataList> vehicleaggregator
                = (String key, VehicleActivity value, VehicleDataList aggregate) -> {
                    List<VehicleActivity> list = aggregate.getVehicleActivities();
                    if (list == null) {
                        LOG.warn("Should't be here anymore... (vehicleaggregator)");
                        list = new ArrayList<>();
                        aggregate.setVehicleActivities(list);
                    }

                    // Just in case, guard once again against duplicates
                    Iterator<VehicleActivity> iter = list.iterator();
                    while (iter.hasNext()) {
                        VehicleActivity next = iter.next();
                        if (value.getRecordTime().equals(next.getRecordTime())) {
                            return aggregate;
                        }
                    }

                    list.add(value);
                    return aggregate;
                };

        KTable<String, VehicleDataList> vehiclehistory = tohistory
                .filter((String key, VehicleActivity value) -> value.getTripStart() != null)
                .map((String key, VehicleActivity value) -> {
                    String postfix = value.getTripStart().format(DateTimeFormatter.ISO_LOCAL_DATE);
                    String newkey = value.getVehicleId() + "-" + postfix;
                    return KeyValue.pair(newkey, value);
                })
                .groupByKey(Serialized.with(Serdes.String(), vafserde))
                .aggregate(vehicleinitializer, vehicleaggregator,
                        Materialized.<String, VehicleDataList, KeyValueStore<Bytes, byte[]>>as("vehicle-aggregation-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(vaflistserde)
                );

        vehiclehistory.toStream().to("vehicle-history", Produced.with(Serdes.String(), vaflistserde));
        transformed.to("vehicles", Produced.with(Serdes.String(), vafserde));

        KStream<String, VehicleActivity> tolines
                = transformed
                        .map((key, value)
                                -> KeyValue.pair(value.getInternalLineId(), value));

        tolines.to("data-by-lineid", Produced.with(Serdes.String(), vafserde));

        /* Seems not to be needed, but leaving still here just in case...
        KStream<String, VehicleActivity> tochanges  = 
                transformed.filter((key, value) -> 
                        value.LineHasChanged())
                .map((key, value) -> 
                    {
                        LOG.debug("Vehicle " + value.getVehicleId() + " changed line.");
                        return KeyValue.pair(value.getLineId(), value);
                })
                ;

        tochanges.to("changes-by-lineid", Produced.with(Serdes.String(), vafserde));
         */
        return streamin;
    }

    // This must be rewritten. It takes too long to find the right service.
    // Can't keep up at least with HSL realtime feed. Also adding a few more
    // threads to handle the could help.
    // Consider mapping by lineid, direction, start time, should leave less possibilities
    // to test against.
    private VehicleActivity mapServiceid(VehicleActivity left, RouteData right) {
        if (right == null) {
            return left;
        }

        LocalDate date = left.getTripStart().toLocalDate();
        List<ServiceData> possibilities = new ArrayList<>();
        Map<String, ServiceData> services = right.services;
        String tripId = null;

        // FOLI GTFS data does not contain any valid dates, but we
        // get the right (?) trip id already from realtime feed.
        // Chek if it matches, an if so, return immediately.
        if (left.getTripID() != null) {
            // Didn't actually check...
            return left;
        }

        int loops = 0;

        for (ServiceData sd : services.values()) {
            loops++;
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
                continue;
            }

            if (sd.validfrom.isAfter(date)) {
                continue;
            }
            if (sd.validuntil.isBefore(date)) {
                continue;
            }
            if (sd.notinuse.contains(date)) {
                continue;
            }

            if (left.getDirection().equals("1")) {
                tripId = sd.timesforward.get(left.getStartTime());
                if (tripId == null) {
                    continue;
                }
            }

            if (left.getDirection().equals("2")) {
                tripId = sd.timesbackward.get(left.getStartTime());
                if (tripId == null) {
                    continue;
                }
            }

            possibilities.add(sd);
        }

        if (possibilities.size() == 0) {
            LOG.warn("Could not find service.");
        } else if (possibilities.size() > 1) {
            LOG.warn("Found too many possible services.");
        } else {
            LOG.debug("Found the right service, looped " + Integer.toString(loops) + " times.");
            left.setTripID(tripId);
            left.setServiceID(possibilities.get(0).serviceId);
        }
        return left;
    }

    private VehicleActivity mapServiceidPossibilities(VehicleActivity left, ServiceList right) {
        return left;
    }

    /*
    private VehicleActivity mapServiceidNew(VehicleActivity left, ServiceList right) {
        if (right == null) {
            return left;
        }
        
        LocalDate date = left.getTripStart().toLocalDate();
        List<ServiceDataBase> possibilities = new ArrayList<>();

        // FOLI GTFS data does not contain any valid dates, but we
        // get the right (?) trip id already from realtime feed.
        // Chek if it matches, an if so, return immediately.
        if (left.getTripID() != null) {
            // Didn't actually check...
            return left;
        }

        int loops = 0;
        Iterator<ServiceDataBase> iter = right.iterator();
        while (iter.hasNext()) {
            ServiceDataBase sd = iter.next();
            loops++;
            DayOfWeek dow = date.getDayOfWeek();

            byte result = 0x0;
            switch(dow) {
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
                continue;
            }

            if (sd.validfrom.isAfter(date)) {
                continue;
            }
            if (sd.validuntil.isBefore(date)) {
                continue;
            }
            if (sd.notinuse.contains(date)) {
                continue;
            }
            
            possibilities.add(sd);
            
            // Not safe when testing...
            break;
        }
        
        if (possibilities.size() == 0) {
            LOG.warn("Could not find service.");
        }
        else if (possibilities.size() > 1) {
            LOG.warn("Found too many possible services.");
        }
        else {
            LOG.debug("Found the right service, looped " + Integer.toString(loops) + " times.");
            left.setServiceID(possibilities.get(0).serviceId);
        }
        return left;
    }
     */
    class VehicleTransformer
            implements TransformerSupplier<String, VehicleActivity, KeyValue<String, VehicleActivity>> {

        final protected String storeName;

        public VehicleTransformer(StreamsBuilder builder, Serde<String> keyserde, Serde<VehicleActivity> valserde, String storeName) {
            StoreBuilder<KeyValueStore<String, VehicleActivity>> store
                    = Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(storeName),
                            keyserde,
                            valserde)
                            .withCachingEnabled();

            builder.addStateStore(store);
            this.storeName = storeName;
        }

        @Override
        public Transformer<String, VehicleActivity, KeyValue<String, VehicleActivity>> get() {
            return new TransformerImpl();
        }

        class TransformerImpl implements Transformer<String, VehicleActivity, KeyValue<String, VehicleActivity>> {

            protected KeyValueStore<String, VehicleActivity> store;
            protected ProcessorContext context;

            // See next method init. Almost impossible to debug
            // with old data if we clean it away every 60 seconds. 
            private static final boolean TESTING = true;

            @Override
            public void init(ProcessorContext context) {
                this.context = context;
                this.store = (KeyValueStore<String, VehicleActivity>) context.getStateStore(storeName);

                // Schedule a punctuate() method every 60000 milliseconds based on wall-clock time.
                // The idea is to finally get rid of vehicles we haven't received any data for a while.
                // Like night-time.
                /*
                this.context.schedule(60000, PunctuationType.WALL_CLOCK_TIME, (timestamp) -> {
                    KeyValueIterator<String, VehicleSet> iter = this.store.all();
                    while (iter.hasNext()) {
                        KeyValue<String, VehicleSet> entry = iter.next();
                        if (entry.value.isEmpty() == false) {
                            VehicleActivity vaf = entry.value.last();
                            Instant now = Instant.ofEpochMilli(timestamp);

                            if (vaf.getRecordTime().plusSeconds(60).isBefore(now) && !TESTING) {
                                context.forward(vaf.getVehicleId(), vaf);
                                entry.value.clear();
                                this.store.put(entry.key, entry.value);
                                LOG.debug("Cleared all data for vehicle " + vaf.getVehicleId() + " and removed it from line " + vaf.getInternalLineId());
                            }
                        }
                    }
                    iter.close();

                    // commit the current processing progress
                    context.commit();
                });
                 */
            }

            /*
            @Override
            public KeyValue<String, VehicleActivity> transform(String key, VehicleActivity value) {
                VehicleSet previous = store.get(key);
                if (previous == null) {
                    previous = new VehicleSet();
                }
                VehicleActivity transformed = transform(value, previous);
                store.put(key, previous);
                return KeyValue.pair(key, transformed);
            }
             */
 /*
            @Override
            public VehicleActivity transform(String k, VehicleActivity v) {
                VehicleActivity previous = store.get(k);
                VehicleActivity transformed = transform(v, previous);
                store.put(k, previous);
                return transformed;
            }
             */
            @Override
            public KeyValue<String, VehicleActivity> transform(String k, VehicleActivity v) {
                // VehicleActivity previous = store.get(k);
                // VehicleActivity transformed = transform(v, previous);
                // store.put(k, previous);
                // return KeyValue.pair(k, transformed);
                return KeyValue.pair(k, v);
            }

            VehicleActivity transform(VehicleActivity current, VehicleActivity previous) {
                if (previous == null) {
                    current.setAddToHistory(true);
                    current.setLastAddToHistory(current.getRecordTime());
                    return current;
                }

                boolean calculate = true;

                // We do not accept records coming in too late.
                if (current.getRecordTime().isBefore(previous.getRecordTime())) {
                    return previous;
                }

                // We get duplicates quite often, with the later ones missing
                // some previous.
                if (current.getRecordTime().equals(previous.getRecordTime())) {
                    return previous;
                }

                // Vehicle is at end the of line, remove it immediately. It will come back
                // later, but maybe not on the same line;
                if (current.getEol().isPresent() && current.getEol().get() == true) {
                    current.setAddToHistory(true);
                    current.setLastAddToHistory(current.getRecordTime());
                    previous.setLineHasChanged(true);
                    context.forward(previous.getVehicleId(), previous);
                }
                // Vehicle has changed line, useless to calculate the change of delay.
                // But we want a new history record now.
                if (current.getInternalLineId().equals(previous.getInternalLineId()) == false) {
                    current.setAddToHistory(true);
                    current.setLastAddToHistory(current.getRecordTime());
                    previous.setLineHasChanged(true);
                    context.forward(previous.getVehicleId(), previous);
                    calculate = false;
                }

                // Change of direction, useless to calculate the change of delay.
                // But we want a new history record now.
                if (current.getDirection().equals(previous.getDirection()) == false) {
                    current.setAddToHistory(true);
                    current.setLastAddToHistory(current.getRecordTime());
                    calculate = false;
                }

                // Not yet added to history? Find out when we added last time.
                // If more than 60 seconds, then add.
                if (current.AddToHistory() == false) {
                    Instant compareto = current.getRecordTime().minusSeconds(60);
                    if (previous.getLastAddToHistory().isBefore(compareto)) {
                        current.setAddToHistory(true);
                        current.setLastAddToHistory(current.getRecordTime());

                    }
                }
                /*
                if (current.AddToHistory() == false) {
                    Iterator<VehicleActivity> iter = previous.descendingIterator();
                    Instant compareto = current.getRecordTime().minusSeconds(60);

                    while (iter.hasNext()) {
                        VehicleActivity next = iter.next();

                        if (next.AddToHistory()) {
                            if (next.getRecordTime().isBefore(compareto)) {
                                current.setAddToHistory(true);
                            }
                            break;
                        }
                    }
                }
                 */
                if (calculate) {
                    VehicleActivity reference = null;
                    /*
                    Iterator<VehicleActivity> iter = previous.descendingIterator();

                    while (iter.hasNext()) {
                        reference = iter.next();

                        if (reference.AddToHistory()) {
                            break;
                        }
                    }
                     */
                    // Calculate approximate bearing if missing. Must check later
                    // if I got the direction right or 180 degrees wrong, and that
                    // both samples actually have the needed coordinates.
                    if (current.getBearing() == null) {
                        double lat1 = Math.toRadians(current.getLatitude());
                        double long1 = Math.toRadians(current.getLongitude());
                        double lat2 = Math.toRadians(previous.getLatitude());
                        double long2 = Math.toRadians(previous.getLongitude());

                        double bearingradians = Math.atan2(Math.asin(long2 - long1) * Math.cos(lat2), Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(long2 - long1));
                        double bearingdegrees = Math.toDegrees(bearingradians);

                        if (bearingdegrees < 0) {
                            bearingdegrees = 360 + bearingdegrees;
                        }
                        current.setBearing(bearingdegrees);
                    }

                    if (current.getDelay() != null && reference.getDelay() != null) {
                        Integer delaychange = current.getDelay() - reference.getDelay();
                        current.setDelayChange(delaychange);
                    }

                    if (current.getRecordTime() != null && reference.getRecordTime() != null) {
                        Integer measurementlength = (int) (current.getRecordTime().getEpochSecond() - reference.getRecordTime().getEpochSecond());
                        current.setMeasurementLength(measurementlength);
                    }
                }

                return current;
            }

            @Override
            public KeyValue<String, VehicleActivity> punctuate(long timestamp) {
                // Not needed and also deprecated.
                return null;
            }

            @Override
            public void close() {
                // Note: The store should NOT be closed manually here via `stateStore.close()`!
                // The Kafka Streams API will automatically close stores when necessary.
            }

        }
    }
}
