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
package fi.ahto.kafkaspringstreamtransformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.ahto.kafka.streams.state.utils.SimpleTransformerSupplierWithStore;
import fi.ahto.kafka.streams.state.utils.TransformerSupplierWithStore;
import fi.ahto.kafkaspringdatacontracts.siri.VehicleActivityFlattened;
import fi.ahto.kafkaspringdatacontracts.siri.VehicleDataList;
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
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

/**
 *
 * @author Jouni Ahto
 */
@Component
public class VehicleActivityTransformers {

    private static final Logger log = LoggerFactory.getLogger(VehicleActivityTransformers.class);
    private final JsonSerde<VehicleActivityFlattened> serdein = new JsonSerde<>(VehicleActivityFlattened.class);
    private static final JsonSerde<VehicleDataList> serdeout = new JsonSerde<>(VehicleDataList.class);

    @Autowired
    private ObjectMapper objectMapper;

    void VehicleActivityTransformers() {
        log.info("VehicleActivityTransformers created");
    }

    private static final class WithPreviousTransformerSupplier
            // private static final class WithPreviousTransformerSupplier<K>
            implements TransformerSupplier<String, VehicleActivityFlattened, KeyValue<String, VehicleActivityFlattened>> {

        final private String stateStoreName;
        // final private Serdes serde;

        public WithPreviousTransformerSupplier(String stateStoreName) {
            this.stateStoreName = stateStoreName;
        }

        @Override
        public Transformer<String, VehicleActivityFlattened, KeyValue<String, VehicleActivityFlattened>> get() {
            return new Transformer<String, VehicleActivityFlattened, KeyValue<String, VehicleActivityFlattened>>() {
                private KeyValueStore<String, VehicleActivityFlattened> stateStore;

                @Override
                public void init(ProcessorContext pc) {
                    stateStore = (KeyValueStore<String, VehicleActivityFlattened>) pc.getStateStore(stateStoreName);
                }

                @Override
                public KeyValue<String, VehicleActivityFlattened> transform(String k, VehicleActivityFlattened v) {
                    // VehicleActivityFlattened val = stateStore.get(k);
                    VehicleActivityFlattened val = stateStore.get(v.getVehicleId());
                    VehicleActivityFlattened newVal = transform(k, val, v);
                    // stateStore.put(k, newVal);
                    stateStore.put(v.getVehicleId(), newVal);
                    // return KeyValue.pair(k, val);
                    return KeyValue.pair(k, newVal);
                }

                public VehicleActivityFlattened transform(String k, VehicleActivityFlattened oldVal, VehicleActivityFlattened newVal) {
                    // Vehicle hasn't been on the line.
                    if (oldVal == null) {
                        return newVal;
                    }

                    // Vehicle has changed line.
                    if (oldVal.getLineId().equals(oldVal.getLineId()) == false) {
                        return newVal;
                    }

                    // Change of direction, useless to calculate the change of delay.
                    if (newVal.getDirection().equals(oldVal.getDirection()) == false) {
                        return newVal;
                    }

                    // Vehicle hasn't been on the line long enough to calculate whether the delay is going up- or downwards.
                    if (newVal.getRecordTime().minusSeconds(55).compareTo(oldVal.getRecordTime()) < 0) {
                        return newVal;
                    }

                    if (newVal.getDelay() != null && oldVal.getDelay() != null) {
                        Integer delaychange = newVal.getDelay() - oldVal.getDelay();
                        // Integer delaytime = (int) (newVal.getRecordTime().getEpochSecond() - oldVal.getRecordTime().getEpochSecond());
                        Long delaytime = (newVal.getRecordTime().getEpochSecond() - oldVal.getRecordTime().getEpochSecond());
                        int i = 0;
                    }

                    return newVal;
                }

                @Override
                public KeyValue<String, VehicleActivityFlattened> punctuate(long l) {
                    // Not needed and also deprecated.
                    return null;
                }

                @Override
                public void close() {
                    // Note: The store should NOT be closed manually here via `stateStore.close()`!
                    // The Kafka Streams API will automatically close stores when necessary.
                }
            };
        }
    }

    // Work in progress to convert this to an abstract template class.
    public abstract class TestTemplate<K, V>
            implements TransformerSupplier<K, V, KeyValue<K, V>> {

        final private String stateStoreName;
        final private StoreBuilder<KeyValueStore<K, V>> stateStore;
        TransformerImpl transformer;

        public TestTemplate(StreamsBuilder builder, Serde<K> keyserde, Serde<V> valserde, String stateStoreName) {
            this.stateStoreName = stateStoreName;
            StoreBuilder<KeyValueStore<K, V>> store = Stores.keyValueStoreBuilder(
                    Stores.persistentKeyValueStore(stateStoreName),
                    keyserde,
                    valserde)
                    .withCachingEnabled();

            builder.addStateStore(store);
            this.stateStore = store;
            this.transformer = createTransformer();
        }

        public abstract TransformerImpl createTransformer();

        @Override
        public Transformer<K, V, KeyValue<K, V>> get() {
            return transformer;
        }

        public abstract class TransformerImpl implements Transformer<K, V, KeyValue<K, V>> {
            protected KeyValueStore<K, V> stateStore;

            @Override
            public void init(ProcessorContext pc) {
                stateStore = (KeyValueStore<K, V>) pc.getStateStore(stateStoreName);
            }

            @Override
            public abstract KeyValue<K, V> transform(K k, V v);

            public abstract KeyValue<K, V> transform(K k, V v1, V v2);

            @Override
            public KeyValue<K, V> punctuate(long l) {
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

    @Bean
    public KStream<String, VehicleActivityFlattened> kStream(StreamsBuilder builder) {
        log.info("Constructing stream from data-by-lineid to grouped-by-lineid");
        final JsonSerde<VehicleActivityFlattened> serdeinfinal = new JsonSerde<>(VehicleActivityFlattened.class, objectMapper);

        // Create a state store manually.
        StoreBuilder<KeyValueStore<String, VehicleActivityFlattened>> vehicleStore = Stores.keyValueStoreBuilder(
                Stores.persistentKeyValueStore("vehicleStore"),
                Serdes.String(),
                serdeinfinal)
                .withCachingEnabled();

        // Important (1 of 2): You must add the state store to the topology, otherwise your application
        // will fail at run-time (because the state store is referred to in `transform()` below.
        builder.addStateStore(vehicleStore);

        // When something happens on the line.
        final KStream<String, VehicleActivityFlattened> streamin = builder.stream("data-by-lineid", Consumed.with(Serdes.String(), serdeinfinal));

        // Get the previous value of vehicle from store (if it exists).
        // Handle data here...
        // Must use Processor API. We need to store the current value of the vehicle to be able
        // to refer to it later. This is exactly what this transformer does.
        // Finally, construct and return a KStream<String, List<VehicleActivityFlattened>> ?
        KStream<String, VehicleActivityFlattened> streamtransformed
                = streamin.transform(new WithPreviousTransformerSupplier(vehicleStore.name()), vehicleStore.name());

        // Just testing generic inner classes. Not easy.
        
        class RealTemplate extends TestTemplate<String, VehicleActivityFlattened> {

            public RealTemplate(StreamsBuilder builder, Serde<String> keyserde, Serde<VehicleActivityFlattened> valserde, String stateStoreName) {
                super(builder, keyserde, valserde, stateStoreName);
            }

            @Override
            public TransformerImpl createTransformer() {
                return new TransformerImpl() {
                    @Override
                    public KeyValue<String, VehicleActivityFlattened> transform(String k, VehicleActivityFlattened v) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }

                    @Override
                    public KeyValue<String, VehicleActivityFlattened> transform(String k, VehicleActivityFlattened v1, VehicleActivityFlattened v2) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                };
            }
            
        }
        RealTemplate RealClass = new RealTemplate(builder, Serdes.String(), serdein, "foobarstore");
        // KStream<String, VehicleActivityFlattened> streamtest = streamin.transform(RealClass, "foobarstore");
        
        
        SimpleTransformerSupplierWithStore<String, VehicleActivityFlattened> OtherRealTransformer =
                new SimpleTransformerSupplierWithStore<String, VehicleActivityFlattened>(builder, Serdes.String(), serdein, "foobarbaz") {
            @Override
            public TransformerImpl createTransformer() {
                return new TransformerImpl("foobarbaz") {
                    @Override
                    public Object transformValue(Object oldVal, Object v) {
                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                    }
                };
            }
        };
        KStream<String, VehicleActivityFlattened> otherstreamtest = streamin.transform(OtherRealTransformer, "foobarbaz");
        // return streamtransformed;
        return otherstreamtest;
    }

    private VehicleActivityFlattened MapperFunc(VehicleActivityFlattened left, VehicleActivityFlattened right) {
        // Vehicle hasn't been on the line (within KTables retention period)
        if (right == null) {
            return left;
        }

        // Vehicle has changed line.
        if (left.getLineId().equals(left.getLineId()) == false) {
            return left;
        }

        // Change of direction, useless to calculate the change of delay.
        if (right.getDirection().equals(left.getDirection()) == false) {
            return left;
        }

        // Vehicle hasn't been on the line long enough to calculate whether the delay is going up- or downwards.
        if (right.getRecordTime().minusSeconds(65).compareTo(left.getRecordTime()) < 0) {
            return left;
        }

        Integer delaychange = right.getDelay() - left.getDelay();

        return left;
    }
}
