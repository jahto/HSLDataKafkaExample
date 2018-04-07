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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivityFlattened;
import fi.ahto.example.traffic.data.contracts.siri.TransitType;
import fi.ahto.example.traffic.data.contracts.siri.VehicleActivity;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
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
public class MQTTDataPoller {

    private static final Logger LOG = LoggerFactory.getLogger(MQTTDataPoller.class);
    private static final Lock LOCK = new ReentrantLock();
    private static final String SOURCE = "FI:HSL";
    private static final String PREFIX = SOURCE + ":";

    @Autowired
    private KafkaTemplate<String, VehicleActivityFlattened> msgtemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    ProducerFactory<String, VehicleActivityFlattened> vehicleActivityProducerFactory;
    /*
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter
                = new MqttPahoMessageDrivenChannelAdapter("tcp://213.138.147.225:1883", "testClient", "/hfp/journey/#");
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }
    */
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
                    VehicleActivityFlattened vaf = readDataAsJsonNodes(topic, data);
                    putDataToQueues(vaf);
                } catch (IOException ex) {
                }
            }
        };
    }
    /*
    @MessagingGateway(defaultRequestChannel = "mqttInputChannel")
    public interface MqttMsgproducer {

        void sendToMqtt(String data);
    }
    */
    public void putDataToQueues(VehicleActivityFlattened data) {
        KafkaTemplate<String, VehicleActivityFlattened> msgtemplate = new KafkaTemplate<>(vehicleActivityProducerFactory);
        msgtemplate.send("data-by-vehicleid", data.getVehicleId(), data);
        /*
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("onevehicle-" + vaf.getVehicleId() + ".json", true));
            String val = objectMapper.writeValueAsString(data);
            writer.append(val);
            writer.append("\n");
            writer.close();
        } catch (JsonProcessingException ex) {
            java.util.logging.Logger.getLogger(MQTTDataPoller.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(MQTTDataPoller.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
    }

    public VehicleActivityFlattened readDataAsJsonNodes(String queue, String msg) throws IOException {
        // Could be a safer way to read incoming data in case the are occasional bad nodes.
        // Bound to happen with the source of incoming data as a moving target.

        JsonNode data = objectMapper.readTree(msg);
        VehicleActivityFlattened vaf = flattenVehicleActivity(queue, data);
        return vaf;
    }

    public VehicleActivityFlattened flattenVehicleActivity(String queue, JsonNode node) {
        String[] splitted = queue.split("/");
        String vehicle = splitted[4];
        String line = splitted[5];
        String direction = splitted[6];
        String starttime = splitted[8];
        String nextstop = splitted[9];

        LineInfo info = decodeLineNumber(line);
        JsonNode vp = node.path("VP");

        VehicleActivityFlattened vaf = new VehicleActivityFlattened();
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

        Instant tripstart = Instant.parse(vp.path("tst").asText());
        ZonedDateTime zdt = ZonedDateTime.ofInstant(tripstart, ZoneId.of("Europe/Helsinki"));
        vaf.setTripStart(zdt);
        
        vaf.setNextStopId(PREFIX + nextstop);
        
        int i = 0;
        /*
        if (va.getMonitoredVehicleJourney().IsValid() == false) {
            return null;
        }
        
        LineInfo line;
        if ((line = decodeLineNumber(va.getMonitoredVehicleJourney().getLineRef().getValue())) == null) {
            return null;
        }

        VehicleActivityFlattened vaf = new VehicleActivityFlattened();
        vaf.setSource(SOURCE);
        vaf.setDelay(va.getMonitoredVehicleJourney().getDelaySeconds());
        vaf.setDirection(va.getMonitoredVehicleJourney().getDirectionRef().getValue());
        vaf.setInternalLineId(PREFIX + va.getMonitoredVehicleJourney().getLineRef().getValue());
        vaf.setLatitude(va.getMonitoredVehicleJourney().getVehicleLocation().getLatitude());
        vaf.setLineId(line.getLine());
        vaf.setLongitude(va.getMonitoredVehicleJourney().getVehicleLocation().getLongitude());
        vaf.setRecordTime(va.getRecordedAtTime());
        // What does this field refer to?
        vaf.setStopPoint(va.getMonitoredVehicleJourney().getMonitoredCall().getStopPointRef());
        vaf.setTransitType(line.getType());
        vaf.setVehicleId(PREFIX + va.getMonitoredVehicleJourney().getVehicleRef().getValue());

        LocalDate date = va.getMonitoredVehicleJourney().getFramedVehicleJourneyRef().getDataFrameRef().getValue();
        if (va.getMonitoredVehicleJourney().getFramedVehicleJourneyRef().getDatedVehicleJourneyRef() != null) {
            Integer hour = Integer.parseInt(va.getMonitoredVehicleJourney().getFramedVehicleJourneyRef().getDatedVehicleJourneyRef().substring(0, 2));
            Integer minute = Integer.parseInt(va.getMonitoredVehicleJourney().getFramedVehicleJourneyRef().getDatedVehicleJourneyRef().substring(2));
            LocalTime time = LocalTime.of(hour, minute);
            vaf.setTripStart(ZonedDateTime.of(date, time, ZoneId.of("Europe/Helsinki")));
        }

        return vaf;
         */
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

    public static final String testdata = "{\n"
            + "    \"Siri\": {\n"
            + "        \"version\": \"1.3\",\n"
            + "        \"ServiceDelivery\": {\n"
            + "            \"ResponseTimestamp\": 1509970911494,\n"
            + "            \"ProducerRef\": {\n"
            + "                \"value\": \"HSL\"\n"
            + "            },\n"
            + "            \"Status\": true,\n"
            + "            \"MoreData\": false,\n"
            + "            \"VehicleMonitoringDelivery\": [{\n"
            + "                    \"version\": \"1.3\",\n"
            + "                    \"ResponseTimestamp\": 1509970911494,\n"
            + "                    \"Status\": true,\n"
            + "                    \"VehicleActivity\": [{\n"
            + "                            \"ValidUntilTime\": 1509970818000,\n"
            + "                            \"RecordedAtTime\": 1509970788000,\n"
            + "                            \"MonitoredVehicleJourney\": {\n"
            + "                                \"LineRef\": {\n"
            + "                                    \"value\": \"2105\"\n"
            + "                                },\n"
            + "                                \"DirectionRef\": {\n"
            + "                                    \"value\": \"2\"\n"
            + "                                },\n"
            + "                                \"FramedVehicleJourneyRef\": {\n"
            + "                                    \"DataFrameRef\": {\n"
            + "                                        \"value\": \"2017-11-06\"\n"
            + "                                    },\n"
            + "                                    \"DatedVehicleJourneyRef\": \"1355\"\n"
            + "                                },\n"
            + "                                \"OperatorRef\": {\n"
            + "                                    \"value\": \"HSL\"\n"
            + "                                },\n"
            + "                                \"Monitored\": true,\n"
            + "                                \"VehicleLocation\": {\n"
            + "                                    \"Longitude\": 24.84252,\n"
            + "                                    \"Latitude\": 60.16583\n"
            + "                                },\n"
            + "                                \"Delay\": 168,\n"
            + "                                \"MonitoredCall\": {\n"
            + "                                    \"StopPointRef\": \"1201130\"\n"
            + "                                },\n"
            + "                                \"VehicleRef\": {\n"
            + "                                    \"value\": \"10428788\"\n"
            + "                                }\n"
            + "                            }\n"
            + "                        }, {\n"
            + "                            \"ValidUntilTime\": 1509970940000,\n"
            + "                            \"RecordedAtTime\": 1509970910000,\n"
            + "                            \"MonitoredVehicleJourney\": {\n"
            + "                                \"LineRef\": {\n"
            + "                                    \"value\": \"4723\"\n"
            + "                                },\n"
            + "                                \"DirectionRef\": {\n"
            + "                                    \"value\": \"1\"\n"
            + "                                },\n"
            + "                                \"FramedVehicleJourneyRef\": {\n"
            + "                                    \"DataFrameRef\": {\n"
            + "                                        \"value\": \"2017-11-06\"\n"
            + "                                    },\n"
            + "                                    \"DatedVehicleJourneyRef\": \"1420\"\n"
            + "                                },\n"
            + "                                \"OperatorRef\": {\n"
            + "                                    \"value\": \"HSL\"\n"
            + "                                },\n"
            + "                                \"Monitored\": true,\n"
            + "                                \"VehicleLocation\": {\n"
            + "                                    \"Longitude\": 25.10179,\n"
            + "                                    \"Latitude\": 60.31573\n"
            + "                                },\n"
            + "                                \"Delay\": 110,\n"
            + "                                \"MonitoredCall\": {\n"
            + "                                    \"StopPointRef\": \"4750216\"\n"
            + "                                },\n"
            + "                                \"VehicleRef\": {\n"
            + "                                    \"value\": \"46400ba6\"\n"
            + "                                }\n"
            + "                            }\n"
            + "                        }]\n"
            + "                }]\n"
            + "        }\n"
            + "    }\n"
            + "}\n"
            + "";
}
