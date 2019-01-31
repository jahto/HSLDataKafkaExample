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
import com.github.jahto.utils.CommonFSTConfiguration;
import com.github.jahto.utils.FSTSerde;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import fi.ahto.example.traffic.data.contracts.internal.VehicleHistoryRecord;
import fi.ahto.example.traffic.data.contracts.internal.VehicleHistorySet;
import java.time.format.DateTimeFormatter;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
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
        final FSTConfiguration conf = CommonFSTConfiguration.getCommonFSTConfiguration();
        final FSTSerde<VehicleActivity> fstvaserde = new FSTSerde<>(VehicleActivity.class, conf);
        final FSTSerde<VehicleHistorySet> fstvhsetserde = new FSTSerde<>(VehicleHistorySet.class, conf);

        final JsonSerde<VehicleActivity> vaserde = new JsonSerde<>(VehicleActivity.class, objectMapper);
        final JsonSerde<VehicleHistorySet> vhsetserde = new JsonSerde<>(VehicleHistorySet.class, objectMapper);

        KStream<String, VehicleActivity> streamin = builder.stream("data-by-vehicleid", Consumed.with(Serdes.String(), fstvaserde));
        VehicleTransformerSupplier transformer = new VehicleTransformerSupplier(builder, Serdes.String(), fstvaserde, "vehicle-transformer-extended");

        KStream<String, VehicleActivity> transformed = streamin.transform(transformer, "vehicle-transformer-extended");

        // Collect a rough history per vehicle and day.
        KStream<String, VehicleActivity> tohistory
                = transformed.filter((key, value) -> value != null && value.AddToHistory());

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
                    LOG.debug("Aggregating vehicle {}", key);
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
                .groupByKey(Serialized.with(Serdes.String(), vaserde))
                // .groupByKey(Serialized.with(Serdes.String(), fstvaserde))
                .aggregate(vehicleinitializer, vehicleaggregator,
                        Materialized.<String, VehicleHistorySet, KeyValueStore<Bytes, byte[]>>as("vehicle-aggregation-store")
                                .withKeySerde(Serdes.String())
                                // .withValueSerde(vhsetserde)
                                .withValueSerde(fstvhsetserde)
                );

        vehiclehistory.toStream().to("vehicle-history", Produced.with(Serdes.String(), fstvhsetserde));

        transformed.to("vehicles", Produced.with(Serdes.String(), fstvaserde));

        KStream<String, VehicleActivity> tolines
                = transformed
                        .filter((key, value) -> value != null)
                        .map((key, value)
                                -> KeyValue.pair(value.getInternalLineId(), value));

        // tolines.to("data-by-lineid", Produced.with(Serdes.String(), vaserde));
        tolines.to("data-by-lineid", Produced.with(Serdes.String(), fstvaserde));

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
}
