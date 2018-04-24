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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.serialization.GtfsReader;
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

    private static String prefix = null;
    private static RouteToStopsMapper mapper = null;

    @Autowired
    private GtfsEntityHandler entityHandler;

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
            mapper = new RouteToStopsMapper();
            reader.setInputLocation(input);
            reader.run();
            triggerChanges();
        } catch (IOException ex) {

        }
    }

    private void triggerChanges() {
        // Should now put the collected to kafka queues, TBD tomorrow.
    }
    
    @Component
    private static class GtfsEntityHandler implements EntityHandler {

        @Autowired
        private KafkaTemplate<String, String> kafkaTemplate;

        @Override
        public void handleEntity(Object bean) {
            if (bean instanceof Stop) {
                Stop stop = (Stop) bean;
                // System.out.println("stop: " + prefix + stop.getId().getId() + " " + stop.getName());
                kafkaTemplate.send("stops", prefix + stop.getId().getId(), stop.getName());
            }

            if (bean instanceof Route) {
                Route route = (Route) bean;
                // System.out.println("route: " + prefix + route.getId().getId() + " " + route.getLongName());
                kafkaTemplate.send("routes", prefix + route.getId().getId(), route.getLongName());
            }
            
            if (bean instanceof StopTime) {
                StopTime stoptime = (StopTime) bean;
                mapper.add(prefix, stoptime);
            }
            /*
            if (bean instanceof Trip) {
                Trip trip = (Trip) bean;
            }
            */
        }
    }
}
