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
import fi.ahto.example.traffic.data.contracts.internal.TransitType;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private ObjectMapper objectMapper;

    @Autowired
    ProducerFactory<String, VehicleActivity> vehicleActivityProducerFactory;

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
        KafkaTemplate<String, VehicleActivity> msgtemplate = new KafkaTemplate<>(vehicleActivityProducerFactory);
        msgtemplate.send("data-by-vehicleid", data.getVehicleId(), data);
    }

    public VehicleActivity readDataAsJsonNodes(String queue, String msg) throws IOException {
        // Could be a safer way to read incoming data in case the are occasional bad nodes.
        // Bound to happen with the source of incoming data as a moving target.

        JsonNode data = objectMapper.readTree(msg);
        VehicleActivity vaf = flattenVehicleActivity(queue, data);
        return vaf;
    }

    public VehicleActivity flattenVehicleActivity(String queue, JsonNode node) {
        String[] splitted = queue.split("/");
        String ongoing = splitted[4];
        String opeator = splitted[6];
        // String vehicle = splitted[7];
        String line = splitted[8];
        String direction = splitted[9];
        String headsign = splitted[10];
        String starttime = splitted[11];
        String nextstop = splitted[12];

        LineInfo info = decodeLineNumber(line);
        JsonNode vp = node.path("VP");

        String vehicle = vp.path("dir").asText() + "/" + vp.path("dir").asText();
        
        VehicleActivity vaf = new VehicleActivity();
        vaf.setSource(SOURCE);
        vaf.setInternalLineId(PREFIX + line);
        vaf.setLineId(info.getLine());
        vaf.setTransitType(info.getType());
        vaf.setVehicleId(PREFIX + vehicle);

        vaf.setDelay(vp.path("dl").asInt());
        vaf.setLatitude(vp.path("lat").asDouble());
        vaf.setLongitude(vp.path("long").asDouble());
        vaf.setDirection(vp.path("dir").asText());
        vaf.setRecordTime(Instant.ofEpochSecond(vp.path("tsi").asLong()));

        // Instant tripstart = Instant.parse(vp.path("tst").asText());
        // ZonedDateTime zdt = ZonedDateTime.ofInstant(tripstart, ZoneId.of("Europe/Helsinki"));
        // vaf.setTripStart(zdt);

        LocalDate operday = LocalDate.parse(vp.path("oday").asText());
        LocalTime start = LocalTime.parse(vp.path("start").asText());
        ZonedDateTime zdt = ZonedDateTime.of(operday, start, ZoneId.of("Europe/Helsinki"));
        vaf.setTripStart(zdt);
        // HSL feed seems to refer to the next stop
        vaf.setNextStopId(PREFIX + nextstop);
        
        // New data available.
        vaf.setBearing(vp.path("hdg").asDouble());
        vaf.setSpeed(vp.path("spd").asDouble());
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

        // Buses - this is the first test, because it matches too many cases,
        // but possible errors will be corrected in the later tests.
        if (line.matches("^[1245679].*")) {
            rval.setType(TransitType.BUS);
            rval.setLine(line.substring(1).replaceFirst("^0", ""));
        }

        // Suomenlinna ferry
        if (line.equals("1019")) {
            rval.setType(TransitType.FERRY);
            rval.setLine("19");
        }

        // Metro
        if (line.startsWith("130")) {
            rval.setType(TransitType.METRO);
            rval.setLine("M" + line.substring(4));
        }

        // Train
        if (line.startsWith("300")) {
            rval.setType(TransitType.TRAIN);
            rval.setLine(line.substring(4));
        }

        // Helsinki trams 1-9
        if (line.startsWith("100")) {
            rval.setType(TransitType.TRAM);
            rval.setLine(line.substring(3));
        }

        // Helsinki tram line 10
        if (line.startsWith("1010")) {
            rval.setType(TransitType.TRAM);
            rval.setLine(line.substring(2));
        }

        // Occasional bus replacing tram
        if (rval.getType() == TransitType.TRAM && rval.getLine().endsWith("X")) {
            rval.setType(TransitType.BUS);
        }

        return rval;
    }

    public class LineInfo {

        public String getLine() {
            return line;
        }

        public void setLine(String line) {
            this.line = line;
        }

        public TransitType getType() {
            return type;
        }

        public void setType(TransitType type) {
            this.type = type;
        }

        private String line = null;
        private TransitType type = TransitType.UNKNOWN;
    }
}
