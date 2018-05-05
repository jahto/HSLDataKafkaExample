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

import fi.ahto.example.traffic.data.contracts.internal.ServiceData;
import fi.ahto.example.traffic.data.contracts.internal.TripStop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;
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

    @Autowired
    private GtfsEntityHandler entityHandler;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public static void main(String[] args) {
        SpringApplication.run(GTFSDataReader.class, args);
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
            TripStop stop = v.first();
            String serviceid = v.service;
            ServiceData service = mapper.services.get(serviceid);
            if (service != null) {
                if (v.direction.equals("0")) {
                    service.timesforward.put(stop.arrivalTime, k.tripid);
                }
                if (v.direction.equals("1")) {
                    service.timesbackward.put(stop.arrivalTime, k.tripid);
                }
            }
        });

        LOG.debug("Sending services");
        mapper.services.forEach((k, v) -> {
            kafkaTemplate.send("services", k, v);
        });

        LOG.debug("Sending stops");
        mapper.stops.forEach((k, v) -> {
            kafkaTemplate.send("stops", k, v);
        });

        LOG.debug("Sending routes");
        mapper.routes.forEach((k, v) -> {
            kafkaTemplate.send("routes", k, v);
        });

        LOG.debug("Sending trips");
        mapper.trips.forEach((k, v) -> {
            // Try to find out how to get the right partition for k.routeid,
            // so we don't have to use a global table, there's quite lof of
            // data in trips...
            kafkaTemplate.send("trips", k.tripid, v);
        });

        /*
        mapper.guesses.forEach((k, v) -> {
            guessTemplate.send("guesses", k, v);
        });
         */
        LOG.debug("Sending shapes");
        collector.shapes.forEach((k, v) -> {
            kafkaTemplate.send("shapes", k, v);
        });
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
