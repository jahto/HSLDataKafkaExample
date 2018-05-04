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
import fi.ahto.example.traffic.data.contracts.internal.ServiceStop;
import fi.ahto.example.traffic.data.contracts.internal.ServiceStopSet;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import fi.ahto.example.traffic.data.contracts.internal.TransitType;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
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
    private static final String SOURCE = "FI:FOLI";
    private static final String PREFIX = SOURCE + ":";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    ProducerFactory<String, VehicleActivity> vehicleActivityProducerFactory;

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
        KafkaTemplate<String, VehicleActivity> msgtemplate = new KafkaTemplate<>(vehicleActivityProducerFactory);
        for (VehicleActivity vaf : data) {
            msgtemplate.send("data-by-vehicleid", vaf.getVehicleId(), vaf);
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
                } else {
                    LOG.error("Problem with node: " + node.asText());
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

        vaf.setDirection(node.path("directionref").asText());
        vaf.setInternalLineId(PREFIX + node.path("lineref").asText());
        // Seems to be the same as the previous one, anyway...
        vaf.setLineId(node.path("publishedlinename").asText());

        // Good enough for FOLI until tram traffic starts there.
        vaf.setTransitType(TransitType.BUS);
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
        ZonedDateTime zdt = ZonedDateTime.ofInstant(start, ZoneId.of("Europe/Helsinki"));
        
        vaf.setTripStart(zdt);

        vaf.setNextStopId(PREFIX + node.path("next_stoppointref").asText());
        vaf.setNextStopName(node.path("next_stoppointname").asText());
        vaf.setTripID(node.path("blockref").asText());
        
        JsonNode onwardcalls = node.path("onwardcalls");

        if (onwardcalls.isMissingNode() == false && onwardcalls.isArray()) {
            ServiceStopSet set = vaf.getOnwardCalls();
            
            for (JsonNode call : onwardcalls) {
                ServiceStop stop = new ServiceStop();
                stop.stopid = PREFIX + call.path("stoppointref").asText();
                stop.seq = call.path("visitnumber").asInt();
                stop.name = call.path("stoppointname").asText();
                stop.arrivalTime = Instant.ofEpochSecond(call.path("expectedarrivaltime").asLong());
                set.add(stop);
            }
        }
        
        return vaf;
    }
}
