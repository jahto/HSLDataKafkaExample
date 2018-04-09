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

import fi.ahto.example.traffic.data.contracts.internal.VehicleDataList;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

/**
 *
 * @author Jouni Ahto
 */

@Service
// @DependsOn("TrafficDataStreamsListener")
public class TrafficDataStores {
    private static final Logger LOG = LoggerFactory.getLogger(TrafficDataStreamsListener.class);
    private static final String lineDataStore = "data-by-lineid-enhanced-store";
    private static final String vehicleDataStore = "vehicle-history-store";
    
    @Autowired
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;
    
    @Bean
    @Lazy(true)
    @DependsOn("constructLineDataTable")
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
                    ReadOnlyKeyValueStore<String, VehicleDataList> store = streams.store(lineDataStore, QueryableStoreTypes.keyValueStore());
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
    @DependsOn("constructVehicleDataTable")
    public ReadOnlyKeyValueStore<String, VehicleDataList> vehicleDataStore() {
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
                    ReadOnlyKeyValueStore<String, VehicleDataList> store = streams.store(vehicleDataStore, QueryableStoreTypes.keyValueStore());
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

