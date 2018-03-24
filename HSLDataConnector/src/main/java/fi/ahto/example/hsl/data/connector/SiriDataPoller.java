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
package fi.ahto.example.hsl.data.connector;

import fi.ahto.example.hsl.data.contracts.siri.SiriRoot;
import fi.ahto.example.hsl.data.contracts.siri.VehicleActivity;
import fi.ahto.example.hsl.data.contracts.siri.VehicleActivityFlattened;
import fi.ahto.example.hsl.data.contracts.siri.VehicleMonitoringDelivery;
import fi.ahto.example.hsl.data.contracts.siri.TransitType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static fi.ahto.example.hsl.data.connector.SiriDataPoller.testdata;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author Jouni Ahto
 */
@Component
public class SiriDataPoller {

    private static final Logger LOG = LoggerFactory.getLogger(SiriDataPoller.class);
    private static final Lock LOCK = new ReentrantLock();

    @Autowired
    private KafkaTemplate<String, VehicleActivityFlattened> msgtemplate;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    ProducerFactory<String, VehicleActivityFlattened> vehicleActivityProducerFactory;

    // @Scheduled(fixedRate = 1000)
    public void pollTestData() {
        // Seems to be unnecessary when using the default executor, it doesn't start a new
        // task anyway until the previous one has finished. But things could change if
        // some other executor is used...
        if (!LOCK.tryLock()) {
            LOG.info("Skipping polling");
            return;
        }
        try {
            LOG.info("Polling data...");
            SiriRoot data = objectMapper.readValue(testdata, SiriRoot.class);
            Instant foo = data.getSiri().getServiceDelivery().getResponseTimestamp();
            ZonedDateTime bar = foo.atZone(ZoneId.of("Europe/Helsinki"));

            List<VehicleActivityFlattened> vehicleList = new ArrayList<>();
            for (VehicleMonitoringDelivery vmd : data.getSiri().getServiceDelivery().getVehicleMonitoringDelivery()) {
                for (VehicleActivity va : vmd.getVehicleActivity()) {
                    if (va.getMonitoredVehicleJourney().IsValid()) {
                        VehicleActivityFlattened vaf = flattenVehicleActivity(va);
                        if (vaf != null) {
                            vehicleList.add(vaf);
                        }
                    }
                }
            }

            Thread.sleep(6);
        } catch (InterruptedException e) {
            // Nothing to do, but must be catched.
        } catch (IOException ex) {
            LOG.error("", ex);
        } finally {
            LOCK.unlock();
        }
    }
    
    // Remove comment below when trying to actually run this...
    // @Scheduled(fixedRate = 60000)
    public void pollRealData() throws URISyntaxException {
        try {
            List<VehicleActivityFlattened> dataFlattened;
            // URI uri = getServiceURI();
            URI uri = new URI("http://api.digitransit.fi/realtime/vehicle-positions/v1/siriaccess/vm/json");
            try (InputStream data = fetchData(uri)) {
                dataFlattened = readDataAsJsonNodes(data);
            }
            if (dataFlattened != null) {
                putDataToQueues(dataFlattened);
            }
        } catch (IOException ex) {
            LOG.error("Problem reading data");
        }
    }
    
    public void feedTestData(InputStream data) throws IOException {
        List<VehicleActivityFlattened> dataFlattened = readDataAsJsonNodes(data);
        if (dataFlattened != null) {
            putDataToQueues(dataFlattened);
        }
    }

    public InputStream fetchData(URI uri) throws IOException {
        // Use lower level methods instead of RestTemplate.
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        ClientHttpRequest request = rf.createRequest(uri, HttpMethod.GET);
        ClientHttpResponse response = request.execute();
        return response.getBody();
    }

    public void putDataToQueues(List<VehicleActivityFlattened> data) {
        KafkaTemplate<String, VehicleActivityFlattened> msgtemplate = new KafkaTemplate<>(vehicleActivityProducerFactory);
        for (VehicleActivityFlattened vaf : data) {
            msgtemplate.send("data-by-vehicleid", vaf.getVehicleId(), vaf);
            msgtemplate.send("data-by-lineid", vaf.getLineId(), vaf);
            msgtemplate.send("data-by-jorecode", vaf.getJoreCode(), vaf);
        }
    }

    public List<VehicleActivityFlattened> readDataAsJsonNodes(InputStream in) throws IOException {
        // Could be a safer way to read incoming data in case the are occasional bad nodes.
        // Bound to happen with the source of incoming data as a moving target.
        JsonNode data = objectMapper.readTree(in);
        JsonNode response = data.path("Siri").path("ServiceDelivery").path("VehicleMonitoringDelivery");

        List<VehicleActivityFlattened> vehicleActivities = new ArrayList<>();

        if (response.isMissingNode() == false && response.isArray()) {
            // There's only one element in the array, if the documentation is correct.
            JsonNode next = response.iterator().next();
            JsonNode values = next.path("VehicleActivity");

            if (values.isMissingNode() == false && values.isArray()) {
                for (JsonNode node : values) {
                    try {
                        VehicleActivity va = objectMapper.convertValue(node, VehicleActivity.class);
                        VehicleActivityFlattened vaf = flattenVehicleActivity(va);
                        if (vaf != null) {
                            vehicleActivities.add(vaf);
                        } else {
                            // LOG.error("Problem with node: " + node.toString());
                        }
                    } catch (IllegalArgumentException ex) {
                        // LOG.error(node.asText(), ex);
                    }
                }
            }
        }

        return vehicleActivities;
    }

    public VehicleActivityFlattened flattenVehicleActivity(VehicleActivity va) {
        if (va.getMonitoredVehicleJourney().IsValid() == false) {
            return null;
        }

        LineInfo line;
        if ((line = decodeLineNumber(va.getMonitoredVehicleJourney().getLineRef().getValue())) == null) {
            return null;
        }

        VehicleActivityFlattened vaf = new VehicleActivityFlattened();
        vaf.setDelay(va.getMonitoredVehicleJourney().getDelaySeconds());
        vaf.setDirection(va.getMonitoredVehicleJourney().getDirectionRef().getValue());
        vaf.setJoreCode(va.getMonitoredVehicleJourney().getLineRef().getValue());
        vaf.setLatitude(va.getMonitoredVehicleJourney().getVehicleLocation().getLatitude());
        vaf.setLineId(line.getLine());
        vaf.setLongitude(va.getMonitoredVehicleJourney().getVehicleLocation().getLongitude());
        vaf.setRecordTime(va.getRecordedAtTime());
        // What does this field refer to?
        vaf.setStopPoint(va.getMonitoredVehicleJourney().getMonitoredCall().getStopPointRef());
        vaf.setTransitType(line.getType());
        vaf.setVehicleId(va.getMonitoredVehicleJourney().getVehicleRef().getValue());

        LocalDate date = va.getMonitoredVehicleJourney().getFramedVehicleJourneyRef().getDataFrameRef().getValue();
        if (va.getMonitoredVehicleJourney().getFramedVehicleJourneyRef().getDatedVehicleJourneyRef() != null) {
            Integer hour = Integer.parseInt(va.getMonitoredVehicleJourney().getFramedVehicleJourneyRef().getDatedVehicleJourneyRef().substring(0, 2));
            Integer minute = Integer.parseInt(va.getMonitoredVehicleJourney().getFramedVehicleJourneyRef().getDatedVehicleJourneyRef().substring(2));
            LocalTime time = LocalTime.of(hour, minute);
            vaf.setTripStart(ZonedDateTime.of(date, time, ZoneId.of("Europe/Helsinki")));
        }

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
