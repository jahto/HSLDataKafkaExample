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
package fi.ahto.example.traffic.data.database.feeder;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.ahto.example.traffic.data.contracts.internal.ServiceDataComplete;
import fi.ahto.example.traffic.data.database.repositories.mongo.RouteRepository;
import fi.ahto.example.traffic.data.database.repositories.mongo.ServiceDataRepository;
import fi.ahto.example.traffic.data.database.repositories.mongo.StopRepository;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

/**
 *
 * @author Jouni Ahto
 */
@Component
// @EnableJpaRepositories
@EnableMongoRepositories
public class DataFeeder {

    private static final Logger LOG = LoggerFactory.getLogger(DataFeeder.class);
    private static final AtomicLong counter = new AtomicLong(1);
    
    @Autowired
    @Qualifier("binary")
    private ObjectMapper smileMapper;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private ServiceDataRepository serviceRepository;
    
    @Bean
    public KStream<String, Route> kStream(StreamsBuilder builder) {
        final JsonSerde<Route> routeserde = new JsonSerde<>(Route.class, smileMapper);
        KStream<String, Route> routestream = builder.stream("dbqueue-route", Consumed.with(Serdes.String(), routeserde));
        routestream.foreach((key, value) -> handleRoute(key, value));

        final JsonSerde<Stop> stopserde = new JsonSerde<>(Stop.class, smileMapper);
        KStream<String, Stop> stopstream = builder.stream("dbqueue-stop", Consumed.with(Serdes.String(), stopserde));
        stopstream.foreach((key, value) -> handleStop(key, value));
        
        final JsonSerde<ServiceDataComplete> serviceserde = new JsonSerde<>(ServiceDataComplete.class, smileMapper);
        KStream<String, ServiceDataComplete> servicestream = builder.stream("dbqueue-services-complete", Consumed.with(Serdes.String(), serviceserde));
        servicestream.foreach((key, value) -> handleService(key, value));
        return routestream;
    }

    private void handleRoute(String key, Route rt) {
        try {
            routeRepository.save(rt);
        } catch (Exception e) {
            LOG.info("handleRoute", e);
        }
    }

    private void handleStop(String key, Stop rt) {
        try {
            stopRepository.save(rt);
        } catch (Exception e) {
            LOG.info("handleStop", e);
        }
    }
    
    private void handleService(String key, ServiceDataComplete rt) {
        try {
            serviceRepository.save(rt);
            if (counter.addAndGet(1) % 100 == 0) {
                LOG.info("Handled {} records", counter);
            }
        } catch (Exception e) {
            LOG.info("handleService", e);
        }
    }
}
