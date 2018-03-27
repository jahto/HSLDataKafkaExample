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
package fi.ahto.example.hsl.data.vehicle.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.ahto.example.hsl.data.contracts.siri.VehicleActivityFlattened;
import fi.ahto.example.hsl.data.contracts.siri.VehicleDataList;
import fi.ahto.kafka.streams.state.utils.SimpleTransformerSupplierWithStore;
import java.time.Instant;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.kstream.TransformerSupplier;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.StreamsBuilderFactoryBean;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

/**
 *
 * @author Jouni Ahto
 */
@Component
public class VehicleActivityTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(VehicleActivityTransformer.class);
    private final JsonSerde<VehicleActivityFlattened> serdein = new JsonSerde<>(VehicleActivityFlattened.class);
    private static final JsonSerde<VehicleDataList> serdeout = new JsonSerde<>(VehicleDataList.class);

    @Autowired
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @Autowired
    private ObjectMapper objectMapper;

    void VehicleActivityTransformers() {
        LOG.info("VehicleActivityTransformers created");
    }

    static class VehicleSet extends TreeSet<VehicleActivityFlattened> {

        public VehicleSet() {
            super(
                    new Comparator<VehicleActivityFlattened>() {
                @Override
                public int compare(VehicleActivityFlattened o1, VehicleActivityFlattened o2) {
                    return o1.getRecordTime().compareTo(o2.getRecordTime());
                }
            }
            );
        }
    }

    @Bean
    public KStream<String, VehicleActivityFlattened> kStream(StreamsBuilder builder) {
        LOG.info("Constructing stream from data-by-lineid to grouped-by-lineid");
        final JsonSerde<VehicleActivityFlattened> vafserde = new JsonSerde<>(VehicleActivityFlattened.class, objectMapper);
        final JsonSerde<VehicleDataList> vaflistserde = new JsonSerde<>(VehicleDataList.class, objectMapper);

        final JsonSerde treeserde = new JsonSerde<>(VehicleSet.class, objectMapper);

        KStream<String, VehicleActivityFlattened> streamin = builder.stream("data-by-vehicleid", Consumed.with(Serdes.String(), vafserde));

        VehicleTransformer transformer = new VehicleTransformer(builder, Serdes.String(), vafserde, "vehicle-transformer");
        // KStream<String, VehicleActivityFlattened> transformed = streamin.transform(transformer, "vehicle-transformer");

        VehicleTransformerExtended transformerextended = new VehicleTransformerExtended(builder, Serdes.String(), treeserde, "vehicle-transformer-extended");
        KStream<String, VehicleActivityFlattened> transformed = streamin.transform(transformerextended, "vehicle-transformer-extended");

        //KStream<String, VehicleActivityFlattened> tohistory  = 
        //        transformed.filter((key, value) -> value.setAddToHistory());
        /*
        KStream<String, VehicleActivityFlattened> linechanged
                = transformed
                        .filter((key, value) -> value.LineHasChanged())
                        .map((key, value) -> KeyValue.pair(value.getLineId(), value));
        linechanged.to("vehicle-data-line-changed", Produced.with(Serdes.String(), vafserde));

        KStream<String, VehicleActivityFlattened> forward
                = transformed
                        .filterNot((key, value) -> value.LineHasChanged())
                        .map((key, value) -> KeyValue.pair(value.getLineId(), value));
        forward.to("vehicle-data-transformed", Produced.with(Serdes.String(), vafserde));
         */
        // Construct Ktable keyed by vehicleid from streamin.
        /*
        Materialized<String, VehicleActivityFlattened, KeyValueStore<Bytes, byte[]>> materialized =
        Materialized.<String, VehicleActivityFlattened, KeyValueStore<Bytes, byte[]>>as("dummy-aggregation-store")
                .withKeySerde(Serdes.String())
                .withValueSerde(vafserde);
        
        KTable<String, VehicleActivityFlattened> allvehicles = streamin
                .map((key, value) -> KeyValue.pair(value.getVehicleId(), value))
                .groupByKey(Serialized.with(Serdes.String(), vafserde))
                .reduce((aggValue, newValue) -> {
                    // LOG.info(newValue.getLineId() + " = " + newValue.getVehicleId());
                    return newValue;
                }, materialized)
                ;
         */
 /*
        // Collect a rough history per vehicle and day.
        Initializer<VehicleDataList> vehicleinitializer = new Initializer<VehicleDataList>() {
            @Override
            public VehicleDataList apply() {
                VehicleDataList valist = new VehicleDataList();
                List<VehicleActivityFlattened> list = new ArrayList<>();
                valist.setVehicleActivity(list);
                return valist;
            }
        };
        
        Aggregator<String, VehicleActivityFlattened, VehicleDataList> vehicleaggregator =
                new Aggregator<String, VehicleActivityFlattened, VehicleDataList>() {
            @Override
            public VehicleDataList apply(String key, VehicleActivityFlattened value, VehicleDataList aggregate) {
                List<VehicleActivityFlattened> list = aggregate.getVehicleActivity();
                
                ListIterator<VehicleActivityFlattened> iter = list.listIterator(list.size());
                long time1 = value.getRecordTime().getEpochSecond();
                
                while (iter.hasPrevious()) {
                    VehicleActivityFlattened vaf = iter.previous();
                    long time2 = vaf.getRecordTime().getEpochSecond();
                    long difference = time1 - time2;
                    
                    // A sample every minute or greater is enough for a rough history
                    // for demonstration purposes. Weeds out possible duplicates too.
                    if (difference > -60 && difference < 60) {
                        return aggregate;
                    }
                }
                
                list.add(value);
                return aggregate;
            }
        };
        
        KTable<String, VehicleDataList> vehiclehistory = streamin
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
         */
        return streamin;
    }

    class VehicleTransformer extends SimpleTransformerSupplierWithStore<String, VehicleActivityFlattened> {

        public VehicleTransformer(StreamsBuilder builder, Serde<String> keyserde, Serde<VehicleActivityFlattened> valserde, String storeName) {
            super(builder, keyserde, valserde, storeName);
        }

        @Override
        protected TransformerImpl createTransformer() {
            return new TransformerImpl() {
                @Override
                protected VehicleActivityFlattened transformValue(VehicleActivityFlattened previous, VehicleActivityFlattened current) {
                    // LOG.info("Transforming vehicle " + current.getVehicleId());
                    // Vehicle hasn't been on the line.
                    if (previous == null) {
                        return current;
                    }

                    if (current.getRecordTime() != null && previous.getRecordTime() != null) {
                        Integer measurementlength = (int) (current.getRecordTime().getEpochSecond() - previous.getRecordTime().getEpochSecond());
                        LOG.info("measurementlength " + measurementlength.toString());
                    }

                    // We do not accept records coming in too late.
                    if (current.getRecordTime().isBefore(previous.getRecordTime())) {
                        return previous;
                    }

                    // Vehicle has changed line, useless to calculate the change of delay.
                    // But we want a new history record now.
                    if (current.getLineId().equals(previous.getLineId()) == true) {
                        current.setAddToHistory(true);
                        previous.setLineHasChanged(true);
                        context.forward(previous.getVehicleId(), previous);
                        return current;
                    }

                    // Change of direction, useless to calculate the change of delay.
                    // But we want a new history record now.
                    if (current.getDirection().equals(previous.getDirection()) == true) {
                        current.setAddToHistory(true);
                        return current;
                    }

                    // Vehicle hasn't been on the line long enough to calculate whether the delay is going up- or downwards.
                    // if (current.getRecordTime().minusSeconds(65).compareTo(previous.getRecordTime()) < 0) {
                    //     return current;
                    //}
                    if (current.getDelay() != null && previous.getDelay() != null) {
                        Integer delaychange = current.getDelay() - previous.getDelay();
                        current.setDelayChange(delaychange);
                    }

                    if (current.getRecordTime() != null && previous.getRecordTime() != null) {
                        Integer measurementlength = (int) (current.getRecordTime().getEpochSecond() - previous.getRecordTime().getEpochSecond());
                        current.setMeasurementLength(measurementlength);
                    }

                    return current;
                }

            };
        }
    }

    class VehicleTransformerExtended
            implements TransformerSupplier<String, VehicleActivityFlattened, KeyValue<String, VehicleActivityFlattened>> {

        final protected String storeName;

        public VehicleTransformerExtended(StreamsBuilder builder, Serde<String> keyserde, Serde<VehicleSet> valserde, String storeName) {
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

            @Override
            public void init(ProcessorContext context) {
                this.context = context;
                this.store = (KeyValueStore<String, VehicleSet>) context.getStateStore(storeName);
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
                if (current.getLineId().equals(previous.getLineId()) == false) {
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

                if (calculate) {

                }

                // Not yet added to history? Find out when we added last time.
                // If more than 60 seconds, then add.
                if (current.setAddToHistory() == false) {
                    Iterator<VehicleActivityFlattened> iter = data.descendingIterator();
                    Instant compareto = current.getRecordTime().minusSeconds(60);

                    while (iter.hasNext()) {
                        VehicleActivityFlattened next = iter.next();

                        if (next.setAddToHistory()) {
                            if (next.getRecordTime().isBefore(compareto)) {
                                current.setAddToHistory(true);
                            }
                            break;
                        }
                    }

                }

                // Clean up any data older than 600 seconds.
                Instant compareto = current.getRecordTime().minusSeconds(600);
                Iterator<VehicleActivityFlattened> iter = data.iterator();

                while (iter.hasNext()) {
                    VehicleActivityFlattened next = iter.next();
                    
                    if (next.getRecordTime().isBefore(compareto)) {
                        iter.remove();
                    }
                    else {
                        // Safe to break from the loop now;
                        break;
                    }
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
