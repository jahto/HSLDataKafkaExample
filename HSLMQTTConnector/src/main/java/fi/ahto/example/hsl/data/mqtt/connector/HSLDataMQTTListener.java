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
package fi.ahto.example.hsl.data.mqtt.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
// import fi.ahto.example.traffic.data.contracts.internal.RouteType;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

/**
 *
 * @author Jouni Ahto
 */
@Component
@IntegrationComponentScan
@EnableIntegration
public class HSLDataMQTTListener {

    private static final Logger LOG = LoggerFactory.getLogger(HSLDataMQTTListener.class);
    private static final String SOURCE = "FI:HSL";
    private static final String PREFIX = SOURCE + ":";

    @Autowired
    @Qualifier( "json")
    private ObjectMapper objectMapper;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel", autoStartup = "true")
    public MessageHandler handler() {
        return new MessageHandler() {

            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                MessageHeaders headers = message.getHeaders();
                String topic = (String) headers.get(MqttHeaders.RECEIVED_TOPIC);
                String data = (String) message.getPayload();
                System.out.println(topic);
                System.out.println(data);
                System.exit(0);

                try {
                    VehicleActivity vaf = readDataAsJsonNodes(topic, data);
                    putDataToQueues(vaf);
                } catch (IOException ex) {
                }
            }
        };
    }

    public void feedTestData(String data) throws IOException {
        String[] splitted = data.split(" ", 2);
        VehicleActivity dataFlattened = readDataAsJsonNodes(splitted[0], splitted[1]);
        if (dataFlattened != null) {
            putDataToQueues(dataFlattened);
        }
    }

    public void putDataToQueues(VehicleActivity data) {
        // KafkaTemplate<String, VehicleActivity> msgtemplate = new KafkaTemplate<>(vehicleActivityProducerFactory);
        // msgtemplate.send("data-by-vehicleid", data.getVehicleId(), data);
        kafkaTemplate.send("data-by-vehicleid", data.getVehicleId(), data);
    }

    public VehicleActivity readDataAsJsonNodes(String queue, String msg) throws IOException {
        // Could be a safer way to read incoming data in case the are occasional bad nodes.
        // Bound to happen with the source of incoming data as a moving target.

        JsonNode data = objectMapper.readTree(msg.getBytes());
        VehicleActivity vaf = flattenVehicleActivity(queue, data);
        return vaf;
    }

    public VehicleActivity flattenVehicleActivity(String queue, JsonNode node) {
        String[] splitted = queue.split("/");
        String ongoing = splitted[4];
        String operator = splitted[6];
        String vehicle = splitted[7];
        String line = splitted[8];
        String direction = splitted[9];
        String headsign = splitted[10];
        String starttime = splitted[11];
        String nextstop = splitted[12];
        
        // According to the docs, not working properly yet.
        // Not handled properly here either...
        if (ongoing.equals("upcoming")) {
            return null;
        }

        LineInfo info = decodeLineNumber(line);
        // Currently, skip some known problem cases where the GTSS data is missing.
        if (info == null) {
            return null;
        }
        
        JsonNode vp = node.path("VP");

        // Not reliable... feed contains errors
        // String vehicle = vp.path("oper").asText() + ":" + vp.path("veh").asText();
        // This seems to get correct results.
        operator = operator.replaceFirst("^0*", "");
        vehicle = vehicle.replaceFirst("^0*", "");
        vehicle = operator + ":" + vehicle;

        VehicleActivity vaf = new VehicleActivity();
        vaf.setSource(SOURCE);
        vaf.setInternalLineId(PREFIX + info.getInternal());
        vaf.setLineId(info.getLine());
        // We set it later from the route data...
        // vaf.setTransitType(info.getType());
        vaf.setVehicleId(PREFIX + vehicle);

        vaf.setDelay(vp.path("dl").asInt());
        // Use Siri and GTFS-RT definition of the meaning of delay.
        vaf.setDelay(0 - vaf.getDelay());
        vaf.setLatitude((float)vp.path("lat").asDouble());
        vaf.setLongitude((float)vp.path("long").asDouble());
        vaf.setDirection(vp.path("dir").asText());
        vaf.setRecordTime(Instant.ofEpochSecond(vp.path("tsi").asLong()));

        LocalDate operday = LocalDate.parse(vp.path("oday").asText());
        LocalTime start = LocalTime.parse(vp.path("start").asText());
        ZonedDateTime zdt = ZonedDateTime.of(operday, start, ZoneId.of("Europe/Helsinki"));
        vaf.setTripStart(zdt);
        // HSL feed seems to refer to the next stop
        if ("EOL".equals(nextstop) || nextstop.isEmpty()) {
            vaf.setAtRouteEnd(true);
        } else {
            vaf.setNextStopId(PREFIX + nextstop);
        }
        // New data available.
        vaf.setBearing((float)vp.path("hdg").asDouble());
        vaf.setSpeed((float)vp.path("spd").asDouble());
        vaf.setOperatingDate(operday);
        vaf.setStartTime(start);
        return vaf;
    }

    public LineInfo decodeLineNumber(String line) throws IllegalArgumentException {
        LineInfo rval = new LineInfo();

        if (line == null) {
            return rval;
        }

        // Feed seems to occasionally contain some left-over old data 
        if (line.length() < 4) {
            return rval;
        }

        rval.setInternal(line);
        
        // Buses - this is the first test, because it matches too many cases,
        // but possible errors will be corrected in the later tests.
        if (line.matches("^[1245679].*")) {
            // rval.setType(RouteType.BUS);
            rval.setLine(line.substring(1).replaceFirst("^0", ""));
        }

        // Suomenlinna ferry
        if (line.equals("1019")) {
            // rval.setType(RouteType.FERRY);
            rval.setLine("19");
        }

        // Metro
        if (line.startsWith("130")) {
            // rval.setType(RouteType.METRO);
            rval.setLine("M" + line.substring(4));
        }

        // Train
        if (line.startsWith("300")) {
            // rval.setType(RouteType.TRAIN);
            rval.setLine(line.substring(4));
        }

        // Helsinki trams 1-9
        if (line.startsWith("100")) {
            // rval.setType(RouteType.TRAM);
            rval.setLine(line.substring(3));
        }

        // Helsinki tram line 10
        if (line.startsWith("1010")) {
            // rval.setType(RouteType.TRAM);
            rval.setLine(line.substring(2));
        }

        // Occasional bus replacing tram
        /*
        if (rval.getType() == RouteType.TRAM && rval.getLine().endsWith("X")) {
            rval.setType(RouteType.BUS);
        }
        */
        // Observed anomalies
        if (line.equals("3001Z3")) {
            // Without the ending "3" in GTFS data, seems to be just an extended version
            // of line "Z" operating further to/from Kouvola instead of Lahti.
            rval.setInternal("3001Z");
            rval.setLine("Z");
            return null;
        }
        if (line.equals("3002U6")) {
            rval.setInternal("3002U");
            rval.setLine("U");
            return null;
        }
        if (line.equals("9787A4")) {
            return null;
        }
        if (line.equals("9788K4")) {
            return null;
        }
        if (line.equals("9994K1")) {
            return null;
        }
        if (line.equals("6919")) {
            return null;
        }
        return rval;
    }

    private class LineInfo {
        public String getInternal() {
            return internal;
        }

        public void setInternal(String internal) {
            this.internal = internal;
        }

        public String getLine() {
            return line;
        }

        public void setLine(String line) {
            this.line = line;
        }
        /*
        public RouteType getType() {
            return type;
        }

        public void setType(RouteType type) {
            this.type = type;
        }
        */
        private String line = null;
        private String internal = null;
        // private RouteType type = RouteType.UNKNOWN;
    }
}
