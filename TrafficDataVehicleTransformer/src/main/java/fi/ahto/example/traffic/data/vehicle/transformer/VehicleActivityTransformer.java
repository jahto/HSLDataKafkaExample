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
import fi.ahto.example.traffic.data.contracts.internal.RouteStop;
import fi.ahto.example.traffic.data.contracts.internal.RouteData;
import fi.ahto.example.traffic.data.contracts.internal.RouteStopSet;
import fi.ahto.example.traffic.data.contracts.internal.StopData;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import fi.ahto.example.traffic.data.contracts.internal.VehicleDataList;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
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

    @Bean
    public KStream<String, VehicleActivity> kStream(StreamsBuilder builder) {
        LOG.debug("Constructing stream from data-by-vehicleid");
        final JsonSerde<VehicleActivity> vafserde = new JsonSerde<>(VehicleActivity.class, objectMapper);
        final JsonSerde<VehicleDataList> vaflistserde = new JsonSerde<>(VehicleDataList.class, objectMapper);
        final JsonSerde<StopData> stopserde = new JsonSerde<>(StopData.class, objectMapper);
        final JsonSerde<RouteData> routeserde = new JsonSerde<>(RouteData.class, objectMapper);

        KStream<String, VehicleActivity> streamin = builder.stream("data-by-vehicleid", Consumed.with(Serdes.String(), vafserde));

        GlobalKTable<String, StopData> stops
                = builder.globalTable("stops",
                        Consumed.with(Serdes.String(), stopserde),
                        Materialized.<String, StopData, KeyValueStore<Bytes, byte[]>>as("stops"));

        GlobalKTable<String, RouteData> routes
                = builder.globalTable("routes",
                        Consumed.with(Serdes.String(), routeserde),
                        Materialized.<String, RouteData, KeyValueStore<Bytes, byte[]>>as("routestops"));

        // We do currently not use this stream for anything other than its side effects.
        // I.e. adding all the remaining stops to onwardcalls.
        KStream<String, VehicleActivity> foostream = streamin.leftJoin(routes,
                (String key, VehicleActivity value) -> {
                    return value.getInternalLineId();
                },
                (VehicleActivity left, RouteData right) -> {
                    return mapNextStops(left, right);
                });

        // We do currently not use this stream for anything other than its side effects.
        KStream<String, VehicleActivity> barstream = streamin.leftJoin(stops,
                (String key, VehicleActivity value) -> {
                    if (value.getNextStopId() == null) {
                        return "FOOBARBAZ"; // Will most probably not match...
                    }
                    return value.getNextStopId();
                },
                (VehicleActivity left, StopData right) -> {
                    if (right == null || left.getNextStopName() != null) {
                        return left;
                    }
                    left.setNextStopName(right.stopname);
                    return left;
                });

        final JsonSerde<VehicleSet> treeserde = new JsonSerde<>(VehicleSet.class, objectMapper);
        VehicleTransformer transformer = new VehicleTransformer(builder, Serdes.String(), treeserde, "vehicle-transformer-extended");
        KStream<String, VehicleActivity> transformed = streamin.transform(transformer, "vehicle-transformer-extended");

        // Collect a rough history per vehicle and day.
        KStream<String, VehicleActivity> tohistory
                = transformed.filter((key, value) -> value.AddToHistory());

        Initializer<VehicleDataList> vehicleinitializer = new Initializer<VehicleDataList>() {
            @Override
            public VehicleDataList apply() {
                VehicleDataList valist = new VehicleDataList();
                // List<VehicleActivity> list = new ArrayList<>();
                // valist.setVehicleActivities(list);
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

    private VehicleActivity mapNextStops(VehicleActivity left, RouteData right) {
        if (right == null) {
            return left;
        }

        RouteStopSet set = null;
        if (left.getDirection().equals("1")) {
            set = right.stopsforward;
        }
        if (left.getDirection().equals("2")) {
            set = right.stopsbackward;
        }

        if (set == null) {
            return left;
        }

        if (left.getNextStopId() == null && left.getStopPoint() != null) {
            LOG.warn("Should't be here anymore?... (mapNextStops)");
            Iterator<RouteStop> iter = set.iterator();
            while (iter.hasNext()) {
                RouteStop stop = iter.next();
                if (left.getStopPoint().equals(stop.stopid)) {
                    if (iter.hasNext()) {
                        RouteStop next = iter.next();
                        left.setNextStopId(next.stopid);
                    }
                    break;
                }
            }
        }

        RouteStopSet calls = left.getOnwardCalls();
        if (calls.isEmpty()) {
            String next = left.getNextStopId();
            if (next != null) {
                for (RouteStop stop : set) {
                    if (stop.stopid.equals(next)) {
                        NavigableSet<RouteStop> missing = set.tailSet(stop, false);
                        if (missing != null && missing.size() > 0) {
                            calls.addAll(missing);
                        }
                        break;
                    }
                }
            }
        } else {
            RouteStop last = calls.last();
            NavigableSet<RouteStop> missing = set.tailSet(last, false);
            if (missing != null && missing.size() > 0) {
                calls.addAll(missing);
            }
        }

        if (left.getNextStopId() != null) {
            if (left.getNextStopId().equals(set.first().stopid)) {
                left.setAtRouteStart(true);
            }
            if (left.getNextStopId().equals(set.last().stopid)) {
                left.setAtRouteEnd(true);
            }
        } else {
            LOG.warn("For some reason, we still haven't found next stop (mapNextStops)");
        }

        if (left.isAtRouteStart() && left.isAtRouteEnd()) {
            LOG.warn("Something is clearly very wrong with the logic in method mapNextStops()!");
        }
        
        if ("FI:HSL".equals(left.getSource())) {
            String routeid = left.getInternalLineId();
            String stopid = set.first().stopid;
            String dir = left.getDirection();
            LocalTime lt = left.getTripStart().toLocalTime();
            String namefinal = routeid + "_" + stopid + "_" + dir + "_" + lt.toString().replace(":", "");
            left.setTripID(namefinal);

        }

        return left;
    }

    class VehicleTransformer
            implements TransformerSupplier<String, VehicleActivity, KeyValue<String, VehicleActivity>> {

        final protected String storeName;

        public VehicleTransformer(StreamsBuilder builder, Serde<String> keyserde, Serde<VehicleSet> valserde, String storeName) {
            StoreBuilder<KeyValueStore<String, VehicleSet>> store
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

            protected KeyValueStore<String, VehicleSet> store;
            protected ProcessorContext context;

            // See next method init. Almost impossible to debug
            // with old data if we clean it away every 60 seconds. 
            private static final boolean TESTING = true;

            @Override
            public void init(ProcessorContext context) {
                this.context = context;
                this.store = (KeyValueStore<String, VehicleSet>) context.getStateStore(storeName);

                // Schedule a punctuate() method every 60000 milliseconds based on wall-clock time.
                // The idea is to finally get rid of vehicles we haven't received any data for a while.
                // Like night-time.
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
            }

            @Override
            public KeyValue<String, VehicleActivity> transform(String key, VehicleActivity value) {
                VehicleSet data = store.get(key);
                if (data == null) {
                    data = new VehicleSet();
                }
                VehicleActivity transformed = transform(value, data);
                store.put(key, data);
                return KeyValue.pair(key, transformed);
            }

            VehicleActivity transform(VehicleActivity current, VehicleSet data) {
                if (data.isEmpty()) {
                    current.setAddToHistory(true);
                    data.add(current);
                    return current;
                }

                boolean calculate = true;

                VehicleActivity previous = data.last();

                // We do not accept records coming in too late.
                if (current.getRecordTime().isBefore(previous.getRecordTime())) {
                    return previous;
                }

                // We get duplicates quite often, with the later ones missing
                // some data.
                if (current.getRecordTime().equals(previous.getRecordTime())) {
                    return previous;
                }

                // Vehicle has changed line, useless to calculate the change of delay.
                // But we want a new history record now.
                if (current.getInternalLineId().equals(previous.getInternalLineId()) == false) {
                    current.setAddToHistory(true);
                    previous.setLineHasChanged(true);
                    context.forward(previous.getVehicleId(), previous);
                    calculate = false;
                }

                // Change of direction, useless to calculate the change of delay.
                // But we want a new history record now.
                if (current.getDirection().equals(previous.getDirection()) == false) {
                    current.setAddToHistory(true);
                    calculate = false;
                }

                // Not yet added to history? Find out when we added last time.
                // If more than 60 seconds, then add.
                if (current.AddToHistory() == false) {
                    Iterator<VehicleActivity> iter = data.descendingIterator();
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

                if (calculate) {
                    VehicleActivity reference = null;
                    Iterator<VehicleActivity> iter = data.descendingIterator();

                    while (iter.hasNext()) {
                        reference = iter.next();

                        if (reference.AddToHistory()) {
                            break;
                        }
                    }

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

                // Clean up any data older than 600 seconds.
                Instant compareto = current.getRecordTime().minusSeconds(600);
                Iterator<VehicleActivity> iter = data.iterator();

                while (iter.hasNext()) {
                    VehicleActivity next = iter.next();

                    if (next.getRecordTime().isBefore(compareto)) {
                        iter.remove();
                        continue;
                    }
                    // Safe to break from the loop now;
                    break;
                }

                data.add(current);

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
