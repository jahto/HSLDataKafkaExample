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
package fi.ahto.example.activemq.data.connector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jahto.utils.FSTSerde;
import fi.ahto.example.traffic.data.contracts.internal.Arrivals;
import fi.ahto.example.traffic.data.contracts.internal.StopData;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import fi.ahto.example.traffic.data.contracts.internal.VehicleAtStop;
import fi.ahto.example.traffic.data.contracts.internal.VehicleDataList;
import fi.ahto.example.traffic.data.contracts.internal.VehicleHistorySet;
import java.util.logging.Level;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.nustaq.serialization.FSTConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 *
 * @author Jouni Ahto
 */
@Component
public class QueueFeeder {
    private static final Logger LOG = LoggerFactory.getLogger(QueueFeeder.class);

    @Autowired
    @Qualifier( "json")
    private ObjectMapper objectMapper;
    
    @Autowired
    @Qualifier( "binary")
    private ObjectMapper smileMapper;

    @Autowired
    private JmsTemplate jmsTemplate;
    
    @Autowired
    private FSTConfiguration conf;
    
    @Bean
    public KStream<String, Arrivals> kStream(StreamsBuilder builder) {
        final FSTSerde<Arrivals> arrserde = new FSTSerde<>(Arrivals.class, conf);
        final FSTSerde<VehicleActivity> vaserde = new FSTSerde<>(VehicleActivity.class, conf);
        final FSTSerde<VehicleDataList> valistserde = new FSTSerde<>(VehicleDataList.class, conf);
        final FSTSerde<VehicleHistorySet> vhsetserde = new FSTSerde<>(VehicleHistorySet.class, conf);
        
        KStream<String, Arrivals> arrivalsstream = builder.stream("vehicles-arriving-to-stop", Consumed.with(Serdes.String(), arrserde));
        KStream<String, VehicleDataList> linesstream = builder.stream("data-by-lineid-enhanced", Consumed.with(Serdes.String(), valistserde));
        KStream<String, VehicleActivity> vehiclesstream = builder.stream("vehicles", Consumed.with(Serdes.String(), vaserde));
        KStream<String, VehicleHistorySet> vehiclehistorystream = builder.stream("vehicle-history", Consumed.with(Serdes.String(), vhsetserde));

        arrivalsstream.foreach((key, value) -> handleArrivals(key, value));
        linesstream.foreach((key, value) -> handleLines(key, value));
        vehiclesstream.foreach((key, value) -> handleVehicles(key, value));
        vehiclehistorystream.foreach((key, value) -> handleVehicleHistory(key, value));

        return arrivalsstream;
    }
    
    private void handleArrivals(String key, Arrivals arr) {
        try {
            String[] splitted = key.split(":", 3);
            String topic = "rt.stops." + splitted[0] + "." + splitted[1] + "." + splitted[2];
            String message = objectMapper.writeValueAsString(arr);
            
            jmsTemplate.send(topic, (Session session) -> {
                TextMessage textMessage = session.createTextMessage(message);
                return textMessage;
            });
        } catch (JsonProcessingException ex) {
        }
    }

    private void handleLines(String key, VehicleDataList arr) {
        try {
            String[] splitted = key.split(":", 3);
            String topic = "rt.lines." + splitted[0] + "." + splitted[1] + "." + splitted[2];
            String message = objectMapper.writeValueAsString(arr);
            
            jmsTemplate.send(topic, (Session session) -> {
                TextMessage textMessage = session.createTextMessage(message);
                return textMessage;
            });
        } catch (JsonProcessingException ex) {
        }
    }

    private void handleVehicles(String key, VehicleActivity arr) {
        try {
            String[] splitted = key.split(":", 3);
            String topic = "rt.vehicles." + splitted[0] + "." + splitted[1] + "." + splitted[2];
            String message = objectMapper.writeValueAsString(arr);
            
            jmsTemplate.send(topic, (Session session) -> {
                TextMessage textMessage = session.createTextMessage(message);
                return textMessage;
            });
        } catch (JsonProcessingException ex) {
        }
    }

    private void handleVehicleHistory(String key, VehicleHistorySet arr) {
        try {
            String[] splitted = key.split(":", 3);
            String topic = "rt.vehiclehistory." + splitted[0] + "." + splitted[1] + "." + splitted[2];
            String message = objectMapper.writeValueAsString(arr);
            
            jmsTemplate.send(topic, (Session session) -> {
                TextMessage textMessage = session.createTextMessage(message);
                return textMessage;
            });
        } catch (JsonProcessingException ex) {
        }
    }
}
