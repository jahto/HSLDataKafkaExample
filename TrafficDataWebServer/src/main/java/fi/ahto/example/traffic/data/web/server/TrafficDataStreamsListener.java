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
import com.github.jahto.utils.FSTSerde;
import fi.ahto.example.traffic.data.contracts.internal.Arrivals;
import fi.ahto.example.traffic.data.contracts.internal.RouteData;
import fi.ahto.example.traffic.data.contracts.internal.ShapeSet;
import fi.ahto.example.traffic.data.contracts.internal.StopData;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import fi.ahto.example.traffic.data.contracts.internal.VehicleDataList;
import fi.ahto.example.traffic.data.contracts.internal.VehicleHistorySet;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.nustaq.serialization.FSTConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Qualifier( "json")
    private ObjectMapper objectMapper;
    
    @Autowired
    @Qualifier( "binary")
    private ObjectMapper smileMapper;

    @Autowired
    private FSTConfiguration conf;

    @Bean
    public GlobalKTable<String, VehicleDataList> constructLineDataTable(StreamsBuilder streamBuilder) {
        // final JsonSerde<VehicleDataList> serde = new JsonSerde<>(VehicleDataList.class, smileMapper);
        final FSTSerde<VehicleDataList> serde = new FSTSerde<>(VehicleDataList.class, conf);
        LOG.debug("Constructing " + StaticData.LINE_STORE + " with StreamsBuilder");
        GlobalKTable<String, VehicleDataList> table
                = streamBuilder.globalTable(StaticData.LINE_STREAM,
                        Consumed.with(Serdes.String(), serde),
                        Materialized.<String, VehicleDataList, KeyValueStore<Bytes, byte[]>>as(StaticData.LINE_STORE));
        return table;
    }

    @Bean
    public GlobalKTable<String, VehicleActivity> constructVehicleDataTable(StreamsBuilder streamBuilder) {
        // final JsonSerde<VehicleActivity> serde = new JsonSerde<>(VehicleActivity.class, smileMapper);
        final FSTSerde<VehicleActivity> serde = new FSTSerde<>(VehicleActivity.class, conf);
        LOG.debug("Constructing " + StaticData.VEHICLE_STORE + " with StreamsBuilder");
        GlobalKTable<String, VehicleActivity> table
                = streamBuilder.globalTable(StaticData.VEHICLE_STREAM,
                        Consumed.with(Serdes.String(), serde),
                        Materialized.<String, VehicleActivity, KeyValueStore<Bytes, byte[]>>as(StaticData.VEHICLE_STORE));
        return table;
    }

    @Bean
    public GlobalKTable<String, VehicleHistorySet> constructVehicleHistoryDataTable(StreamsBuilder streamBuilder) {
        // final JsonSerde<VehicleHistorySet> serde = new JsonSerde<>(VehicleHistorySet.class, smileMapper);
        final FSTSerde<VehicleHistorySet> serde = new FSTSerde<>(VehicleHistorySet.class, conf);
        LOG.debug("Constructing " + StaticData.VEHICLE_HISTORY_STORE + " with StreamsBuilder");
        GlobalKTable<String, VehicleHistorySet> table
                = streamBuilder.globalTable(StaticData.VEHICLE_HISTORY_STREAM,
                        Consumed.with(Serdes.String(), serde),
                        Materialized.<String, VehicleHistorySet, KeyValueStore<Bytes, byte[]>>as(StaticData.VEHICLE_HISTORY_STORE));
        return table;
    }

    @Bean
    public GlobalKTable<String, RouteData> constructRouteDataTable(StreamsBuilder streamBuilder) {
        final JsonSerde<RouteData> serde = new JsonSerde<>(RouteData.class, smileMapper);
        LOG.debug("Constructing " + StaticData.ROUTE_STORE + " with StreamsBuilder");
        GlobalKTable<String, RouteData> table
                = streamBuilder.globalTable(StaticData.ROUTE_STREAM,
                        Consumed.with(Serdes.String(), serde),
                        Materialized.<String, RouteData, KeyValueStore<Bytes, byte[]>>as(StaticData.ROUTE_STORE));
        return table;
    }

    @Bean
    public GlobalKTable<String, StopData> constructStopDataTable(StreamsBuilder streamBuilder) {
        final JsonSerde<StopData> serde = new JsonSerde<>(StopData.class, smileMapper);
        LOG.debug("Constructing " + StaticData.STOP_STORE + " with StreamsBuilder");
        GlobalKTable<String, StopData> table
                = streamBuilder.globalTable(StaticData.STOP_STREAM,
                        Consumed.with(Serdes.String(), serde),
                        Materialized.<String, StopData, KeyValueStore<Bytes, byte[]>>as(StaticData.STOP_STORE));
        return table;
    }

    @Bean
    public GlobalKTable<String, ShapeSet> constructShapeDataTable(StreamsBuilder streamBuilder) {
        final JsonSerde<ShapeSet> serde = new JsonSerde<>(ShapeSet.class, smileMapper);
        LOG.debug("Constructing " + StaticData.SHAPE_STORE + " with StreamsBuilder");
        GlobalKTable<String, ShapeSet> table
                = streamBuilder.globalTable(StaticData.SHAPE_STREAM,
                        Consumed.with(Serdes.String(), serde),
                        Materialized.<String, ShapeSet, KeyValueStore<Bytes, byte[]>>as(StaticData.SHAPE_STORE));
        return table;
    }

    @Bean
    public GlobalKTable<String, Arrivals> constructStopChangesDataTable(StreamsBuilder streamBuilder) {
        //final JsonSerde<Arrivals> serde = new JsonSerde<>(Arrivals.class, smileMapper);
        final FSTSerde<Arrivals> serde = new FSTSerde<>(Arrivals.class, conf);
        LOG.debug("Constructing " + StaticData.STOP_CHANGES_STORE + " with StreamsBuilder");
        GlobalKTable<String, Arrivals> table
                = streamBuilder.globalTable(StaticData.STOP_CHANGES_STREAM,
                        Consumed.with(Serdes.String(), serde),
                        Materialized.<String, Arrivals, KeyValueStore<Bytes, byte[]>>as(StaticData.STOP_CHANGES_STORE));
        return table;
    }
}
