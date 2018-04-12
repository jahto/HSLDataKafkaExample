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

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivityFlattened;
import fi.ahto.example.traffic.data.contracts.siri.TransitType;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
import uk.org.siri.siri20.MonitoredCallStructure;
import uk.org.siri.siri20.OnwardCallStructure;
import uk.org.siri.siri20.OnwardCallsStructure;
import uk.org.siri.siri20.Siri;
import uk.org.siri.siri20.VehicleActivityStructure;
import uk.org.siri.siri20.VehicleActivityStructure.MonitoredVehicleJourney;
import uk.org.siri.siri20.VehicleMonitoringDeliveryStructure;

/**
 *
 * @author Jouni Ahto
 */
@Component
public class SiriDataPoller {

    private static final Logger LOG = LoggerFactory.getLogger(SiriDataPoller.class);
    private static final Lock LOCK = new ReentrantLock();
    private static final String SOURCE = "NO:ENTUR";
    private static final String PREFIX = SOURCE + ":";
    private static final UUID uuid = UUID.randomUUID();

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
            // datasetId=RUT
            URI uri = new URI("http://api.entur.org/anshar/1.0/rest/vm" + "?requestorId=" + uuid.toString());
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
        /*
        if (dataFlattened != null) {
            LOG.debug("Putting data to queues");
            putDataToQueues(dataFlattened);
        }
         */
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
        objectMapper.setSerializationInclusion(Include.NON_EMPTY);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        int i = 0;
        try {
            Siri s = parseXml(in);
            List<VehicleMonitoringDeliveryStructure> vms = s.getServiceDelivery().getVehicleMonitoringDeliveries();
            List<VehicleActivityStructure> vas = vms.get(0).getVehicleActivities();

            List<VehicleActivityFlattened> vehicleActivities = new ArrayList<>();
            for (VehicleActivityStructure va : vas) {
                try {
                    VehicleActivityFlattened vaf = flattenVehicleActivity(va);
                    if (vaf != null) {
                        vehicleActivities.add(vaf);
                    } else {
                        String val = objectMapper.writeValueAsString(va);
                        LOG.error("Problem with node: " + val);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    String val = objectMapper.writeValueAsString(va);
                    LOG.error("Exception with node: " + val + ex);
                    i++;
                }
            }
            if (i > 0) {
                LOG.warn("Exceptions total: " + Integer.toString(i));
            }
        } catch (JAXBException ex) {
        } catch (XMLStreamException ex) {
        }
        return null;
    }

    public VehicleActivityFlattened flattenVehicleActivity(VehicleActivityStructure va) {
        VehicleActivityFlattened vaf = new VehicleActivityFlattened();
        vaf.setSource(SOURCE);
        va.getRecordedAtTime().toInstant();
        vaf.setRecordTime(va.getRecordedAtTime().toInstant());
        MonitoredVehicleJourney mvh = va.getMonitoredVehicleJourney();
        if (mvh.getLineRef() == null) {
            return null;
        }

        if (mvh.getDelay() != null) {
            int sign = mvh.getDelay().getSign();
            if (sign == 0) {
                vaf.setDelay(0);
            } else {
                javax.xml.datatype.Duration dur = mvh.getDelay();
                // Hope that nothing is more than a day late...
                int secs = (dur.getHours() * 60 * 60) + (dur.getMinutes() * 60) + dur.getSeconds();
                if (sign == -1) {
                    secs = 0 - secs;
                }
                vaf.setDelay(secs);
            }
        }

        if (mvh.getDirectionRef() != null) {
            vaf.setDirection(mvh.getDirectionRef().getValue());
        } else {
            vaf.setDirection("UNKNOWN");
        }
        vaf.setInternalLineId(PREFIX + mvh.getLineRef().getValue());
        vaf.setLineId(mvh.getPublishedLineNames().get(0).getValue());

        // How to get the right value?.
        vaf.setTransitType(decodeTransitType(mvh));
        vaf.setVehicleId(PREFIX + mvh.getVehicleRef().getValue());
        if (mvh.getBearing() != null) {
            vaf.setBearing(mvh.getBearing().doubleValue());
        }
        vaf.setLatitude(mvh.getVehicleLocation().getLatitude().doubleValue());
        vaf.setLongitude(mvh.getVehicleLocation().getLongitude().doubleValue());
        if (mvh.getOriginAimedDepartureTime() != null) {
            vaf.setTripStart(mvh.getOriginAimedDepartureTime().withZoneSameInstant(ZoneId.of("Europe/Oslo")));
        }
        // What does this field refer to?
        /*
        if (va.getMonitoredVehicleJourney().getMonitoredCall() != null) {
            vaf.setStopPoint(va.getMonitoredVehicleJourney().getMonitoredCall().getStopPointRef());
        }
         */

        MonitoredCallStructure mc = mvh.getMonitoredCall();
        OnwardCallsStructure oc = null;
        List<OnwardCallStructure> list = null;
        OnwardCallStructure next = null;
        oc = mvh.getOnwardCalls();
        if (oc != null) {
            list = oc.getOnwardCalls();
            if (list != null && list.size() > 0) {
                next = list.get(0);
            }
        }

        // Vehicles on line "Extra Line" of contain no information about the stops.
        if (mc.getStopPointRef() != null) {
            // Happens when the next stop is also the last one on the route
            if (oc == null) {
                vaf.setNextStopId(PREFIX + mc.getStopPointRef().getValue());
                vaf.setNextStopName(mc.getStopPointNames().get(0).getValue());
            } // Happens when the vehicle is waiting to start the journey.
            else if (mc.getVisitNumber().compareTo(BigInteger.ONE) == 0 && mc.isVehicleAtStop()) {
                if (next != null) {
                    vaf.setNextStopId(PREFIX + next.getStopPointRef().getValue());
                    vaf.setNextStopName(next.getStopPointNames().get(0).getValue());
                }
            } else if (mc.isVehicleAtStop()) {
                if (next != null) {
                    vaf.setNextStopId(PREFIX + next.getStopPointRef().getValue());
                    vaf.setNextStopName(next.getStopPointNames().get(0).getValue());
                }
            } else {
                vaf.setNextStopId(PREFIX + mc.getStopPointRef().getValue());
                vaf.setNextStopName(mc.getStopPointNames().get(0).getValue());
            }
        }
        return vaf;
    }
    
        public TransitType decodeTransitType(MonitoredVehicleJourney data) throws IllegalArgumentException {
            // Check Oslo trams
            if (data.getLineRef().getValue().startsWith("RUT:")) {
                if (data.getPublishedLineNames().get(0).getValue().equals("11")) {
                    return TransitType.TRAM;
                }
                if (data.getPublishedLineNames().get(0).getValue().equals("12")) {
                    return TransitType.TRAM;
                }
                if (data.getPublishedLineNames().get(0).getValue().equals("13")) {
                    return TransitType.TRAM;
                }
                if (data.getPublishedLineNames().get(0).getValue().equals("17")) {
                    return TransitType.TRAM;
                }
                if (data.getPublishedLineNames().get(0).getValue().equals("18")) {
                    return TransitType.TRAM;
                }
                if (data.getPublishedLineNames().get(0).getValue().equals("19")) {
                    return TransitType.TRAM;
                }
                
            }
            // Check Oslo metro, how?
            
            // Otherwise, assume it's a bus.
            return TransitType.BUS;
        }

}
