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
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import fi.ahto.example.traffic.data.contracts.internal.VehicleDataList;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
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
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Bean
    public GlobalKTable<String, VehicleDataList> constructLineDataTable(StreamsBuilder streamBuilder) {
        final JsonSerde<VehicleDataList> vaflistserde = new JsonSerde<>(VehicleDataList.class, objectMapper);
        LOG.debug("Constructing " + StaticData.LINE_STORE + " with StreamsBuilder");
        GlobalKTable<String, VehicleDataList> table
                = streamBuilder.globalTable(StaticData.LINE_STREAM,
                        Consumed.with(Serdes.String(), vaflistserde),
                        Materialized.<String, VehicleDataList, KeyValueStore<Bytes, byte[]>>as(StaticData.LINE_STORE));
        // Should be safe, build() just returns the internal Topology object, no side effects.
        Topology t = streamBuilder.build();
        // Here we should be able to add a processor sends the changelog data to MessageBroker.
        // Have to find out how to get the correct parentname. Check Topology.describe. An alternative
        // could be Topology.connectProcessorAndStateStores(processorname, statestorename).
        // t.addProcessor(name, supplier, parentNames);
        return table;
    }

    @Bean
    public GlobalKTable<String, VehicleActivity> constructVehicleDataTable(StreamsBuilder streamBuilder) {
        final JsonSerde<VehicleActivity> vaflistserde = new JsonSerde<>(VehicleActivity.class, objectMapper);
        LOG.debug("Constructing " + StaticData.VEHICLE_STORE + " with StreamsBuilder");
        GlobalKTable<String, VehicleActivity> table
                = streamBuilder.globalTable(StaticData.VEHICLE_STREAM,
                        Consumed.with(Serdes.String(), vaflistserde),
                        Materialized.<String, VehicleActivity, KeyValueStore<Bytes, byte[]>>as(StaticData.VEHICLE_STORE));
        return table;
    }

    @Bean
    public GlobalKTable<String, VehicleDataList> constructVehicleHistoryDataTable(StreamsBuilder streamBuilder) {
        final JsonSerde<VehicleDataList> vaflistserde = new JsonSerde<>(VehicleDataList.class, objectMapper);
        LOG.debug("Constructing " + StaticData.VEHICLE_HISTORY_STORE + " with StreamsBuilder");
        GlobalKTable<String, VehicleDataList> table
                = streamBuilder.globalTable(StaticData.VEHICLE_HISTORY_STREAM,
                        Consumed.with(Serdes.String(), vaflistserde),
                        Materialized.<String, VehicleDataList, KeyValueStore<Bytes, byte[]>>as(StaticData.VEHICLE_HISTORY_STORE));
        return table;
    }

    @Bean
    public GlobalKTable<String, RouteData> constructRouteDataTable(StreamsBuilder streamBuilder) {
        final JsonSerde<RouteData> stopserde = new JsonSerde<>(RouteData.class, objectMapper);
        LOG.debug("Constructing " + StaticData.ROUTE_STORE + " with StreamsBuilder");
        GlobalKTable<String, RouteData> table
                = streamBuilder.globalTable(StaticData.ROUTE_STREAM,
                        Consumed.with(Serdes.String(), stopserde),
                        Materialized.<String, RouteData, KeyValueStore<Bytes, byte[]>>as(StaticData.ROUTE_STORE));
        return table;
    }

    @Bean
    public GlobalKTable<String, StopData> constructStopDataTable(StreamsBuilder streamBuilder) {
        final JsonSerde<StopData> stopserde = new JsonSerde<>(StopData.class, objectMapper);
        LOG.debug("Constructing " + StaticData.STOP_STORE + " with StreamsBuilder");
        GlobalKTable<String, StopData> table
                = streamBuilder.globalTable(StaticData.STOP_STREAM,
                        Consumed.with(Serdes.String(), stopserde),
                        Materialized.<String, StopData, KeyValueStore<Bytes, byte[]>>as(StaticData.STOP_STORE));
        return table;
    }

    @Bean
    public GlobalKTable<String, ShapeSet> constructShapeDataTable(StreamsBuilder streamBuilder) {
        final JsonSerde<ShapeSet> shapeserde = new JsonSerde<>(ShapeSet.class, objectMapper);
        LOG.debug("Constructing " + StaticData.SHAPE_STORE + " with StreamsBuilder");
        GlobalKTable<String, ShapeSet> table
                = streamBuilder.globalTable(StaticData.SHAPE_STREAM,
                        Consumed.with(Serdes.String(), shapeserde),
                        Materialized.<String, ShapeSet, KeyValueStore<Bytes, byte[]>>as(StaticData.SHAPE_STORE));
        return table;
    }
}

