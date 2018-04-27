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
package fi.ahto.example.traffic.data.web.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.ahto.example.traffic.data.contracts.internal.RouteData;
import fi.ahto.example.traffic.data.contracts.internal.ShapeSet;
import fi.ahto.example.traffic.data.contracts.internal.StopData;
import fi.ahto.example.traffic.data.contracts.internal.VehicleDataList;
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
    private static final String routeDataStream = "routes";
    private static final String routeDataStore = "routes-store";
    private static final String stopDataStream = "stops";
    private static final String stopDataStore = "stops-store";
    private static final String shapeDataStream = "shapes";
    private static final String shapeDataStore = "shapes-store";
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Bean
    public GlobalKTable<String, VehicleDataList> constructLineDataTable(StreamsBuilder streamBuilder) {
        final JsonSerde<VehicleDataList> vaflistserde = new JsonSerde<>(VehicleDataList.class, objectMapper);
        LOG.debug("Constructing " + lineDataStore + " with StreamsBuilder");
        GlobalKTable<String, VehicleDataList> table
                = streamBuilder.globalTable(lineDataStream,
                        Consumed.with(Serdes.String(), vaflistserde),
                        Materialized.<String, VehicleDataList, KeyValueStore<Bytes, byte[]>>as(lineDataStore));
        return table;
    }

    @Bean
    public GlobalKTable<String, VehicleDataList> constructVehicleDataTable(StreamsBuilder streamBuilder) {
        final JsonSerde<VehicleDataList> vaflistserde = new JsonSerde<>(VehicleDataList.class, objectMapper);
        LOG.debug("Constructing " + vehicleDataStore + " with StreamsBuilder");
        GlobalKTable<String, VehicleDataList> table
                = streamBuilder.globalTable(vehicleDataStream,
                        Consumed.with(Serdes.String(), vaflistserde),
                        Materialized.<String, VehicleDataList, KeyValueStore<Bytes, byte[]>>as(vehicleDataStore));
        return table;
    }

    @Bean
    public GlobalKTable<String, RouteData> constructRouteDataTable(StreamsBuilder streamBuilder) {
        final JsonSerde<RouteData> stopserde = new JsonSerde<>(RouteData.class, objectMapper);
        LOG.debug("Constructing " + routeDataStore + " with StreamsBuilder");
        GlobalKTable<String, RouteData> table
                = streamBuilder.globalTable(routeDataStream,
                        Consumed.with(Serdes.String(), stopserde),
                        Materialized.<String, RouteData, KeyValueStore<Bytes, byte[]>>as(routeDataStore));
        return table;
    }

    @Bean
    public GlobalKTable<String, StopData> constructStopDataTable(StreamsBuilder streamBuilder) {
        final JsonSerde<StopData> stopserde = new JsonSerde<>(StopData.class, objectMapper);
        LOG.debug("Constructing " + stopDataStore + " with StreamsBuilder");
        GlobalKTable<String, StopData> table
                = streamBuilder.globalTable(stopDataStream,
                        Consumed.with(Serdes.String(), stopserde),
                        Materialized.<String, StopData, KeyValueStore<Bytes, byte[]>>as(stopDataStore));
        return table;
    }

    @Bean
    public GlobalKTable<String, ShapeSet> constructShapeDataTable(StreamsBuilder streamBuilder) {
        final JsonSerde<ShapeSet> shapeserde = new JsonSerde<>(ShapeSet.class, objectMapper);
        LOG.debug("Constructing " + shapeDataStore + " with StreamsBuilder");
        GlobalKTable<String, ShapeSet> table
                = streamBuilder.globalTable(shapeDataStream,
                        Consumed.with(Serdes.String(), shapeserde),
                        Materialized.<String, ShapeSet, KeyValueStore<Bytes, byte[]>>as(shapeDataStore));
        return table;
    }
}

