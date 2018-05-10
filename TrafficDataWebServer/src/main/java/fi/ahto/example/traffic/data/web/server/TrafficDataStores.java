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

import fi.ahto.example.traffic.data.contracts.internal.RouteData;
import fi.ahto.example.traffic.data.contracts.internal.ShapeSet;
import fi.ahto.example.traffic.data.contracts.internal.StopData;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import fi.ahto.example.traffic.data.contracts.internal.VehicleDataList;
import fi.ahto.example.traffic.data.contracts.internal.VehicleHistorySet;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

/**
 *
 * @author Jouni Ahto
 */

// TODO: Refactor this class, too much duplicate code.

@Service
// @DependsOn("TrafficDataStreamsListener")
public class TrafficDataStores {
    private static final Logger LOG = LoggerFactory.getLogger(TrafficDataStreamsListener.class);
    
    @Autowired
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;
    
    @Bean
    @Lazy(true)
    //@DependsOn("constructLineDataTable")
    public ReadOnlyKeyValueStore<String, VehicleDataList> lineDataStore() {
        LOG.debug("Constructing lineDataStore");

        while (true) {
            try {
                try {
                    KafkaStreams streams = streamsBuilderFactoryBean.getKafkaStreams();
                    if (streams == null) {
                        LOG.debug("Waiting for streams to be constructed and ready");
                        Thread.sleep(100);
                        continue;
                    }
                    ReadOnlyKeyValueStore<String, VehicleDataList> store = streams.store(StaticData.LINE_STORE, QueryableStoreTypes.keyValueStore());
                    LOG.debug("Store is now open for querying");
                    return store;
                } catch (InvalidStateStoreException ex) { // store not yet open for querying
                    LOG.debug("Waiting for store to open... " + ex.getMessage());
                    Thread.sleep(100);
                }
            } catch (InterruptedException ex) {
                continue;
            }
        }
    }
    
    @Bean
    @Lazy(true)
    //@DependsOn("constructVehicleDataTable")
    public ReadOnlyKeyValueStore<String, VehicleActivity> vehicleDataStore() {
        LOG.debug("Constructing vehicleDataStore");

        while (true) {
            try {
                try {
                    KafkaStreams streams = streamsBuilderFactoryBean.getKafkaStreams();
                    if (streams == null) {
                        LOG.debug("Waiting for streams to be constructed and ready");
                        Thread.sleep(100);
                        continue;
                    }
                    ReadOnlyKeyValueStore<String, VehicleActivity> store = streams.store(StaticData.VEHICLE_STORE, QueryableStoreTypes.keyValueStore());
                    LOG.debug("Store is now open for querying");
                    return store;
                } catch (InvalidStateStoreException ex) { // store not yet open for querying
                    LOG.debug("Waiting for store to open... " + ex.getMessage());
                    Thread.sleep(100);
                }
            } catch (InterruptedException ex) {
                continue;
            }
        }
    }

    @Bean
    @Lazy(true)
    //@DependsOn("constructVehicleDataTable")
    public ReadOnlyKeyValueStore<String, VehicleHistorySet> vehicleHistoryDataStore() {
        LOG.debug("Constructing vehicleHistoryDataStore");

        while (true) {
            try {
                try {
                    KafkaStreams streams = streamsBuilderFactoryBean.getKafkaStreams();
                    if (streams == null) {
                        LOG.debug("Waiting for streams to be constructed and ready");
                        Thread.sleep(100);
                        continue;
                    }
                    ReadOnlyKeyValueStore<String, VehicleHistorySet> store = streams.store(StaticData.VEHICLE_HISTORY_STORE, QueryableStoreTypes.keyValueStore());
                    LOG.debug("Store is now open for querying");
                    return store;
                } catch (InvalidStateStoreException ex) { // store not yet open for querying
                    LOG.debug("Waiting for store to open... " + ex.getMessage());
                    Thread.sleep(100);
                }
            } catch (InterruptedException ex) {
                continue;
            }
        }
    }

    @Bean
    @Lazy(true)
    //@DependsOn("constructRouteDataTable")
    public ReadOnlyKeyValueStore<String, RouteData> routeDataStore() {
        LOG.debug("Constructing routeDataStore");

        while (true) {
            try {
                try {
                    KafkaStreams streams = streamsBuilderFactoryBean.getKafkaStreams();
                    if (streams == null) {
                        LOG.debug("Waiting for streams to be constructed and ready");
                        Thread.sleep(100);
                        continue;
                    }
                    ReadOnlyKeyValueStore<String, RouteData> store = streams.store(StaticData.ROUTE_STORE, QueryableStoreTypes.keyValueStore());
                    LOG.debug("Store is now open for querying");
                    return store;
                } catch (InvalidStateStoreException ex) { // store not yet open for querying
                    LOG.debug("Waiting for store to open... " + ex.getMessage());
                    Thread.sleep(100);
                }
            } catch (InterruptedException ex) {
                continue;
            }
        }
    }

    @Bean
    @Lazy(true)
    //@DependsOn("constructStopDataTable")
    public ReadOnlyKeyValueStore<String, StopData> stopDataStore() {
        LOG.debug("Constructing stopDataStore");

        while (true) {
            try {
                try {
                    KafkaStreams streams = streamsBuilderFactoryBean.getKafkaStreams();
                    if (streams == null) {
                        LOG.debug("Waiting for streams to be constructed and ready");
                        Thread.sleep(100);
                        continue;
                    }
                    ReadOnlyKeyValueStore<String, StopData> store = streams.store(StaticData.STOP_STORE, QueryableStoreTypes.keyValueStore());
                    LOG.debug("Store is now open for querying");
                    return store;
                } catch (InvalidStateStoreException ex) { // store not yet open for querying
                    LOG.debug("Waiting for store to open... " + ex.getMessage());
                    Thread.sleep(100);
                }
            } catch (InterruptedException ex) {
                continue;
            }
        }
    }

    @Bean
    @Lazy(true)
    //@DependsOn("constructShapeDataTable")
    public ReadOnlyKeyValueStore<String, ShapeSet> shapeDataStore() {
        LOG.debug("Constructing shapeDataStore");

        while (true) {
            try {
                try {
                    KafkaStreams streams = streamsBuilderFactoryBean.getKafkaStreams();
                    if (streams == null) {
                        LOG.debug("Waiting for streams to be constructed and ready");
                        Thread.sleep(100);
                        continue;
                    }
                    ReadOnlyKeyValueStore<String, ShapeSet> store = streams.store(StaticData.SHAPE_STORE, QueryableStoreTypes.keyValueStore());
                    LOG.debug("Store is now open for querying");
                    return store;
                } catch (InvalidStateStoreException ex) { // store not yet open for querying
                    LOG.debug("Waiting for store to open... " + ex.getMessage());
                    Thread.sleep(100);
                }
            } catch (InterruptedException ex) {
                continue;
            }
        }
    }
}

