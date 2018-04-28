/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.example.hsl.data.mqtt.connector.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fi.ahto.example.hsl.data.mqtt.connector.KafkaConfiguration;
import fi.ahto.example.hsl.data.mqtt.connector.HSLDataMQTTListener;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
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
        // assertEquals(list.size(), 1);
        /*
            VehicleActivity vaf = list.get(0);
            objectMapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            String result = objectMapper.writeValueAsString(vaf);
         */
        int i = 0;
    }

    private final String testdata
            = "/hfp/journey/bus/05148673/2158/2/XXX/1952/2432227/60;24/16/68/50 {\"VP\":{\"desi\":\"158\",\"dir\":\"2\",\"oper\":\"XXX\",\"veh\":\"05148673\",\"tst\":\"2018-03-31T17:01:49.503Z\",\"tsi\":1522515709,\"spd\":null,\"lat\":60.16594,\"long\":24.6803,\"dl\":49,\"jrn\":\"XXX\",\"line\":\"XXX\",\"start\":\"1952\",\"source\":\"hsl roisto\"}}";
}
