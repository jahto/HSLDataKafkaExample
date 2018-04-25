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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivityFlattened;
import fi.ahto.example.traffic.data.contracts.internal.TransitType;
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
import org.springframework.stereotype.Component;

/**
 *
 * @author Jouni Ahto
 */
@Component
public class SiriDataPoller {

    private static final Logger LOG = LoggerFactory.getLogger(SiriDataPoller.class);
    private static final Lock LOCK = new ReentrantLock();
    private static final String SOURCE = "FI:HSL";
    private static final String PREFIX = SOURCE + ":";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    ProducerFactory<String, VehicleActivityFlattened> vehicleActivityProducerFactory;

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
            LOG.debug("Putting data to queues");
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
            /*
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter("onevehicle-" + vaf.getVehicleId() + ".json", true));
                String val = objectMapper.writeValueAsString(vaf);
                writer.append(val);
                writer.append("\n");
                writer.close();
            } catch (JsonProcessingException ex) {
            } catch (IOException ex) {
            }
             */
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
                        VehicleActivityFlattened vaf = flattenVehicleActivity(node);
                        if (vaf != null) {
                            vehicleActivities.add(vaf);
                        } else {
                            LOG.error("Problem with node: " + node.asText());
                        }
                    } catch (IllegalArgumentException ex) {
                        LOG.error(node.asText(), ex);
                    }
                }
            }
        }

        return vehicleActivities;
    }

    public VehicleActivityFlattened flattenVehicleActivity(JsonNode node) {
        VehicleActivityFlattened vaf = new VehicleActivityFlattened();
        vaf.setSource(SOURCE);

        String rat = node.path("RecordedAtTime").asText();
        long inst = Long.parseLong(rat);
        vaf.setRecordTime(Instant.ofEpochMilli(inst));
        JsonNode jrn = node.path("MonitoredVehicleJourney");

        LineInfo line;
        if ((line = decodeLineNumber(jrn.path("LineRef").path("value").asText())) == null) {
            return null;
        }

        vaf.setDelay(jrn.path("Delay").asInt());
        vaf.setDirection(jrn.path("DirectionRef").asText());

        vaf.setInternalLineId(PREFIX + jrn.path("LineRef").path("value").asText());
        vaf.setLineId(line.getLine());

        vaf.setTransitType(line.getType());
        vaf.setVehicleId(PREFIX + jrn.path("VehicleRef").asText());
        // vaf.setBearing(jrn.path("bearing").asDouble());
        // vaf.setSpeed(jrn.path("speed").asDouble());

        JsonNode loc = jrn.path("VehicleLocation");
        vaf.setLatitude(loc.path("Latitude").asDouble());
        vaf.setLongitude(loc.path("Longitude").asDouble());

        vaf.setStopPoint(PREFIX + jrn.path("MonitoredCall").path("StopPointRef").asText());

        String datestr = jrn.path("FramedVehicleJourneyRef").path("DataFrameRef").path("value").asText();
        String timestr = jrn.path("FramedVehicleJourneyRef").path("DatedVehicleJourneyRef").asText();

        if (datestr != null && timestr != null) {
            LocalDate date = LocalDate.parse(datestr);
            Integer hour = Integer.parseInt(timestr.substring(0, 2));
            Integer minute = Integer.parseInt(timestr.substring(2));
            LocalTime time = LocalTime.of(hour, minute);
            vaf.setTripStart(ZonedDateTime.of(date, time, ZoneId.of("Europe/Helsinki")));
        }

        return vaf;
    }
    /*
    public VehicleActivityFlattened flattenVehicleActivityOld(VehicleActivityStructure va) {
        LineInfo line;
        if ((line = decodeLineNumber(va.getMonitoredVehicleJourney().getLineRef().getValue())) == null) {
            return null;
        }

        VehicleActivityFlattened vaf = new VehicleActivityFlattened();
        // vaf.setDelay(va.getMonitoredVehicleJourney().getDelaySeconds());
        vaf.setDirection(va.getMonitoredVehicleJourney().getDirectionRef().getValue());
        vaf.setInternalLineId(PREFIX + va.getMonitoredVehicleJourney().getLineRef().getValue());
        vaf.setLatitude(va.getMonitoredVehicleJourney().getVehicleLocation().getLatitude().doubleValue());
        vaf.setLineId(line.getLine());
        vaf.setLongitude(va.getMonitoredVehicleJourney().getVehicleLocation().getLongitude().doubleValue());
        vaf.setRecordTime(va.getRecordedAtTime().toInstant());
        // What does this field refer to?
        vaf.setStopPoint(va.getMonitoredVehicleJourney().getMonitoredCall().getStopPointRef().getValue());
        vaf.setNextStopId(PREFIX + va.getMonitoredVehicleJourney().getMonitoredCall().getStopPointRef().getValue());
        vaf.setTransitType(line.getType());
        vaf.setVehicleId(PREFIX + va.getMonitoredVehicleJourney().getVehicleRef().getValue());

        // LocalDate date = va.getMonitoredVehicleJourney().getFramedVehicleJourneyRef().getDataFrameRef().getValue();
        LocalDate date = LocalDate.parse(va.getMonitoredVehicleJourney().getFramedVehicleJourneyRef().getDataFrameRef().getValue());
        if (va.getMonitoredVehicleJourney().getFramedVehicleJourneyRef().getDatedVehicleJourneyRef() != null) {
            Integer hour = Integer.parseInt(va.getMonitoredVehicleJourney().getFramedVehicleJourneyRef().getDatedVehicleJourneyRef().substring(0, 2));
            Integer minute = Integer.parseInt(va.getMonitoredVehicleJourney().getFramedVehicleJourneyRef().getDatedVehicleJourneyRef().substring(2));
            LocalTime time = LocalTime.of(hour, minute);
            vaf.setTripStart(ZonedDateTime.of(date, time, ZoneId.of("Europe/Helsinki")));
        }

        return vaf;
    }
    */
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
