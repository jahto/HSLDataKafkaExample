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
package fi.ahto.example.tkl.data.connector.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fi.ahto.example.tkl.data.connector.KafkaConfiguration;
import fi.ahto.example.tkl.data.connector.SiriDataPoller;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivityFlattened;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
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
        try (InputStream stream = new ByteArrayInputStream(TESTDATA.getBytes())) {
            List<VehicleActivityFlattened> list = siriDataPoller.readDataAsJsonNodes(stream);
            assertEquals(list.size(), 1);
            VehicleActivityFlattened vaf = list.get(0);
        }
    }

    // @Test   /* Uncomment when needed */
    public void feedExampleDataToQueues() {
        objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        for (int i = 0; i < 1000; i++) {
            String postfix = Integer.toString(i);
            String filename = "exampledata-tre-" + postfix + ".json";

            File file = new File("../testdata/" + filename);
            try (InputStream stream = new FileInputStream(file)) {
                siriDataPoller.feedTestData(stream);
            } catch (IOException ex) {
                LOG.debug("Problem with file", ex);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(SimpleTests.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private static final String TESTDATA ="{\n" +
"  \"status\" : \"success\",\n" +
"  \"data\" : {\n" +
"    \"headers\" : {\n" +
"      \"paging\" : {\n" +
"        \"startIndex\" : 0,\n" +
"        \"pageSize\" : 35,\n" +
"        \"moreData\" : false\n" +
"      }\n" +
"    }\n" +
"  },\n" +
"  \"body\" : [ {\n" +
"    \"recordedAtTime\" : \"2018-04-01T09:34:09.027+03:00\",\n" +
"    \"validUntilTime\" : \"2018-04-01T09:34:39.027+03:00\",\n" +
"    \"monitoredVehicleJourney\" : {\n" +
"      \"lineRef\" : \"17\",\n" +
"      \"directionRef\" : \"2\",\n" +
"      \"framedVehicleJourneyRef\" : {\n" +
"        \"dateFrameRef\" : \"2018-04-01\",\n" +
"        \"datedVehicleJourneyRef\" : \"http://178.217.134.14/journeys/api/1/journeys/17_0900_4065_1592\"\n" +
"      },\n" +
"      \"vehicleLocation\" : {\n" +
"        \"longitude\" : \"23.7837252\",\n" +
"        \"latitude\" : \"61.4988315\"\n" +
"      },\n" +
"      \"operatorRef\" : \"Paunu\",\n" +
"      \"bearing\" : \"106.0\",\n" +
"      \"delay\" : \"-P0Y0M0DT0H0M40.000S\",\n" +
"      \"vehicleRef\" : \"Paunu_111\",\n" +
"      \"journeyPatternRef\" : \"17\",\n" +
"      \"originShortName\" : \"1592\",\n" +
"      \"destinationShortName\" : \"4065\",\n" +
"      \"speed\" : \"14.4\",\n" +
"      \"originAimedDepartureTime\" : \"0900\",\n" +
"      \"onwardCalls\" : [ {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:36:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:36:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4500\",\n" +
"        \"order\" : \"33\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:36:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:36:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4502\",\n" +
"        \"order\" : \"34\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:37:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:37:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4504\",\n" +
"        \"order\" : \"35\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:38:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:38:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4506\",\n" +
"        \"order\" : \"36\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:39:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:39:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4514\",\n" +
"        \"order\" : \"37\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:40:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:40:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4516\",\n" +
"        \"order\" : \"38\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:41:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:41:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4518\",\n" +
"        \"order\" : \"39\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:43:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:43:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4520\",\n" +
"        \"order\" : \"40\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:44:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:44:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4522\",\n" +
"        \"order\" : \"41\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:45:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:45:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4524\",\n" +
"        \"order\" : \"42\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:46:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:46:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4526\",\n" +
"        \"order\" : \"43\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:47:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:47:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4556\",\n" +
"        \"order\" : \"44\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:48:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:48:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4530\",\n" +
"        \"order\" : \"45\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:50:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:50:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4568\",\n" +
"        \"order\" : \"46\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:51:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:51:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4534\",\n" +
"        \"order\" : \"47\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:51:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:51:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4536\",\n" +
"        \"order\" : \"48\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:52:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:52:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4538\",\n" +
"        \"order\" : \"49\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:52:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:52:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4540\",\n" +
"        \"order\" : \"50\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:53:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:53:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4542\",\n" +
"        \"order\" : \"51\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:53:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:53:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4418\",\n" +
"        \"order\" : \"52\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:54:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:54:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4544\",\n" +
"        \"order\" : \"53\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:55:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:55:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4546\",\n" +
"        \"order\" : \"54\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:55:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:55:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4088\",\n" +
"        \"order\" : \"55\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:56:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:56:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4102\",\n" +
"        \"order\" : \"56\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:56:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:56:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4050\",\n" +
"        \"order\" : \"57\"\n" +
"      }, {\n" +
"        \"expectedArrivalTime\" : \"2018-04-01T09:57:00+03:00\",\n" +
"        \"expectedDepartureTime\" : \"2018-04-01T09:57:00+03:00\",\n" +
"        \"stopPointRef\" : \"http://178.217.134.14/journeys/api/1/stop-points/4065\",\n" +
"        \"order\" : \"58\"\n" +
"      } ]\n" +
"    }" + 
"  } ]\n" +
"}";
}
