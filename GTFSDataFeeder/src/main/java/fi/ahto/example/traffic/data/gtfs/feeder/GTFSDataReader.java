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
package fi.ahto.example.traffic.data.gtfs.feeder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jahto.utils.CommonFSTConfiguration;
import com.github.jahto.utils.FSTSerde;
import fi.ahto.example.traffic.data.contracts.internal.ServiceTrips;
import fi.ahto.example.traffic.data.contracts.internal.ServiceList;
import fi.ahto.example.traffic.data.contracts.internal.TripStop;
import fi.ahto.example.traffic.data.contracts.internal.TripStopSet;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serializer;
import org.nustaq.serialization.FSTConfiguration;
import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

/**
 *
 * @author Jouni Ahto
 */
@SpringBootApplication
public class GTFSDataReader implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(GTFSDataReader.class);

    private static String prefix = null;
    private static DataMapper mapper = null;
    private static ShapeCollector collector = null;
    private final Map<String, ServiceList> routeservices = new HashMap<>();

    @Autowired
    private GtfsEntityHandler entityHandler;

    @Autowired
    private Producer<String, Object> producer;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FSTSerde<ServiceList> fstslserde;

    @Autowired
    private FSTSerde<ServiceTrips> fststserde;

    @Autowired
    private FSTSerde<TripStopSet> fsttsserde;

    public static void main(String[] args) {
        SpringApplication.run(GTFSDataReader.class, args);
    }

    ProducerRecord sendJsonRecord(String topic, String key, Object value) {
        try {
            byte[] msg = objectMapper.writeValueAsBytes(value);
            ProducerRecord record = new ProducerRecord(topic, key, msg);
            producer.send(record);
            return record;
        } catch (JsonProcessingException ex) {
            java.util.logging.Logger.getLogger(GTFSDataReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    ProducerRecord sendRecord(String topic, String key, ServiceList value) {
        Serializer ser = fstslserde.serializer();
        byte[] msg = ser.serialize(topic, value);
        ProducerRecord record = new ProducerRecord(topic, key, msg);
        producer.send(record);
        return record;
    }

    ProducerRecord sendRecord(String topic, String key, ServiceTrips value) {
        Serializer ser = fststserde.serializer();
        byte[] msg = ser.serialize(topic, value);
        ProducerRecord record = new ProducerRecord(topic, key, msg);
        producer.send(record);
        return record;
    }

    ProducerRecord sendRecord(String topic, String key, TripStopSet value) {
        Serializer ser = fsttsserde.serializer();
        byte[] msg = ser.serialize(topic, value);
        ProducerRecord record = new ProducerRecord(topic, key, msg);
        producer.send(record);
        return record;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (args.containsOption("datadir")) {
            List<String> dirs = args.getOptionValues("datadir");
            for (String dir : dirs) {
                File file = new File(dir);
                if (file.canRead() && file.isDirectory()) {
                    if (validatedir(file)) {
                        processdir(file);
                    }
                }
            }
        } else {
            File datadir = new File("data");
            if (datadir.canRead() == false) {
                System.exit(1);
            }
            if (datadir.isDirectory() == false) {
                System.exit(1);
            }

            File[] files = datadir.listFiles();
            for (File file : files) {
                if (file.canRead() && file.isDirectory()) {
                    if (validatedir(file)) {
                        processdir(file);
                    }
                }
            }
        }
    }

    boolean validatedir(File dir) {
        File prefixfile = new File(dir, "prefix.data");
        if (prefixfile.exists() && prefixfile.canRead() && prefixfile.length() > 0) {
            try (BufferedReader br = new BufferedReader(new FileReader(prefixfile))) {
                String line = br.readLine();
                if (line != null) {
                    prefix = line;
                    return true;
                }
            } catch (FileNotFoundException ex) {
            } catch (IOException ex) {
            }
        }
        return false;
    }

    void processdir(File dir) {
        GtfsReader reader = new GtfsReader();
        reader.addEntityHandler(entityHandler);
        File input = dir.getAbsoluteFile();
        try {
            mapper = new DataMapper();
            collector = new ShapeCollector();
            reader.setInputLocation(input);
            reader.run();
            triggerChanges();
        } catch (IOException ex) {

        }
    }

    private void triggerChanges() {
        mapper.trips.forEach((k, v) -> {
            try {
                TripStop stop = v.first();
                String serviceid = v.service;
                String routeid = v.route;
                // String key = serviceid + ":" + routeid;
                String key = serviceid;
                ServiceTrips service = mapper.servicetrips.get(key);
                if (service != null) {
                    if (service.route == null) {
                        LOG.warn("Logic error!");
                        service.route = v.route;
                    }
                    if (v.direction.equals("0")) {
                        service.timesforward.put(stop.arrivalTime.toSecondOfDay(), k);
                    }
                    if (v.direction.equals("1")) {
                        service.timesbackward.put(stop.arrivalTime.toSecondOfDay(), k);
                    }
                }
                ServiceTrips block = mapper.servicetrips.get(v.block);
                if (block != null) {
                    if (service.route == null) {
                        LOG.warn("Logic error!");
                        service.route = v.route;
                    }
                    if (v.direction.equals("0")) {
                        block.timesforward.put(stop.arrivalTime.toSecondOfDay(), k);
                    }
                    if (v.direction.equals("1")) {
                        block.timesbackward.put(stop.arrivalTime.toSecondOfDay(), k);
                    }
                }
            } catch (Exception e) {
                LOG.warn("{}", e);
            }
        });

        mapper.services.forEach(
                (k, v) -> {
                    for (String route : v.routeIds) {
                        ServiceList list = routeservices.get(route);
                        if (list == null) {
                            list = new ServiceList();
                            routeservices.put(route, list);
                        }
                        list.add(v);
                    }
                }
        );

        LOG.debug("Sending routes-to-services maps");
        routeservices.forEach(
                (k, v) -> {
                    //kafkaTemplate.send("routes-to-services", k, v);
                    ProducerRecord record = sendRecord("routes-to-services", k, v);
                    //kafkaTemplate.send(record);
                    LOG.info("rts {}", k);
                }
        );

        LOG.debug("Sending services-to-trips maps");
        mapper.servicetrips.forEach(
                (k, v) -> {
                    if (k != null && v != null) {
                        if (v.route != null) {
                            //kafkaTemplate.send("services-to-trips", k, v);
                            ProducerRecord record = sendRecord("services-to-trips", k, v);
                            //kafkaTemplate.send(record);
                            LOG.info("stt {} to partition for {}", k, v.route);
                        } else {
                            LOG.warn("Logic error!");
                        }
                    } else {
                        LOG.warn("Logic error!");
                    }
                }
        );

        LOG.debug("Sending stops");
        mapper.stops.forEach(
                (k, v) -> {
                    sendJsonRecord("stops", k, v);
                    //kafkaTemplate.send("stops", k, v);
                }
        );

        LOG.debug("Sending routes");
        mapper.routes.forEach(
                (k, v) -> {
                    sendJsonRecord("routes", k, v);
                    //kafkaTemplate.send("routes", k, v);
                }
        );

        LOG.debug("Sending trips");
        mapper.trips.forEach(
                (k, v) -> {
                    sendRecord("trips", k, v);
                    //kafkaTemplate.send("trips", k, v);
                    //ProducerRecord record = sendRecord("trips", k, v);
                    //kafkaTemplate.send(record);
                }
        );

        LOG.debug("Sending shapes");
        collector.shapes.forEach(
                (k, v) -> {
                    sendJsonRecord("shapes", k, v);
                    //kafkaTemplate.send("shapes", k, v);
                }
        );

    }

    @Component
    private static class GtfsEntityHandler implements EntityHandler {

        @Override
        public void handleEntity(Object bean) {
            if (bean instanceof StopTime) {
                StopTime stoptime = (StopTime) bean;

                try {
                    mapper.add(prefix, stoptime);
                } catch (Exception e) {
                    LOG.error("Problem with " + stoptime.toString(), e);
                }

            }

            if (bean instanceof ServiceCalendar) {
                ServiceCalendar sc = (ServiceCalendar) bean;
                mapper.add(prefix, sc);
            }
            if (bean instanceof ServiceCalendarDate) {
                ServiceCalendarDate scd = (ServiceCalendarDate) bean;
                mapper.add(prefix, scd);
            }

            if (bean instanceof ShapePoint) {
                ShapePoint shape = (ShapePoint) bean;
                collector.add(prefix, shape);
            }
        }
    }
}
