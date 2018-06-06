/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.example.hsl.data.mqtt.connector.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fi.ahto.example.hsl.data.mqtt.connector.HSLDataMQTTListener;
import fi.ahto.example.hsl.data.mqtt.connector.KafkaConfiguration;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.stream.Stream;
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
    private HSLDataMQTTListener dataPoller;

    @Test
    public void testReadDataAsJsonNodes() throws IOException {
        // A safer way to read incoming data in case the are occasional bad nodes.
        objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        String[] splitted = testdata.split(" ", 2);

        VehicleActivity list = dataPoller.readDataAsJsonNodes(splitted[0], splitted[1]);
        /*
            VehicleActivity vaf = list.get(0);
            objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            String result = objectMapper.writeValueAsString(vaf);
         */
        int i = 0;
    }

    @Test
    /* Uncomment when needed */
    public void feedExampleDataToQueues() {
        // String filename = "helsinki.mqtt";
        String filename = "odd.mqtt";
        filename = "../testdata/" + filename;
        try (Stream<String> stream = Files.lines(Paths.get(filename))) {
            stream.forEach(data -> {
                try {
                    dataPoller.feedTestData(data);
                } catch (IOException ex) {
                }
            });
        } catch (IOException ex) {
        }
    }
    private final String testdata
            = "/hfp/v1/journey/ongoing/bus/0047/00239/1035/2/Munkkivuori/16:40/1304103/4/60;24/28/07/57 {\"VP\":{\"desi\":\"35\",\"dir\":\"2\",\"oper\":47,\"veh\":239,\"tst\":\"2018-05-03T13:40:01Z\",\"tsi\":1525354801,\"spd\":5.19,\"hdg\":92,\"lat\":60.205438,\"long\":24.877832,\"acc\":2.12,\"dl\":0,\"odo\":87,\"drst\":0,\"oday\":\"2018-05-03\",\"jrn\":24,\"line\":512,\"start\":\"16:40\"}}";
}
