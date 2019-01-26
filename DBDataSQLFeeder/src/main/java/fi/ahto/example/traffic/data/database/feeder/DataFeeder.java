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
import fi.ahto.example.traffic.data.contracts.internal.StopData;
import fi.ahto.example.traffic.data.contracts.internal.TripData;
import fi.ahto.example.traffic.data.database.repositories.sql.SQLCalendarManagement;
import fi.ahto.example.traffic.data.database.repositories.sql.SQLRouteManagement;
import fi.ahto.example.traffic.data.database.repositories.sql.SQLStopManagement;
import fi.ahto.example.traffic.data.database.repositories.sql.SQLTripManagement;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

/**
 *
 * @author Jouni Ahto
 */
@Component
public class DataFeeder {

    private static final Logger LOG = LoggerFactory.getLogger(DataFeeder.class);
    private static ConcurrentHashMap<String, Long> routenums = new ConcurrentHashMap<>();
    private static final Object routelock = new Object();

    @Autowired
    @Qualifier("binary")
    private ObjectMapper smileMapper;

    @Autowired
    private SQLRouteManagement routeManagement;

    @Autowired
    private SQLStopManagement stopManagement;

    @Autowired
    private SQLCalendarManagement calendarManagement;

    @Autowired
    private SQLTripManagement tripManagement;

    @Bean
    public KStream<String, RouteData> kStream(StreamsBuilder builder) {
        final JsonSerde<RouteData> routeserde = new JsonSerde<>(RouteData.class, smileMapper);
        KStream<String, RouteData> routestream = builder.stream("dbqueue-route", Consumed.with(Serdes.String(), routeserde));
        routestream.foreach((key, value) -> routeManagement.handleRoute(key, value, true));

        final JsonSerde<StopData> stopserde = new JsonSerde<>(StopData.class, smileMapper);
        KStream<String, StopData> stopstream = builder.stream("dbqueue-stop", Consumed.with(Serdes.String(), stopserde));
        stopstream.foreach((key, value) -> stopManagement.handleStop(key, value, true));

        final JsonSerde<ServiceData> serviceserde = new JsonSerde<>(ServiceData.class, smileMapper);
        KStream<String, ServiceData> servicestream = builder.stream("dbqueue-services-complete", Consumed.with(Serdes.String(), serviceserde));
        servicestream.foreach((key, value) -> calendarManagement.handleService(key, value, true));

        final JsonSerde<TripData> tripserde = new JsonSerde<>(TripData.class, smileMapper);
        KStream<String, TripData> tripstream = builder.stream("dbqueue-trips-complete", Consumed.with(Serdes.String(), tripserde));
        tripstream.foreach((key, value) -> tripManagement.handleTrip(key, value));

        return routestream;
    }
}
