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
import fi.ahto.example.traffic.data.contracts.database.sql.DBCalendar;
import fi.ahto.example.traffic.data.contracts.database.sql.DBCalendarDate;
import fi.ahto.example.traffic.data.contracts.database.sql.DBFrequency;
import fi.ahto.example.traffic.data.contracts.database.sql.DBRoute;
import fi.ahto.example.traffic.data.contracts.database.sql.DBStop;
import fi.ahto.example.traffic.data.contracts.internal.ServiceDataComplete;
import fi.ahto.example.traffic.data.database.repositories.sql.SQLCalendarDateRepository;
import fi.ahto.example.traffic.data.database.repositories.sql.SQLCalendarRepository;
import fi.ahto.example.traffic.data.database.repositories.sql.SQLFrequencyRepository;
import fi.ahto.example.traffic.data.database.repositories.sql.SQLRouteRepository;
import fi.ahto.example.traffic.data.database.repositories.sql.SQLStopRepository;
import fi.ahto.example.traffic.data.database.repositories.sql.SQLStopTimeRepository;
import fi.ahto.example.traffic.data.database.repositories.sql.SQLTripRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
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
    private static AtomicLong counter = new AtomicLong(1);
    private static ConcurrentHashMap<String, Long> routenums = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Long> stopnums = new ConcurrentHashMap<>();
    
    @Autowired
    @Qualifier("binary")
    private ObjectMapper smileMapper;

    @Autowired
    private SQLRouteRepository routeRepository;

    @Autowired
    private SQLStopRepository stopRepository;

    @Autowired
    private SQLCalendarRepository calendarRepository;

    @Autowired
    private SQLCalendarDateRepository calendarDateRepository;

    @Autowired
    private SQLFrequencyRepository frequencyRepository;

    /*
    @Autowired
    private SQLStopTimeRepository stopTimeRepository;

    @Autowired
    private SQLTripRepository tripRepository;
    */
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
        /*
        final JsonSerde<ServiceCalendar> calendarserde = new JsonSerde<>(ServiceCalendar.class, smileMapper);
        KStream<String, ServiceCalendar> calendarstream = builder.stream("dbqueue-calendar", Consumed.with(Serdes.String(), calendarserde));
        calendarstream.foreach((key, value) -> handleCalendar(key, value));

        final JsonSerde<ServiceCalendarDate> calendardateserde = new JsonSerde<>(ServiceCalendarDate.class, smileMapper);
        KStream<String, ServiceCalendarDate> calendardatestream = builder.stream("dbqueue-calendardate", Consumed.with(Serdes.String(), calendardateserde));
        calendardatestream.foreach((key, value) -> handleCalendarDate(key, value));
        */
        /*
        final JsonSerde<Frequency> frequencyserde = new JsonSerde<>(Frequency.class, smileMapper);
        KStream<String, Frequency> frequencystream = builder.stream("dbqueue-frequency", Consumed.with(Serdes.String(), frequencyserde));
        frequencystream.foreach((key, value) -> handleFrequency(key, value));
        */
        /*
        final JsonSerde<StopTime> stoptimeserde = new JsonSerde<>(StopTime.class, smileMapper);
        KStream<String, StopTime> stoptimestream = builder.stream("dbqueue-stoptime", Consumed.with(Serdes.String(), stoptimeserde));
        stoptimestream.foreach((key, value) -> handleStopTime(key, value));
        */
        /*
        final JsonSerde<Trip> tripserde = new JsonSerde<>(Trip.class, smileMapper);
        KStream<String, Trip> tripstream = builder.stream("dbqueue-trip", Consumed.with(Serdes.String(), tripserde));
        tripstream.foreach((key, value) -> handleTrip(key, value));
        */
        return routestream;
    }

    private void handleService(String key, ServiceDataComplete rt) {
        try {
            DBCalendar dbc = new DBCalendar(rt);
            Long sid = calendarRepository.save(dbc).getServiceNum();
            if (counter.addAndGet(1) % 100 == 0) {
                LOG.info("Handled {} records", counter);
            }
            // Just checking if the idea works. Should add everything
            // into a batch update and run it. Ok, it seems work...
            // So we can run several threads all pumping data to the db. 
            for (LocalDate ld : rt.getInUse()) {
                DBCalendarDate dbcd = new DBCalendarDate();
                dbcd.setServiceId(dbc.getServiceId());
                dbcd.setExceptionDate(ld);
                dbcd.setExceptionType((short) 1);
                calendarDateRepository.save(dbcd);
            }
            for (LocalDate ld : rt.getNotInUse()) {
                DBCalendarDate dbcd = new DBCalendarDate();
                dbcd.setServiceId(dbc.getServiceId());
                dbcd.setExceptionDate(ld);
                dbcd.setExceptionType((short) 2);
                calendarDateRepository.save(dbcd);
            }
            // Now, add the trips too...
        } catch (Exception e) {
            LOG.info("handleService", e);
        }
    }
    private void handleRoute(String key, Route rt) {
        try {
            DBRoute dbrt = new DBRoute(key, rt);
            
            Long id = routeRepository.save(dbrt).getRouteNum();
            if (id != null) {
                routenums.put(dbrt.getRouteId(), id);
            }
            
        } catch (Exception e) {
            LOG.info("handleRoute", e);
        }
    }

    private void handleStop(String key, Stop rt) {
        try {
            DBStop dbrt = new DBStop(key, rt);
            /*
            Long id = stopRepository.save(dbrt).getStopNum();
            if (id != null) {
                stopnums.put(dbrt.getStopId(), id);
            }
            */
        } catch (Exception e) {
            LOG.info("handleStop", e);
        }
    }
    /*
    private void handleCalendar(String key, ServiceCalendar rt) {
        // Must implement a factory that returns the correct implementation
        // based on database type. Same for repository.
        try {
            DBCalendar dbrt = new DBCalendarSQLImpl(key, rt);
            calendarRepository.save(dbrt);
        } catch (Exception e) {
            LOG.info("handleCalendar", e);
        }
    }

    private void handleCalendarDate(String key, ServiceCalendarDate rt) {
        // Must implement a factory that returns the correct implementation
        // based on database type. Same for repository.
        try {
            DBCalendarDate dbrt = new DBCalendarDateSQLImpl(key, rt);
            calendarDateRepository.save(dbrt);
        } catch (Exception e) {
            LOG.info("handleCalendarDate", e);
        }
    }
    */

    private void handleFrequency(String key, Frequency rt) {
        try {
            DBFrequency dbrt = new DBFrequency(key, rt);
            frequencyRepository.save(dbrt);
            if (counter.addAndGet(1) % 10000 == 0) {
                LOG.info("Handled {} records", counter);
            }
        } catch (Exception e) {
            LOG.info("handleFrequency", e);
        }
    }
    /*
    private void handleStopTime(String key, StopTime rt) {
        // Must implement a factory that returns the correct implementation
        // based on database type. Same for repository.
        try {
            DBStopTime dbrt = new DBStopTime(key, rt);
            stopTimeRepository.save(dbrt);
            if (counter.addAndGet(1) % 10000 == 0) {
                LOG.info("Handled {} records", counter);
            }
        } catch (Exception e) {
            LOG.info("handleStopTime", e);
        }
    }
    */
    /*
    private void handleTrip(String key, Trip rt) {
        // Must implement a factory that returns the correct implementation
        // based on database type. Same for repository.
        try {
            DBTrip dbrt = new DBTripSQLImpl(key, rt);
            tripRepository.save(dbrt);
        } catch (Exception e) {
            LOG.info("handleTrip", e);
        }
    }
    */
    
    private Long getRouteNumber(String id) {
        Long num = routenums.get(id);
        if (num == null) {
            Optional<DBRoute> res = routeRepository.findById(id);
            if (res.isPresent()) {
                DBRoute db = res.get();
                num = db.getRouteNum();
                routenums.put(id, num);
            }
        }
        return num;
    }
    private Long getStopNumber(String id) {
        Long num = stopnums.get(id);
        if (num == null) {
            Optional<DBStop> res = stopRepository.findById(id);
            if (res.isPresent()) {
                DBStop db = res.get();
                num = db.getStopNum();
                stopnums.put(id, num);
            }
        }
        return num;
    }
}
