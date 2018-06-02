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
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import fi.ahto.example.traffic.data.contracts.internal.VehicleHistoryRecord;
import fi.ahto.example.traffic.data.contracts.internal.VehicleHistorySet;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Aggregator;
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

    @Bean
    public KStream<String, VehicleActivity> kStream(StreamsBuilder builder) {
        LOG.debug("Constructing stream from data-by-vehicleid");
        final JsonSerde<VehicleActivity> vafserde = new JsonSerde<>(VehicleActivity.class, objectMapper);
        final JsonSerde<VehicleHistorySet> vsetserde = new JsonSerde<>(VehicleHistorySet.class, objectMapper);

        KStream<String, VehicleActivity> streamin = builder.stream("data-by-vehicleid", Consumed.with(Serdes.String(), vafserde));

        VehicleTransformer transformer = new VehicleTransformer(builder, Serdes.String(), vafserde, "vehicle-transformer-extended");
        KStream<String, VehicleActivity> transformed = streamin.transform(transformer, "vehicle-transformer-extended");

        // Collect a rough history per vehicle and day.
        KStream<String, VehicleActivity> tohistory
                = transformed.filter((key, value) -> value.AddToHistory());

        Initializer<VehicleHistorySet> vehicleinitializer = new Initializer<VehicleHistorySet>() {
            @Override
            public VehicleHistorySet apply() {
                VehicleHistorySet valist = new VehicleHistorySet();
                return valist;
            }
        };

        Aggregator<String, VehicleActivity, VehicleHistorySet> vehicleaggregator
                = (String key, VehicleActivity value, VehicleHistorySet aggregate) -> {
                    VehicleHistoryRecord vhr = new VehicleHistoryRecord(value);
                    LOG.debug("Aggregating vehicle " + key);
                    aggregate.add(vhr);
                    return aggregate;
                };

        KTable<String, VehicleHistorySet> vehiclehistory = tohistory
                .filter((String key, VehicleActivity value) -> value.getTripStart() != null)
                .map((String key, VehicleActivity value) -> {
                    String postfix = value.getTripStart().format(DateTimeFormatter.ISO_LOCAL_DATE);
                    String newkey = value.getVehicleId() + "-" + postfix;
                    return KeyValue.pair(newkey, value);
                })
                .groupByKey(Serialized.with(Serdes.String(), vafserde))
                .aggregate(vehicleinitializer, vehicleaggregator,
                        Materialized.<String, VehicleHistorySet, KeyValueStore<Bytes, byte[]>>as("vehicle-aggregation-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(vsetserde)
                );

        vehiclehistory.toStream().to("vehicle-history", Produced.with(Serdes.String(), vsetserde));
        
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

                this.context.schedule(60000, PunctuationType.WALL_CLOCK_TIME, (timestamp) -> {
                    KeyValueIterator<String, VehicleActivity> iter = this.store.all();
                    while (iter.hasNext()) {
                        KeyValue<String, VehicleActivity> entry = iter.next();
                        if (entry.value != null) {
                            VehicleActivity vaf = entry.value;
                            Instant now = Instant.ofEpochMilli(timestamp);

                            if (vaf.getRecordTime().plusSeconds(60).isBefore(now) && !TESTING) {
                                vaf.setLineHasChanged(true);
                                context.forward(vaf.getVehicleId(), vaf);
                                this.store.delete(entry.key);
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
            public KeyValue<String, VehicleActivity> transform(String k, VehicleActivity v) {
                VehicleActivity previous = store.get(k);
                VehicleActivity transformed = transform(v, previous);
                store.put(k, transformed);
                return KeyValue.pair(k, transformed);
            }
            
            VehicleActivity transform(VehicleActivity current, VehicleActivity previous) {
                LOG.info("Transforming vehicle " + current.getVehicleId());
                
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
                    LOG.debug("Vehicle is at line end " + current.getVehicleId());
                }
                
                // Vehicle has changed line, useless to calculate the change of delay.
                // But we want a new history record now.
                if (current.getInternalLineId().equals(previous.getInternalLineId()) == false) {
                    current.setAddToHistory(true);
                    current.setLastAddToHistory(current.getRecordTime());
                    previous.setLineHasChanged(true);
                    context.forward(previous.getVehicleId(), previous);
                    LOG.debug("Vehicle has changed line " + current.getVehicleId());
                    calculate = false;
                }

                // Change of direction, useless to calculate the change of delay.
                // But we want a new history record now.
                if (current.getDirection().equals(previous.getDirection()) == false) {
                    current.setAddToHistory(true);
                    current.setLastAddToHistory(current.getRecordTime());
                    LOG.debug("Vehicle has changed direction " + current.getVehicleId());
                    calculate = false;
                }

                // Not yet added to history? Find out when we added last time.
                // If more than 59 seconds, then add.
                if (current.AddToHistory() == false) {
                    Instant compareto = current.getRecordTime().minusSeconds(59);
                    if (previous.getLastAddToHistory().isBefore(compareto)) {
                        current.setAddToHistory(true);
                        current.setLastAddToHistory(current.getRecordTime());
                    }
                    else {
                        current.setLastAddToHistory(previous.getLastAddToHistory());
                    }
                }

                if (calculate) {
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
