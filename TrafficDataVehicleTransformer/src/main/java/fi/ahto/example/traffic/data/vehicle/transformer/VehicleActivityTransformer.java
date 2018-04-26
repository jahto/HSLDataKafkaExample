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
import fi.ahto.example.traffic.data.contracts.internal.RouteStops;
import fi.ahto.example.traffic.data.contracts.internal.RouteStopSet;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivityFlattened;
import fi.ahto.example.traffic.data.contracts.internal.VehicleDataList;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
    static class VehicleSet extends TreeSet<VehicleActivityFlattened> {

        public VehicleSet() {
            super((VehicleActivityFlattened o1, VehicleActivityFlattened o2) -> o1.getRecordTime().compareTo(o2.getRecordTime()));
        }
    }

    @Bean
    public KStream<String, VehicleActivityFlattened> kStream(StreamsBuilder builder) {
        LOG.debug("Constructing stream from data-by-vehicleid");
        final JsonSerde<VehicleActivityFlattened> vafserde = new JsonSerde<>(VehicleActivityFlattened.class, objectMapper);
        final JsonSerde<VehicleDataList> vaflistserde = new JsonSerde<>(VehicleDataList.class, objectMapper);
        final JsonSerde<RouteStops> routestopsserde = new JsonSerde<>(RouteStops.class, objectMapper);

        KStream<String, VehicleActivityFlattened> streamin = builder.stream("data-by-vehicleid", Consumed.with(Serdes.String(), vafserde));

        GlobalKTable<String, String> stops
                = builder.globalTable("stops",
                        Consumed.with(Serdes.String(), Serdes.String()),
                        Materialized.<String, String, KeyValueStore<Bytes, byte[]>>as("stops"));

        GlobalKTable<String, RouteStops> routestops
                = builder.globalTable("routes-to-stops",
                        Consumed.with(Serdes.String(), routestopsserde),
                        Materialized.<String, RouteStops, KeyValueStore<Bytes, byte[]>>as("routestops"));

        // We do currently not use this stream for anything other than its side effects.
        KStream<String, VehicleActivityFlattened> foostream = streamin.leftJoin(routestops,
                (String key, VehicleActivityFlattened value) -> {
                    return value.getInternalLineId();
                },
                (VehicleActivityFlattened left, RouteStops right) -> {
                    if (left.getNextStopId() == null && left.getStopPoint() != null) {
                        if (right != null) {
                            RouteStopSet set = null;
                            if (left.getDirection().equals("1")) {
                                set = right.forward;
                            }
                            if (left.getDirection().equals("2")) {
                                set = right.backward;
                            }
                            if (set != null) {
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
                        }
                    }
                   return left; 
                });
        // We do currently not use this stream for anything other than its side effects.
        KStream<String, VehicleActivityFlattened> barstream = streamin.leftJoin(stops,
                (String key, VehicleActivityFlattened value) -> {
                    if (value.getNextStopId() == null) {
                        return "FOOBARBAZ"; // Will most probably not match...
                    }
                    return value.getNextStopId();
                },
                (VehicleActivityFlattened left, String right) -> {
                    if (right == null || left.getNextStopName() != null) {
                        return left;
                    }
                    left.setNextStopName(right);
                    return left;
                });

        final JsonSerde treeserde = new JsonSerde<>(VehicleSet.class, objectMapper);
        VehicleTransformer transformer = new VehicleTransformer(builder, Serdes.String(), treeserde, "vehicle-transformer-extended");
        KStream<String, VehicleActivityFlattened> transformed = streamin.transform(transformer, "vehicle-transformer-extended");

        // Collect a rough history per vehicle and day.
        KStream<String, VehicleActivityFlattened> tohistory
                = transformed.filter((key, value) -> value.AddToHistory());

        Initializer<VehicleDataList> vehicleinitializer = new Initializer<VehicleDataList>() {
            @Override
            public VehicleDataList apply() {
                VehicleDataList valist = new VehicleDataList();
                List<VehicleActivityFlattened> list = new ArrayList<>();
                valist.setVehicleActivity(list);
                return valist;
            }
        };

        Aggregator<String, VehicleActivityFlattened, VehicleDataList> vehicleaggregator
                = new Aggregator<String, VehicleActivityFlattened, VehicleDataList>() {
            @Override
            public VehicleDataList apply(String key, VehicleActivityFlattened value, VehicleDataList aggregate) {
                List<VehicleActivityFlattened> list = aggregate.getVehicleActivity();

                // Just in case, guard once again against duplicates
                Iterator<VehicleActivityFlattened> iter = list.iterator();
                while (iter.hasNext()) {
                    VehicleActivityFlattened next = iter.next();
                    if (value.getRecordTime().equals(next.getRecordTime())) {
                        return aggregate;
                    }
                }

                list.add(value);
                return aggregate;
            }
        };

        KTable<String, VehicleDataList> vehiclehistory = tohistory
                .map((String key, VehicleActivityFlattened value) -> {
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

        KStream<String, VehicleActivityFlattened> tolines
                = transformed
                        .map((key, value)
                                -> KeyValue.pair(value.getInternalLineId(), value));

        tolines.to("data-by-lineid", Produced.with(Serdes.String(), vafserde));

        /* Seems not to be needed, but leaving still here just in case...
        KStream<String, VehicleActivityFlattened> tochanges  = 
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

    class VehicleTransformer
            implements TransformerSupplier<String, VehicleActivityFlattened, KeyValue<String, VehicleActivityFlattened>> {

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
        public Transformer<String, VehicleActivityFlattened, KeyValue<String, VehicleActivityFlattened>> get() {
            return new TransformerImpl();
        }

        class TransformerImpl implements Transformer<String, VehicleActivityFlattened, KeyValue<String, VehicleActivityFlattened>> {

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
                            VehicleActivityFlattened vaf = entry.value.last();
                            Instant now  = Instant.ofEpochMilli(timestamp);
                            
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
            public KeyValue<String, VehicleActivityFlattened> transform(String key, VehicleActivityFlattened value) {
                VehicleSet data = store.get(key);
                if (data == null) {
                    data = new VehicleSet();
                }
                VehicleActivityFlattened transformed = transform(value, data);
                store.put(key, data);
                return KeyValue.pair(key, transformed);
            }

            VehicleActivityFlattened transform(VehicleActivityFlattened current, VehicleSet data) {
                if (data.isEmpty()) {
                    current.setAddToHistory(true);
                    data.add(current);
                    return current;
                }

                boolean calculate = true;

                VehicleActivityFlattened previous = data.last();

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
                    Iterator<VehicleActivityFlattened> iter = data.descendingIterator();
                    Instant compareto = current.getRecordTime().minusSeconds(60);

                    while (iter.hasNext()) {
                        VehicleActivityFlattened next = iter.next();

                        if (next.AddToHistory()) {
                            if (next.getRecordTime().isBefore(compareto)) {
                                current.setAddToHistory(true);
                            }
                            break;
                        }
                    }
                }

                if (calculate) {
                    VehicleActivityFlattened reference = null;
                    Iterator<VehicleActivityFlattened> iter = data.descendingIterator();

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
                Iterator<VehicleActivityFlattened> iter = data.iterator();

                while (iter.hasNext()) {
                    VehicleActivityFlattened next = iter.next();

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
            public KeyValue<String, VehicleActivityFlattened> punctuate(long timestamp) {
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
