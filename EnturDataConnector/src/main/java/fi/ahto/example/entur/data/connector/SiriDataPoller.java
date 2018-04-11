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
package fi.ahto.example.entur.data.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivityFlattened;
// import fi.ahto.example.traffic.data.contracts.siri.Siri;
import fi.ahto.example.traffic.data.contracts.siri.TransitType;
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
import java.util.logging.Level;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;

import static org.rutebanken.siri20.util.SiriXml.parseXml;
import uk.org.siri.siri20.Siri;

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
    private KafkaTemplate<String, VehicleActivityFlattened> msgtemplate;

    @Autowired
    @Qualifier("json")
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("xml")
    private XmlMapper xmlMapper;

    @Autowired
    ProducerFactory<String, VehicleActivityFlattened> vehicleActivityProducerFactory;

    // Remove comment below when trying to actually run this...
    // @Scheduled(fixedRate = 60000)
    public void pollRealData() throws URISyntaxException {
        try {
            List<VehicleActivityFlattened> dataFlattened;
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
        try {
            // Could be a safer way to read incoming data in case the are occasional bad nodes.
            // Bound to happen with the source of incoming data as a moving target.
            // objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            // JsonNode data = objectMapper.readTree(in);
            // JsonNode response = data.path("result").path("vehicles");
            Siri s = parseXml(in);
            // JsonNode data = xmlMapper.readTree(in);
            // JsonNode response = data.path("ServiceDelivery").path("VehicleMonitoringDelivery");
            
            List<VehicleActivityFlattened> vehicleActivities = new ArrayList<>();
            /*
            // if (values.isMissingNode() == false && values.isArray()) {
            for (JsonNode node : response) {
                try {
                    VehicleActivityFlattened vaf = flattenVehicleActivity(node);
                    if (vaf != null) {
                        vehicleActivities.add(vaf);
                    } else {
                        // LOG.error("Problem with node: " + node.toString());
                    }
                } catch (IllegalArgumentException ex) {
                    LOG.error(node.asText(), ex);
                }
            }
            //}
            */
            
        } catch (JAXBException ex) {
            java.util.logging.Logger.getLogger(SiriDataPoller.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XMLStreamException ex) {
            java.util.logging.Logger.getLogger(SiriDataPoller.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public VehicleActivityFlattened flattenVehicleActivity(JsonNode node) {
        VehicleActivityFlattened vaf = new VehicleActivityFlattened();
        vaf.setSource(SOURCE);
        vaf.setRecordTime(Instant.ofEpochSecond(node.path("recordedattime").asLong()));
        vaf.setDelay((int) Duration.parse(node.path("delay").asText()).getSeconds());

        vaf.setDirection(node.path("directionref").asText());
        vaf.setInternalLineId(PREFIX + node.path("lineref").asText());
        vaf.setLineId(node.path("lineref").asText());

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

        return vaf;
    }
}
