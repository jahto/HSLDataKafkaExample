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
package fi.ahto.example.hsl.data.web.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.ahto.kafkaspringdatacontracts.FakeTestMessage;
import fi.ahto.example.hsl.data.contracts.siri.VehicleDataList;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Service;

/**
 *
 * @author Jouni Ahto
 */

@Service
public class TrafficDataStreamsListener {
    private static final Logger LOG = LoggerFactory.getLogger(TrafficDataStreamsListener.class);
    private static final String lineDataStream = "data-by-lineid-enhanced";
    private static final String lineDataStore = "data-by-lineid-enhanced-store";
    private static final String vehicleDataStream = "vehicle-history";
    private static final String vehicleDataStore = "vehicle-history-store";
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Bean
    public GlobalKTable<String, VehicleDataList> constructLineDataTable(StreamsBuilder streamBuilder) {
        final JsonSerde<VehicleDataList> vaflistserde = new JsonSerde<>(VehicleDataList.class, objectMapper);
        LOG.info("Constructing " + lineDataStore + " with StreamsBuilder");
        GlobalKTable<String, VehicleDataList> table
                = streamBuilder.globalTable(lineDataStream,
                        Consumed.with(Serdes.String(), vaflistserde),
                        Materialized.<String, VehicleDataList, KeyValueStore<Bytes, byte[]>>as(lineDataStore));
        return table;
    }

    @Bean
    public GlobalKTable<String, VehicleDataList> constructVehicleDataTable(StreamsBuilder streamBuilder) {
        final JsonSerde<VehicleDataList> vaflistserde = new JsonSerde<>(VehicleDataList.class, objectMapper);
        LOG.info("Constructing " + vehicleDataStore + " with StreamsBuilder");
        GlobalKTable<String, VehicleDataList> table
                = streamBuilder.globalTable(vehicleDataStream,
                        Consumed.with(Serdes.String(), vaflistserde),
                        Materialized.<String, VehicleDataList, KeyValueStore<Bytes, byte[]>>as(vehicleDataStore));
        return table;
    }
}

