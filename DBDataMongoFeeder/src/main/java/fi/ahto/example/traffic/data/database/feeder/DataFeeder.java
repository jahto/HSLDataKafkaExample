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
import fi.ahto.example.traffic.data.contracts.internal.RouteData;
import fi.ahto.example.traffic.data.contracts.internal.ServiceData;
import fi.ahto.example.traffic.data.contracts.internal.ShapeSet;
import fi.ahto.example.traffic.data.contracts.internal.StopData;
import fi.ahto.example.traffic.data.contracts.internal.TripData;
import fi.ahto.example.traffic.data.database.repositories.mongo.RouteRepository;
import fi.ahto.example.traffic.data.database.repositories.mongo.ServiceDataRepository;
import fi.ahto.example.traffic.data.database.repositories.mongo.ShapeSetRepository;
import fi.ahto.example.traffic.data.database.repositories.mongo.StopRepository;
import fi.ahto.example.traffic.data.database.repositories.mongo.TripRepository;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
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
    private TripRepository tripRepository;

    @Autowired
    private ServiceDataRepository serviceRepository;
    
    @Autowired
    private ShapeSetRepository shapeSetRepository;
    
    @Bean
    public KStream<String, RouteData> kStream(StreamsBuilder builder) {
        final JsonSerde<RouteData> routeserde = new JsonSerde<>(RouteData.class, smileMapper);
        KStream<String, RouteData> routestream = builder.stream("dbqueue-route", Consumed.with(Serdes.String(), routeserde));
        routestream.foreach((key, value) -> handleRoute(key, value));

        final JsonSerde<StopData> stopserde = new JsonSerde<>(StopData.class, smileMapper);
        KStream<String, StopData> stopstream = builder.stream("dbqueue-stop", Consumed.with(Serdes.String(), stopserde));
        stopstream.foreach((key, value) -> handleStop(key, value));
        
        final JsonSerde<TripData> tripserde = new JsonSerde<>(TripData.class, smileMapper);
        KStream<String, TripData> tripstream = builder.stream("dbqueue-trips-complete", Consumed.with(Serdes.String(), tripserde));
        tripstream.foreach((key, value) -> handleTrip(key, value));
        
        final JsonSerde<ServiceData> serviceserde = new JsonSerde<>(ServiceData.class, smileMapper);
        KStream<String, ServiceData> servicestream = builder.stream("dbqueue-services-complete", Consumed.with(Serdes.String(), serviceserde));
        servicestream.foreach((key, value) -> handleService(key, value));

        /* Problem, find out how to solve.
java.lang.ClassCastException: org.bson.Document cannot be cast to java.base/java.util.Collection
	at org.springframework.data.mongodb.core.convert.MappingMongoConverter.writeInternal(MappingMongoConverter.java:492) ~[spring-data-mongodb-2.1.3.RELEASE.jar:2.1.3.RELEASE]        final JsonSerde<ShapeSet> shapeserde = new JsonSerde<>(ShapeSet.class, smileMapper);
        */
        final JsonSerde<ShapeSet> shapeserde = new JsonSerde<>(ShapeSet.class, smileMapper);
        KStream<String, ShapeSet> shapestream = builder.stream("dbqueue-shapes", Consumed.with(Serdes.String(), shapeserde));
        shapestream.foreach((key, value) -> handleShapeSet(key, value));

        return routestream;
    }

    private void handleShapeSet(String key, ShapeSet rt) {
        try {
            shapeSetRepository.save(rt);
        } catch (Exception e) {
            LOG.info("handleShapeSet", e);
        }
    }

    private void handleRoute(String key, RouteData rt) {
        try {
            routeRepository.save(rt);
        } catch (Exception e) {
            LOG.info("handleRoute", e);
        }
    }

    private void handleStop(String key, StopData rt) {
        try {
            stopRepository.save(rt);
        } catch (Exception e) {
            LOG.info("handleStop", e);
        }
    }
    
    private void handleTrip(String key, TripData rt) {
        try {
            tripRepository.save(rt);
        } catch (Exception e) {
            LOG.info("handleTrip", e);
        }
    }
    
    private void handleService(String key, ServiceData rt) {
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
