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
package fi.ahto.example.tkl.data.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivityFlattened;
import fi.ahto.example.traffic.data.contracts.siri.TransitType;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
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
    private static final String SOURCE = "FI:TKL";
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
            URI uri = new URI("http://data.itsfactory.fi/journeys/api/1/vehicle-activity");
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
        }
    }

    public List<VehicleActivityFlattened> readDataAsJsonNodes(InputStream in) throws IOException {
        // Could be a safer way to read incoming data in case the are occasional bad nodes.
        // Bound to happen with the source of incoming data as a moving target.
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        JsonNode data = objectMapper.readTree(in);
        JsonNode response = data.path("body");

        List<VehicleActivityFlattened> vehicleActivities = new ArrayList<>();

        if (response.isMissingNode() == false && response.isArray()) {
            for (JsonNode node : response) {
                try {
                    VehicleActivityFlattened vaf = flattenVehicleActivity(node);
                    if (vaf != null) {
                        vehicleActivities.add(vaf);
                    } else {
                        LOG.error("Problem with node: " + node.asText());
                    }
                } catch (IllegalArgumentException ex) {
                    LOG.error("Problem with node: " + node.asText(), ex);
                }
            }
        }

        return vehicleActivities;
    }

    public VehicleActivityFlattened flattenVehicleActivity(JsonNode node) {
        VehicleActivityFlattened vaf = new VehicleActivityFlattened();
        vaf.setSource(SOURCE);

        String rat = node.path("recordedAtTime").asText();
        vaf.setRecordTime(OffsetDateTime.parse(rat).toInstant());
        JsonNode jrn = node.path("monitoredVehicleJourney");

        String delay = jrn.path("delay").asText();
        String exp = "^([+-]?P)\\d+Y\\d+M(\\d+DT\\d+H\\d+M\\d+\\.\\d+S)$";
        
        if (delay.matches(exp)) {
            String res = delay.replaceAll(exp, "$1$2");
            Duration dur = Duration.parse(res);
            vaf.setDelay((int) dur.getSeconds());
        }

        vaf.setDirection(jrn.path("directionRef").asText());

        vaf.setInternalLineId(PREFIX + jrn.path("lineRef").asText());
        vaf.setLineId(jrn.path("lineRef").asText());

        // Good enough for TKL until tram traffic starts there.
        vaf.setTransitType(TransitType.BUS);
        vaf.setVehicleId(PREFIX + jrn.path("vehicleRef").asText());
        vaf.setBearing(jrn.path("bearing").asDouble());
        vaf.setSpeed(jrn.path("speed").asDouble());

        JsonNode loc = jrn.path("vehicleLocation");
        vaf.setLatitude(loc.path("latitude").asDouble());
        vaf.setLongitude(loc.path("longitude").asDouble());

        // What does this field refer to?
        /*
        if (va.getMonitoredVehicleJourney().getMonitoredCall() != null) {
            vaf.setStopPoint(va.getMonitoredVehicleJourney().getMonitoredCall().getStopPointRef());
        }
        */

        String datestr = jrn.path("framedVehicleJourneyRef").path("dateFrameRef").asText();
        String timestr = jrn.path("originAimedDepartureTime").asText();
        
        LocalDate date = LocalDate.parse(datestr);

        Integer hour = Integer.parseInt(timestr.substring(0, 2));
        Integer minute = Integer.parseInt(timestr.substring(2));
        LocalTime time = LocalTime.of(hour, minute);
        vaf.setTripStart(ZonedDateTime.of(date, time, ZoneId.of("Europe/Helsinki")));
        
        // In the hope that the first stop in onwardCalls is the next stop.
        JsonNode stops = jrn.path("onwardCalls");
        if (stops.isMissingNode() == false && stops.isArray()) {
            JsonNode stop = stops.get(0);
            String stopid = stop.path("stopPointRef").asText();
            int index = stopid.lastIndexOf('/');

            // Noted that there's the case when the vehicles speed is 0.0,
            // stops order is 1, and originShortName matches with this stop.
            // So the vehicle is clearly still waiting to start the journey.
            // Should we in that case use next stop?
            
            vaf.setNextStopId(PREFIX + stopid.substring(index + 1));
        }
        else {
            // Sometimes there are no onwardCalls. Could be because the vehicle
            // has arrived/is arriving to the last stop on the route, or something
            // else. This is just guesswork.
            String stopid = jrn.path("destinationShortName").asText();
            vaf.setNextStopId(PREFIX + stopid);
        }
        
        return vaf;
    }
}
