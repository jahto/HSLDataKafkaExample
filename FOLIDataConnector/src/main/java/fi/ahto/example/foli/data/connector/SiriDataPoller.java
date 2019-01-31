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
package fi.ahto.example.foli.data.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jahto.utils.CommonFSTConfiguration;
import com.github.jahto.utils.FSTSerde;
import fi.ahto.example.traffic.data.contracts.internal.GTFSLocalTime;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serializer;
import org.nustaq.serialization.FSTConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author Jouni Ahto
 */
@Component
public class SiriDataPoller {

    private static final Logger LOG = LoggerFactory.getLogger(SiriDataPoller.class);
    private static final Lock LOCK = new ReentrantLock();
    private static final String SOURCE = "FI:FOLI";
    private static final String PREFIX = SOURCE + ":";
    private static final ZoneId zone = ZoneId.of("Europe/Helsinki");
    private static final LocalTime cutoff = LocalTime.of(3, 30); // Check!!!

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Producer<String, Object> producer;
    
    private final FSTConfiguration conf = CommonFSTConfiguration.getCommonFSTConfiguration();
    private final FSTSerde<VehicleActivity> fstvaserde = new FSTSerde<>(VehicleActivity.class, conf);

    void sendRecord(String topic, String key, VehicleActivity value) {
        Serializer ser = fstvaserde.serializer();
        byte[] msg = ser.serialize(topic, value);
        ProducerRecord record = new ProducerRecord(topic, key, msg);
        producer.send(record);
    }

    // Remove comment below when trying to actually run this...
    // @Scheduled(fixedRate = 60000)
    public void pollRealData() throws URISyntaxException {
        try {
            List<VehicleActivity> dataFlattened;
            // URI uri = getServiceURI();
            URI uri = new URI("http://data.foli.fi/siri/vm");
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
        List<VehicleActivity> dataFlattened = readDataAsJsonNodes(data);
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

    public void putDataToQueues(List<VehicleActivity> data) {
        for (VehicleActivity vaf : data) {
            sendRecord("data-by-vehicleid", vaf.getVehicleId(), vaf);
        }
    }

    public List<VehicleActivity> readDataAsJsonNodes(InputStream in) throws IOException {
        // Could be a safer way to read incoming data in case the are occasional bad nodes.
        // Bound to happen with the source of incoming data as a moving target.
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        JsonNode data = objectMapper.readTree(in);
        JsonNode response = data.path("result").path("vehicles");

        List<VehicleActivity> vehicleActivities = new ArrayList<>();

        for (JsonNode node : response) {
            try {
                VehicleActivity vaf = flattenVehicleActivity(node);
                if (vaf != null) {
                    vehicleActivities.add(vaf);
                // } else {
                //    LOG.error("Problem with node: " + node.asText());
                }
            } catch (Exception ex) {
                LOG.error(node.asText(), ex);
            }
        }

        return vehicleActivities;
    }

    public VehicleActivity flattenVehicleActivity(JsonNode node) {
        VehicleActivity vaf = new VehicleActivity();
        vaf.setSource(SOURCE);
        vaf.setRecordTime(Instant.ofEpochSecond(node.path("recordedattime").asLong()));
        if (node.path("delay").isMissingNode() == false) {
            vaf.setDelay((int) Duration.parse(node.path("delay").asText()).getSeconds());
        }

        String dir = node.path("directionref").asText();
        if (dir.equals("1")) {
            dir = "2";
        }
        else if (dir.equals("2")) {
            dir = "1";
        }
        vaf.setDirection(dir);
        if (!node.path("lineref").asText().isEmpty()) {
            vaf.setInternalLineId(PREFIX + node.path("lineref").asText());
            // Seems to be the same as the previous one, anyway...
            vaf.setLineId(node.path("publishedlinename").asText());
        }

        // Good enough for FOLI until tram traffic starts there.
        // We set it later from the route data...
        // vaf.setTransitType(RouteType.BUS);
        vaf.setVehicleId(PREFIX + node.path("vehicleref").asText());
        vaf.setBearing(node.path("bearing").asDouble());
        vaf.setLatitude(node.path("latitude").asDouble());
        vaf.setLongitude(node.path("longitude").asDouble());
        // What does this field refer to?
        /*
        if (va.getMonitoredVehicleJourney().getMonitoredCall() != null) {
            vaf.setStopPoint(va.getMonitoredVehicleJourney().getMonitoredCall().getStopPointRef());
        }
         */

        Instant start = Instant.ofEpochSecond(node.path("originaimeddeparturetime").asLong());
        
        ZonedDateTime zdt = ZonedDateTime.ofInstant(start, zone);

        vaf.setTripStart(zdt);
        vaf.setOperatingDate(zdt.toLocalDate());
        vaf.setStartTime(GTFSLocalTime.ofCutOffAndZonedDateTime(cutoff, zdt));

        if (node.path("next_stoppointref").asText() != null && !node.path("next_stoppointref").asText().isEmpty()) {
            vaf.setNextStopId(PREFIX + node.path("next_stoppointref").asText());
            vaf.setNextStopName(node.path("next_stoppointname").asText());
        }
        else {
            vaf.setAtRouteEnd(true);
        }

        vaf.setBlockId(PREFIX + node.path("blockref").asText());

        // Check for erroneous records where the start time is too far in the future.
        // Guessing it could be either the driver making a mistake, or the feed trying
        // to inform us about future trips, or something else. Anyway, it messes badly
        // with the further steps down the processing pipeline. And there will be some
        // day a time-table prefiller.
        // Allow to start at most 2 minutes before the scheduled time.
        Instant cmp = vaf.getRecordTime().plusSeconds(120);
        if (start.isAfter(cmp)) {
            return null;
        }
        /* This data does not seem to be trustworthy... the times can be in past
           and stop sequence does not match with GTSF data, have to check if there's
           at least any consistency, like substracting 1.
        JsonNode onwardcalls = node.path("onwardcalls");

        if (onwardcalls.isMissingNode() == false && onwardcalls.isArray()) {
            TripStopSet set = vaf.getOnwardCalls();

            for (JsonNode call : onwardcalls) {
                TripStop stop = new TripStop();
                stop.stopid = PREFIX + call.path("stoppointref").asText();
                stop.seq = call.path("visitnumber").asInt();
                // stop.name = call.path("stoppointname").asText();
                Instant inst = Instant.ofEpochSecond(call.path("expectedarrivaltime").asLong());
                LocalTime local = LocalTime.from(inst.atZone(zone));
                stop.arrivalTime = local;
                set.add(stop);
            }
        }
        */
        return vaf;
    }
}
