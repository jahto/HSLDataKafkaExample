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
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivityFlattened;
import fi.ahto.example.traffic.data.contracts.internal.VehicleDataList;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.Consumed;
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
    public KStream<String, VehicleActivityFlattened> kStream(StreamsBuilder builder) {
        LOG.debug("Constructing stream from data-by-lineid");
        final JsonSerde<VehicleActivityFlattened> vafserde = new JsonSerde<>(VehicleActivityFlattened.class, objectMapper);
        final JsonSerde<VehicleDataList> vaflistserde = new JsonSerde<>(VehicleDataList.class, objectMapper);
        KStream<String, VehicleActivityFlattened> streamin = builder.stream("data-by-lineid", Consumed.with(Serdes.String(), vafserde));

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
                // LOG.debug("Aggregating line " + key);
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

                if (value.LineHasChanged() == false) {
                    list.add(value);
                } else {
                    LOG.debug("Removed vehicle " + value.getVehicleId() + " from line " + key);
                }
                return aggregate;
            }
        };
                
        KTable<String, VehicleDataList> lines = streamin
                .groupByKey(Serialized.with(Serdes.String(), vafserde))
                .aggregate(lineinitializer, lineaggregator,
                    Materialized.<String, VehicleDataList, KeyValueStore<Bytes, byte[]>>as("line-aggregation-store")
                    .withKeySerde(Serdes.String())
                    .withValueSerde(vaflistserde)
                );

        lines.toStream().to("data-by-lineid-enhanced", Produced.with(Serdes.String(), vaflistserde));
        return streamin;
    }
}
