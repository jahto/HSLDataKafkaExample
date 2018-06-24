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
package fi.ahto.example.activemq.data.connector;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.ahto.example.traffic.data.contracts.internal.Arrivals;
import fi.ahto.example.traffic.data.contracts.internal.StopData;
import fi.ahto.example.traffic.data.contracts.internal.VehicleAtStop;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
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
public class QueueFeeder {
    private static final Logger LOG = LoggerFactory.getLogger(QueueFeeder.class);

    @Autowired
    private ObjectMapper objectMapper;
    
    @Bean
    public KStream<String, Arrivals> kStream(StreamsBuilder builder) {
        final JsonSerde<VehicleAtStop> vasserde = new JsonSerde<>(VehicleAtStop.class, objectMapper);
        final JsonSerde<StopData> stopserde = new JsonSerde<>(StopData.class, objectMapper);
        final JsonSerde<Arrivals> arrserde = new JsonSerde<>(Arrivals.class, objectMapper);
        
        KStream<String, Arrivals> arrivalsstream = builder.stream("vehicles-arriving-to-stop", Consumed.with(Serdes.String(), arrserde));
        arrivalsstream.foreach((key, value) -> handleArrivals(key, value));
        return arrivalsstream;
    }
    
    private void handleArrivals(String key, Arrivals arr) {
    }
}
