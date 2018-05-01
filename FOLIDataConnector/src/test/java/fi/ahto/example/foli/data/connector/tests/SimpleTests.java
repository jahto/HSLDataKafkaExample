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
package fi.ahto.example.foli.data.connector.tests;

import fi.ahto.example.foli.data.connector.KafkaConfiguration;
import fi.ahto.example.foli.data.connector.SiriDataPoller;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
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
    private SiriDataPoller siriDataPoller;

    @Test
    public void testReadDataAsJsonNodes() throws IOException {
        // A safer way to read incoming data in case the are occasional bad nodes.
        try (InputStream stream = new ByteArrayInputStream(TESTDATA.getBytes())) {
            List<VehicleActivity> list = siriDataPoller.readDataAsJsonNodes(stream);
            assertEquals(list.size(), 1);
            VehicleActivity vaf = list.get(0);
        }
    }

    @Test /* Uncomment when needed */
    public void feedExampleDataToQueues() {
        for (int i = 0; i < 1000; i++) {
            String postfix = Integer.toString(i);
            String filename = "exampledata-tku-" + postfix + ".json";

            File file = new File("../testdata/" + filename);
            try (InputStream stream = new FileInputStream(file)) {
                siriDataPoller.feedTestData(stream);
            } catch (IOException ex) {
                LOG.debug("Problem with file", ex);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
        }
    }
    
    private static final String TESTDATA =
"{\n" +
"    \"sys\": \"VM\",\n" +
"    \"status\": \"OK\",\n" +
"    \"servertime\": 1522564432,\n" +
"    \"result\": {\n" +
"        \"responsetimestamp\": 1522564432,\n" +
"        \"producerref\": \"jlt\",\n" +
"        \"responsemessageidentifier\": \"western-20768\",\n" +
"        \"status\": true,\n" +
"        \"moredata\": false,\n" +
"        \"vehicles\": {\n" +
"            \"160018\": {\n" +
"                \"recordedattime\": 1522564431,\n" +
"                \"validuntiltime\": 1522565031,\n" +
"                \"linkdistance\": 440,\n" +
"                \"percentage\": 50.45,\n" +
"                \"lineref\": \"6\",\n" +
"                \"directionref\": \"1\",\n" +
"                \"publishedlinename\": \"6\",\n" +
"                \"operatorref\": \"16\",\n" +
"                \"originref\": \"5043\",\n" +
"                \"originname\": \"Nuolemo\",\n" +
"                \"destinationref\": \"3016\",\n" +
"                \"destinationname\": \"Naantali, laituri 1\",\n" +
"                \"originaimeddeparturetime\": 1522564200,\n" +
"                \"destinationaimedarrivaltime\": 1522569000,\n" +
"                \"monitored\": true,\n" +
"                \"incongestion\": false,\n" +
"                \"inpanic\": false,\n" +
"                \"longitude\": 22.463995,\n" +
"                \"latitude\": 60.510722,\n" +
"                \"delay\": \"PT90S\",\n" +
"                \"blockref\": \"6323generatedBlock\",\n" +
"                \"vehicleref\": \"160018\",\n" +
"                \"previouscalls\": [{\n" +
"                        \"stoppointref\": \"5036\",\n" +
"                        \"visitnumber\": 5,\n" +
"                        \"stoppointname\": \"Kisakalliontie\",\n" +
"                        \"aimedarrivaltime\": 1522564320,\n" +
"                        \"aimeddeparturetime\": 1522564320\n" +
"                    }],\n" +
"                \"vehicleatstop\": false,\n" +
"                \"next_stoppointref\": \"5034\",\n" +
"                \"next_stoppointname\": \"Piispalantie\",\n" +
"                \"next_destinationdisplay\": \"Turku-Naantali\",\n" +
"                \"next_aimedarrivaltime\": 1522564365,\n" +
"                \"next_expectedarrivaltime\": 1522564433,\n" +
"                \"next_aimeddeparturetime\": 1522564365,\n" +
"                \"next_expecteddeparturetime\": 1522564433,\n" +
"                \"onwardcalls\": [{\n" +
"                        \"stoppointref\": \"5030\",\n" +
"                        \"visitnumber\": 7,\n" +
"                        \"stoppointname\": \"Junninlaituri\",\n" +
"                        \"aimedarrivaltime\": 1522564395,\n" +
"                        \"expectedarrivaltime\": 1522564475,\n" +
"                        \"aimeddeparturetime\": 1522564395,\n" +
"                        \"expecteddeparturetime\": 1522564475\n" +
"                    }]\n" +
"            }\n" +
"        },\n" +
"        \"xml\": null\n" +
"    }\n" +
"}";
}
