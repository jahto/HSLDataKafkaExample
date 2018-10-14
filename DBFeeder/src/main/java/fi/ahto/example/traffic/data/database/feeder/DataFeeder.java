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

import fi.ahto.example.traffic.data.database.repositories.sql.SQLRouteRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.ahto.example.traffic.data.contracts.database.sql.DBRouteSQLImpl;
import fi.ahto.example.traffic.data.contracts.internal.Arrivals;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import fi.ahto.example.traffic.data.contracts.internal.VehicleDataList;
import fi.ahto.example.traffic.data.contracts.internal.VehicleHistorySet;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.nustaq.serialization.FSTConfiguration;
import org.onebusaway.gtfs.model.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;
import fi.ahto.example.traffic.data.contracts.database.DBRoute;

/**
 *
 * @author Jouni Ahto
 */
@Component
public class DataFeeder {

    private static final Logger LOG = LoggerFactory.getLogger(DataFeeder.class);

    @Autowired
    @Qualifier("binary")
    private ObjectMapper smileMapper;

    @Autowired
    private SQLRouteRepository routeRepository;

    @Bean
    public KStream<String, Route> kStream(StreamsBuilder builder) {
        final JsonSerde<Route> routeserde = new JsonSerde<>(Route.class, smileMapper);
        KStream<String, Route> routestream = builder.stream("dbqueue-route", Consumed.with(Serdes.String(), routeserde));
        routestream.foreach((key, value) -> handleRoute(key, value));

        return routestream;
    }

    private void handleRoute(String key, Route rt) {
        DBRoute dbrt = new DBRouteSQLImpl(key, rt);
        routeRepository.save(dbrt);
        int i = 0;
    }
}
