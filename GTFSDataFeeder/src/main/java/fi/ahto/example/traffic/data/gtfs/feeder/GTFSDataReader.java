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

import fi.ahto.example.traffic.data.contracts.internal.RouteData;
import fi.ahto.example.traffic.data.contracts.internal.ShapeSet;
import fi.ahto.example.traffic.data.contracts.internal.StopData;
import fi.ahto.example.traffic.data.contracts.internal.TripStopSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import org.apache.kafka.common.PartitionInfo;
import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Trip;
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
    private static RoutesAndStopsMapper mapper = null;
    private static ShapeCollector collector = null;

    @Autowired
    private GtfsEntityHandler entityHandler;

    @Autowired
    private KafkaTemplate<String, StopData> stopTemplate;

    @Autowired
    private KafkaTemplate<String, RouteData> routeTemplate;

    @Autowired
    private KafkaTemplate<String, ShapeSet> shapeTemplate;

    @Autowired
    private KafkaTemplate<String, TripStopSet> tripTemplate;

    @Autowired
    private KafkaTemplate<String, HashSet<String>> guessTemplate;

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
            mapper = new RoutesAndStopsMapper();
            collector = new ShapeCollector();
            reader.setInputLocation(input);
            reader.run();
            triggerChanges();
        } catch (IOException ex) {

        }
    }

    private void triggerChanges() {
        List<PartitionInfo> parts = routeTemplate.partitionsFor("routes");
        mapper.stops.forEach((k, v) -> {
            stopTemplate.send("stops", k, v);
        });
        mapper.routes.forEach((k, v) -> {
            routeTemplate.send("routes", k, v);
        });
        
        mapper.trips.forEach((k, v) -> {
            // Try to find out how to get the right partition for k.routeid,
            // so we don't have to use a global table, there's quite lof of
            // data in trips...
            tripTemplate.send("trips", k.tripid, v);
        });
        
        mapper.guesses.forEach((k, v) -> {
            guessTemplate.send("guesses", k, v);
        });
        
        collector.shapes.forEach((k, v) -> {
            shapeTemplate.send("shapes", k, v);
        });
    }

    @Component
    private static class GtfsEntityHandler implements EntityHandler {

        @Override
        public void handleEntity(Object bean) {
            /* We can get all the data by handling StopTimes
            // TODO: Combine this data with routes served by
            if (bean instanceof Stop) {
                Stop stop = (Stop) bean;
                // System.out.println("stop: " + prefix + stop.getId().getId() + " " + stop.getName());
                // kafkaTemplate.send("stops", prefix + stop.getId().getId(), stop.getName());
            }

            // TODO: Combine this data with stops served by
            if (bean instanceof Route) {
                Route route = (Route) bean;
                // System.out.println("route: " + prefix + route.getId().getId() + " " + route.getLongName());
                // kafkaTemplate.send("routes", prefix + route.getId().getId(), route.getLongName());
            }
            */
            if (bean instanceof StopTime) {
                StopTime stoptime = (StopTime) bean;
                try {
                mapper.add(prefix, stoptime);
                }
                catch (Exception e) {
                    LOG.error("Problem with " + stoptime.toString(), e);
                }
            }
            /*
            if (bean instanceof Trip) {
                Trip trip = (Trip) bean;
                int i = 0;
            }
            */
            if (bean instanceof ShapePoint) {
                ShapePoint shape = (ShapePoint) bean;
                collector.add(prefix, shape);
            }
        }
    }
}
