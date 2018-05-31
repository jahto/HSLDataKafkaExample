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
package fi.ahto.example.hsl.data.mqtt.connector;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.kafka.support.serializer.JsonSerializer;

/**
 *
 * @author Jouni Ahto
 */
@Configuration
@EnableKafka
@EnableIntegration
public class KafkaConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaConfiguration.class);

    @Value("${BOOTSTRAP_SERVERS:172.17.0.1:9092}")
    private String bootstrapServers;

    @Autowired
    private ObjectMapper objectMapper;

    // Using tradional method, Kafka Streams does not support yet anything more than Kafka topics as sources and sinks.
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "kafka-test-connector");
        return props;
    }

    @Bean
    public ProducerFactory<String, VehicleActivity> vehicleActivityProducerFactory() {
        // This seems to work and will really use the customized objectMapper.
        final JsonSerde<VehicleActivity> serde = new JsonSerde<>(VehicleActivity.class, objectMapper);
        Serializer<VehicleActivity> ser =  serde.serializer();
        DefaultKafkaProducerFactory<String, VehicleActivity> factory = new DefaultKafkaProducerFactory<>(producerConfigs());
        factory.setValueSerializer(ser);
        LOG.debug("Return vehicleActivityProducerFactory");
        return factory;
    }

    @Bean
    public KafkaTemplate<String, VehicleActivity> vehicleActivityKafkaTemplate() {
        LOG.debug("Return vehicleActivityKafkaTemplate");
        return new KafkaTemplate<>(vehicleActivityProducerFactory());
    }
    
    @Bean
    public ObjectMapper customizedObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
        mapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        LOG.debug("customizedObjectMapper constructed");
        return mapper;
    }

    // To keep the tests happy...
    @Bean
    public HSLDataMQTTListener listener() {
        return new HSLDataMQTTListener();
    }
}
