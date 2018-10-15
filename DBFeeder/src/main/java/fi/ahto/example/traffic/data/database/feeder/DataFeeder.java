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
import fi.ahto.example.traffic.data.contracts.database.DBCalendar;
import fi.ahto.example.traffic.data.contracts.database.DBCalendarDate;
import fi.ahto.example.traffic.data.contracts.database.DBFrequency;
import fi.ahto.example.traffic.data.contracts.database.DBRoute;
import fi.ahto.example.traffic.data.contracts.database.DBStop;
import fi.ahto.example.traffic.data.contracts.database.DBStopTime;
import fi.ahto.example.traffic.data.contracts.database.DBTrip;
import fi.ahto.example.traffic.data.contracts.database.sql.DBCalendarDateSQLImpl;
import fi.ahto.example.traffic.data.contracts.database.sql.DBCalendarSQLImpl;
import fi.ahto.example.traffic.data.contracts.database.sql.DBFrequencySQLImpl;
import fi.ahto.example.traffic.data.contracts.database.sql.DBRouteSQLImpl;
import fi.ahto.example.traffic.data.contracts.database.sql.DBStopSQLImpl;
import fi.ahto.example.traffic.data.contracts.database.sql.DBStopTimeSQLImpl;
import fi.ahto.example.traffic.data.contracts.database.sql.DBTripSQLImpl;
import fi.ahto.example.traffic.data.database.repositories.sql.SQLCalendarDateRepository;
import fi.ahto.example.traffic.data.database.repositories.sql.SQLCalendarRepository;
import fi.ahto.example.traffic.data.database.repositories.sql.SQLFrequencyRepository;
import fi.ahto.example.traffic.data.database.repositories.sql.SQLRouteRepository;
import fi.ahto.example.traffic.data.database.repositories.sql.SQLStopRepository;
import fi.ahto.example.traffic.data.database.repositories.sql.SQLStopTimeRepository;
import fi.ahto.example.traffic.data.database.repositories.sql.SQLTripRepository;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

/**
 *
 * @author Jouni Ahto
 */
@Component
@EnableJpaRepositories
public class DataFeeder {

    private static final Logger LOG = LoggerFactory.getLogger(DataFeeder.class);

    @Autowired
    @Qualifier("binary")
    private ObjectMapper smileMapper;

    @Autowired
    private SQLRouteRepository routeRepository;

    @Autowired
    private SQLCalendarRepository calendarRepository;

    @Autowired
    private SQLCalendarDateRepository calendarDateRepository;

    @Autowired
    private SQLFrequencyRepository frequencyRepository;

    @Autowired
    private SQLStopRepository stopRepository;

    @Autowired
    private SQLStopTimeRepository stopTimeRepository;

    @Autowired
    private SQLTripRepository tripRepository;

    @Bean
    public KStream<String, Route> kStream(StreamsBuilder builder) {
        final JsonSerde<Route> routeserde = new JsonSerde<>(Route.class, smileMapper);
        KStream<String, Route> routestream = builder.stream("dbqueue-route", Consumed.with(Serdes.String(), routeserde));
        routestream.foreach((key, value) -> handleRoute(key, value));

        final JsonSerde<ServiceCalendar> calendarserde = new JsonSerde<>(ServiceCalendar.class, smileMapper);
        KStream<String, ServiceCalendar> calendarstream = builder.stream("dbqueue-calendar", Consumed.with(Serdes.String(), calendarserde));
        calendarstream.foreach((key, value) -> handleCalendar(key, value));

        final JsonSerde<ServiceCalendarDate> calendardateserde = new JsonSerde<>(ServiceCalendarDate.class, smileMapper);
        KStream<String, ServiceCalendarDate> calendardatestream = builder.stream("dbqueue-calendardate", Consumed.with(Serdes.String(), calendardateserde));
        calendardatestream.foreach((key, value) -> handleCalendarDate(key, value));

        final JsonSerde<Frequency> frequencyserde = new JsonSerde<>(Frequency.class, smileMapper);
        KStream<String, Frequency> frequencystream = builder.stream("dbqueue-frequency", Consumed.with(Serdes.String(), frequencyserde));
        frequencystream.foreach((key, value) -> handleFrequency(key, value));

        final JsonSerde<Stop> stopserde = new JsonSerde<>(Stop.class, smileMapper);
        KStream<String, Stop> stopstream = builder.stream("dbqueue-stop", Consumed.with(Serdes.String(), stopserde));
        stopstream.foreach((key, value) -> handleStop(key, value));

        final JsonSerde<StopTime> stoptimeserde = new JsonSerde<>(StopTime.class, smileMapper);
        KStream<String, StopTime> stoptimestream = builder.stream("dbqueue-stoptime", Consumed.with(Serdes.String(), stoptimeserde));
        stoptimestream.foreach((key, value) -> handleStopTime(key, value));

        final JsonSerde<Trip> tripserde = new JsonSerde<>(Trip.class, smileMapper);
        KStream<String, Trip> tripstream = builder.stream("dbqueue-trip", Consumed.with(Serdes.String(), tripserde));
        tripstream.foreach((key, value) -> handleTrip(key, value));

        return routestream;
    }

    private void handleRoute(String key, Route rt) {
        // Must implement a factory that returns the correct implementation
        // based on database type. Same for repository.
        DBRoute dbrt = new DBRouteSQLImpl(key, rt);
        routeRepository.save(dbrt);
    }

    private void handleCalendar(String key, ServiceCalendar rt) {
        // Must implement a factory that returns the correct implementation
        // based on database type. Same for repository.
        DBCalendar dbrt = new DBCalendarSQLImpl(key, rt);
        calendarRepository.save(dbrt);
    }

    private void handleCalendarDate(String key, ServiceCalendarDate rt) {
        // Must implement a factory that returns the correct implementation
        // based on database type. Same for repository.
        DBCalendarDate dbrt = new DBCalendarDateSQLImpl(key, rt);
        calendarDateRepository.save(dbrt);
    }

    private void handleFrequency(String key, Frequency rt) {
        // Must implement a factory that returns the correct implementation
        // based on database type. Same for repository.
        DBFrequency dbrt = new DBFrequencySQLImpl(key, rt);
        frequencyRepository.save(dbrt);
    }

    private void handleStop(String key, Stop rt) {
        // Must implement a factory that returns the correct implementation
        // based on database type. Same for repository.
        DBStop dbrt = new DBStopSQLImpl(key, rt);
        stopRepository.save(dbrt);
    }

    private void handleStopTime(String key, StopTime rt) {
        // Must implement a factory that returns the correct implementation
        // based on database type. Same for repository.
        DBStopTime dbrt = new DBStopTimeSQLImpl(key, rt);
        stopTimeRepository.save(dbrt);
    }

    private void handleTrip(String key, Trip rt) {
        // Must implement a factory that returns the correct implementation
        // based on database type. Same for repository.
        DBTrip dbrt = new DBTripSQLImpl(key, rt);
        tripRepository.save(dbrt);
    }
}
