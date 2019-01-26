/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a rtcp of the License at
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
import com.github.jahto.utils.FSTSerde;
import fi.ahto.example.traffic.data.contracts.internal.RouteData;
import fi.ahto.example.traffic.data.contracts.internal.ServiceData;
import fi.ahto.example.traffic.data.contracts.internal.ServiceList;
import fi.ahto.example.traffic.data.contracts.internal.StartTimesToTrips;
import fi.ahto.example.traffic.data.contracts.internal.ShapeSet;
import fi.ahto.example.traffic.data.contracts.internal.StopData;
import fi.ahto.example.traffic.data.contracts.internal.TripData;
import fi.ahto.example.traffic.data.contracts.internal.TripStopSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serializer;
import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
    private Producer<String, Object> producer;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FSTSerde<ServiceList> fstslserde;

    @Autowired
    private FSTSerde<StartTimesToTrips> fststserde;

    @Autowired
    private FSTSerde<TripStopSet> fsttsserde;

    @Autowired
    private FSTSerde<StopData> fstsdserde;

    @Autowired
    private FSTSerde<RouteData> fstrdserde;

    @Autowired
    private FSTSerde<ShapeSet> fstshapeserde;

    public static void main(String[] args) {
        SpringApplication.run(GTFSDataReader.class, args);
    }

    void sendJsonRecord(String topic, String key, Object value) {
        try {
            byte[] msg = objectMapper.writeValueAsBytes(value);
            ProducerRecord record = new ProducerRecord(topic, key, msg);
            producer.send(record);
        } catch (JsonProcessingException ex) {
            LOG.error(topic, ex);
        }
    }

    void sendRecord(String topic, String key, ServiceList value) {
        Serializer ser = fstslserde.serializer();
        byte[] msg = ser.serialize(topic, value);
        ProducerRecord record = new ProducerRecord(topic, key, msg);
        producer.send(record);
    }

    void sendRecord(String topic, String key, StartTimesToTrips value) {
        Serializer ser = fststserde.serializer();
        byte[] msg = ser.serialize(topic, value);
        ProducerRecord record = new ProducerRecord(topic, key, msg);
        producer.send(record);
    }

    void sendRecord(String topic, String key, TripStopSet value) {
        Serializer ser = fsttsserde.serializer();
        byte[] msg = ser.serialize(topic, value);
        ProducerRecord record = new ProducerRecord(topic, key, msg);
        producer.send(record);
    }

    void sendRecord(String topic, String key, StopData value) {
        Serializer ser = fstsdserde.serializer();
        byte[] msg = ser.serialize(topic, value);
        ProducerRecord record = new ProducerRecord(topic, key, msg);
        producer.send(record);
    }

    void sendRecord(String topic, String key, RouteData value) {
        Serializer ser = fstrdserde.serializer();
        byte[] msg = ser.serialize(topic, value);
        ProducerRecord record = new ProducerRecord(topic, key, msg);
        producer.send(record);
    }

    void sendRecord(String topic, String key, ShapeSet value) {
        Serializer ser = fstshapeserde.serializer();
        byte[] msg = ser.serialize(topic, value);
        ProducerRecord record = new ProducerRecord(topic, key, msg);
        producer.send(record);
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
        //reader.setEntityStore(store);
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
        LOG.debug("Sending shapes");
        collector.shapes.forEach(
                (k, v) -> {
                    sendRecord("shapes", k, v.toShapeSet());
                    sendJsonRecord("dbqueue-shapes", k, v.toShapeSet());
                }
        );
        collector.shapes.clear();
        /*
        mapper.services.forEach((k, v) -> {
            ServiceData sd = v.toServiceData();
            for (TripExt t : v.getTrips()) {
                TripData td = t.toTripData(sd);
                sendJsonRecord("dbqueue-trips-complete", td.getTripId(), td);
            }
        });
         */
        mapper.services.forEach((k, v) -> {
            ServiceData sd = v.toServiceData();
            sendJsonRecord("dbqueue-services-complete", k, sd);

            final Map<String, ServiceList> routeservices = new HashMap<>();
            for (String route : v.getRoutes()) {
                ServiceList list = routeservices.get(route);
                if (list == null) {
                    list = new ServiceList();
                    routeservices.put(route, list);
                }
                list.add(v.toStreamServiceData());
            }
            routeservices.forEach(
                    (k2, v2) -> {
                        if (k2.equals("FI:TKL:90")) {
                            int i = 0;
                        }
                        sendRecord("routes-to-services", k2, v2);
                        LOG.info("rts {}", k2);
                    }
            );
        });
        mapper.services.forEach((k, v) -> {
            ServiceData sd = v.toServiceData();
            Map<String, StartTimesToTrips> servicetrips = new HashMap<>();
            for (TripExt t : v.getTrips()) {
                sendJsonRecord("dbqueue-trips-complete", t.getKey(), t.toTripData(sd));
                sendRecord("trips", t.getKey(), t.toTripStopSet());
                StopTimeExt st = t.getStopTimes().first();
                String s = v.getKey();
                String r = t.getRouteId();
                String b = t.getBlockId();
                String d = t.getDirectionId();
                String skey = s + ":" + r + ":" + d;
                if (r.equals("FI:TKL:90")) {
                    if (st.getArrivalTime().toSecondOfDay() == 21900) {
                        int i = 0;
                    }
                }
                StartTimesToTrips service = servicetrips.get(skey);
                if (service == null) {
                    service = new StartTimesToTrips();
                    service.route = r;
                    servicetrips.put(skey, service);
                }
                service.starttimes.put(st.getArrivalTime().toSecondOfDay(), t.getKey());

                if (b != null && !b.isEmpty()) {
                    String bkey = b + ":" + r; // + ":" + d;
                    StartTimesToTrips block = servicetrips.get(bkey);
                    if (block == null) {
                        block = new StartTimesToTrips();
                        block.route = r;
                        servicetrips.put(bkey, block);
                    }
                    block.starttimes.put(st.getArrivalTime().toSecondOfDay(), t.getKey());
                }
            }

            LOG.debug("Sending services-to-trips maps");
            servicetrips.forEach(
                    (key, value) -> {
                        if (key != null && v != null) {
                            if (value.route != null) {
                                if (value.route.equals("FI:TKL:90")) {
                                    int i = 0;
                                }
                                sendRecord("services-to-trips", key, value);
                                LOG.info("stt {} to partition for {}", key, value.route);
                            } else {
                                LOG.warn("Logic error!");
                            }
                        } else {
                            LOG.warn("Logic error!");
                        }
                    }
            );
        });
        /*
        mapper.services.forEach((k, v) -> {
            ServiceData sd = v.toServiceData();
            sendJsonRecord("dbqueue-services-complete", k, sd);

            final Map<String, ServiceList> routeservices = new HashMap<>();
            for (String route : v.getRoutes()) {
                ServiceList list = routeservices.get(route);
                if (list == null) {
                    list = new ServiceList();
                    routeservices.put(route, list);
                }
                list.add(v.toStreamServiceData());
            }
            routeservices.forEach(
                    (k2, v2) -> {
                        sendRecord("routes-to-services", k2, v2);
                        LOG.info("rts {}", k2);
                    }
            );
        });
         */
    }

    @Component
    private class GtfsEntityHandler implements EntityHandler {

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

            if (bean instanceof Frequency) {
                Frequency freq = (Frequency) bean;
                mapper.add(prefix, freq);
            }

            if (bean instanceof Route) {
                Route rt = (Route) bean;
                RouteExt rte = new RouteExt(prefix, rt);
                RouteData rtd = rte.toRouteData();
                sendRecord("routes", rtd.routeid, rtd);
                sendJsonRecord("dbqueue-route", rtd.routeid, rtd);
            }

            if (bean instanceof Stop) {
                Stop st = (Stop) bean;
                StopExt ste = new StopExt(prefix, st);
                StopData std = ste.toStopData();
                sendRecord("stops", std.getStopid(), std);
                sendJsonRecord("dbqueue-stop", std.getStopid(), std);
            }
        }
    }
}
