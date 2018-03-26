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
package fi.ahto.example.hsl.data.line.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.ahto.example.hsl.data.contracts.siri.VehicleActivityFlattened;
import fi.ahto.example.hsl.data.contracts.siri.VehicleDataList;
import fi.ahto.kafka.streams.state.utils.SimpleValueTransformerSupplierWithStore;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
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
import org.apache.kafka.streams.state.KeyValueStore;
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
public class LineTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(LineTransformer.class);
    private final JsonSerde<VehicleActivityFlattened> serdein = new JsonSerde<>(VehicleActivityFlattened.class);
    private static final JsonSerde<VehicleDataList> serdeout = new JsonSerde<>(VehicleDataList.class);

    @Autowired
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;
    
    @Autowired
    private ObjectMapper objectMapper;

    void VehicleActivityTransformers() {
        LOG.info("VehicleActivityTransformers created");
    }

    @Bean
    public KStream<String, VehicleActivityFlattened> kStream(StreamsBuilder builder) {
        LOG.info("Constructing stream from data-by-lineid to grouped-by-lineid");
        final JsonSerde<VehicleActivityFlattened> vafserde = new JsonSerde<>(VehicleActivityFlattened.class, objectMapper);
        final JsonSerde<VehicleDataList> vaflistserde = new JsonSerde<>(VehicleDataList.class, objectMapper);
        VehiclyActivityTransformer transformer = new VehiclyActivityTransformer(builder, Serdes.String(), vafserde, "vehicle-transformer");

        KStream<String, VehicleActivityFlattened> streamin = builder.stream("data-by-lineid", Consumed.with(Serdes.String(), vafserde));
        
        streamin = streamin.transformValues(transformer, "vehicle-transformer");

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
        Initializer<VehicleDataList> lineinitializer = new Initializer<VehicleDataList>() {
            @Override
            public VehicleDataList apply() {
                VehicleDataList valist = new VehicleDataList();
                List<VehicleActivityFlattened> list = new ArrayList<>();
                valist.setVehicleActivity(list);
                return valist;
            }
        };
        
        // Get a table of all vehicles currently operating on the line.
        Aggregator<String, VehicleActivityFlattened, VehicleDataList> lineaggregator =
                new Aggregator<String, VehicleActivityFlattened, VehicleDataList>() {
            @Override
            public VehicleDataList apply(String key, VehicleActivityFlattened value, VehicleDataList aggregate) {
                boolean remove = false;
                List<VehicleActivityFlattened> list = aggregate.getVehicleActivity();

                ListIterator<VehicleActivityFlattened> iter = list.listIterator();
                long time1 = value.getRecordTime().getEpochSecond();

                // Remove entries older than 90 seconds or value itself. Not a safe
                // way to detect when a vehicle has changed line or gone out of traffic.
                while (iter.hasNext()) {
                    VehicleActivityFlattened vaf = iter.next();
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

                list.add(value);
                return aggregate;
            }
        };
        
        KTable<String, VehicleDataList> linedata = streamin
                .groupByKey(Serialized.with(Serdes.String(), vafserde))
                .aggregate(lineinitializer, lineaggregator,
                    Materialized.<String, VehicleDataList, KeyValueStore<Bytes, byte[]>>as("line-aggregation-store")
                    .withKeySerde(Serdes.String())
                    .withValueSerde(vaflistserde)
                );
        
        linedata.toStream().to("data-by-lineid-enhanced", Produced.with(Serdes.String(), vaflistserde));

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

        return streamin;
    }

    class VehiclyActivityTransformer extends SimpleValueTransformerSupplierWithStore<String, VehicleActivityFlattened> {

        public VehiclyActivityTransformer(StreamsBuilder builder, Serde<String> keyserde, Serde<VehicleActivityFlattened> valserde, String storeName) {
            super(builder, keyserde, valserde, storeName);
        }

        @Override
        protected TransformerImpl createTransformer() {
            return new TransformerImpl() {
                @Override
                public VehicleActivityFlattened transform(VehicleActivityFlattened current) {
                    VehicleActivityFlattened previous = stateStore.get(current.getVehicleId());
                    VehicleActivityFlattened transformed = transform(previous, current);
                    stateStore.put(current.getVehicleId(), current);
                    return transformed;
                }

                @Override
                public VehicleActivityFlattened transform(VehicleActivityFlattened previous, VehicleActivityFlattened current) {
                    return transformer(previous, current);
                }

                private VehicleActivityFlattened transformer(VehicleActivityFlattened previous, VehicleActivityFlattened current) {
                    // Vehicle hasn't been on the line.
                    if (previous == null) {
                        return current;
                    }

                    // Vehicle has changed line, useless to calculate the change of delay.
                    if (current.getLineId().equals(previous.getLineId()) == false) {
                        return current;
                    }

                    // Change of direction, useless to calculate the change of delay.
                    if (current.getDirection().equals(previous.getDirection()) == false) {
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
}
