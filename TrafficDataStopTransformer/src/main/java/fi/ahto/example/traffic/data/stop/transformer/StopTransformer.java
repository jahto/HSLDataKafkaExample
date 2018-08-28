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
package fi.ahto.example.traffic.data.stop.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jahto.utils.FSTSerde;
import fi.ahto.example.traffic.data.contracts.internal.Arrivals;
import fi.ahto.example.traffic.data.contracts.internal.StopData;
import fi.ahto.example.traffic.data.contracts.internal.VehicleAtStop;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Aggregator;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
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
public class StopTransformer {

    private static final Logger LOG = LoggerFactory.getLogger(StopTransformer.class);

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private FSTConfiguration conf;

    @Bean
    public KStream<String, VehicleAtStop> kStream(StreamsBuilder builder) {
        LOG.info("Constructing stream from changes-by-stopid");
        final FSTSerde<VehicleAtStop> vasserde = new FSTSerde<>(VehicleAtStop.class, conf);
        final JsonSerde<StopData> stopserde = new JsonSerde<>(StopData.class, objectMapper);
        final FSTSerde<Arrivals> arrserde = new FSTSerde<>(Arrivals.class, conf);
        
        KStream<String, VehicleAtStop> streamin = builder.stream("changes-by-stopid", Consumed.with(Serdes.String(), vasserde));

        GlobalKTable<String, StopData> stops
                = builder.globalTable("stops",
                        Consumed.with(Serdes.String(), stopserde),
                        Materialized.<String, StopData, KeyValueStore<Bytes, byte[]>>as("stops"));
        
        Initializer<Arrivals> arrivalsinitializer = () -> {
            Arrivals alist = new Arrivals();
            return alist;
        };

        Aggregator<String, VehicleAtStop, Arrivals> arrivalsaggregator
                = (String key, VehicleAtStop value, Arrivals aggregate) -> {
                    // LOG.info("Aggregating stop " + key);
                    return adjustStopTimes(key, value, aggregate);
                };

        KTable<String, Arrivals> vehiclesarriving = streamin
                .groupByKey(Serialized.with(Serdes.String(), vasserde))
                .aggregate(arrivalsinitializer, arrivalsaggregator,
                        Materialized.<String, Arrivals, KeyValueStore<Bytes, byte[]>>as("arrivals-aggregation-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(arrserde)
                );

        vehiclesarriving.toStream().to("vehicles-arriving-to-stop", Produced.with(Serdes.String(), arrserde));
        return streamin;
    }
    
    Arrivals adjustStopTimes(String key, VehicleAtStop vas, Arrivals agg) {
        if (vas.remove) {
            LOG.info("Removing vehicle {} from stop {} at {}", vas.vehicleId, key, vas.arrivalTime);
            agg.remove(vas.vehicleId);
        }
        else {
            LOG.info("Adding vehicle {} to stop {} at {}", vas.vehicleId, key, vas.arrivalTime);
            agg.put(vas.vehicleId, vas);
        }
        return agg;
    }
}
