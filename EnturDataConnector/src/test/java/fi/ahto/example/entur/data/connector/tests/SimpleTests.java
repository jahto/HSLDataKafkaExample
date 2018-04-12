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
package fi.ahto.example.entur.data.connector.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fi.ahto.example.entur.data.connector.KafkaConfiguration;
import fi.ahto.example.entur.data.connector.SiriDataPoller;
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
    private SiriDataPoller siriDataPoller;

    // @Test
    public void testReadDataAsJsonNodes() throws IOException {
        // A safer way to read incoming data in case the are occasional bad nodes.
        try (InputStream stream = new ByteArrayInputStream(TESTDATA.getBytes())) {
            List<VehicleActivityFlattened> list = siriDataPoller.readDataAsJsonNodes(stream);
            assertEquals(list.size(), 1);
            VehicleActivityFlattened vaf = list.get(0);
        }
    }

    @Test /* Uncomment when needed */
    public void feedExampleDataToQueues() {
        for (int i = 0; i < 1; i++) {
            String postfix = Integer.toString(i);
            String filename = "exampledata-norway-" + postfix + ".xml";

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
    
    private static final String TESTDATA = "";
}
