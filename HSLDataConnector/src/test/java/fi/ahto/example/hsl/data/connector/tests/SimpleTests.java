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
package fi.ahto.example.hsl.data.connector.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.ahto.example.hsl.data.connector.KafkaConfiguration;
import fi.ahto.example.hsl.data.connector.SiriDataPoller;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivityFlattened;
import fi.ahto.example.traffic.data.contracts.internal.TransitType;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @author Jouni Ahto
 */
@JsonTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = KafkaConfiguration.class)
public class SimpleTests {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleTests.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SiriDataPoller siriDataPoller;

    @Test
    public void testReadDataAsJsonNodes() throws IOException {
        // A safer way to read incoming data in case the are occasional bad nodes.
        try (InputStream stream = new ByteArrayInputStream(testdata.getBytes())) {
            List<VehicleActivityFlattened> list = siriDataPoller.readDataAsJsonNodes(stream);
            assertEquals(list.size(), 2);
            VehicleActivityFlattened vaf = list.get(0);
        }
    }

    // @Test /* Uncomment when needed */
    public void feedExampleDataToQueues() {
        for (int i = 0; i < 1000; i++) {
            String postfix = Integer.toString(i);
            String filename = "exampledata" + postfix + ".json";

            File file = new File("../testdata/" + filename);
            try (InputStream stream = new FileInputStream(file)) {
                siriDataPoller.feedTestData(stream);
            } catch (IOException ex) {
                LOG.debug("Problem with file", ex);
            }
        }
    }

    @Test
    public void testDecodeLineNumber() {
        SiriDataPoller.LineInfo line = siriDataPoller.decodeLineNumber("1006T");
        assertEquals(line.getLine(), "6T");
        assertEquals(line.getType(), TransitType.TRAM);
        line = siriDataPoller.decodeLineNumber("1010");
        assertEquals(line.getLine(), "10");
        assertEquals(line.getType(), TransitType.TRAM);
        line = siriDataPoller.decodeLineNumber("1010X");
        assertEquals(line.getLine(), "10X");
        assertEquals(line.getType(), TransitType.BUS);
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
