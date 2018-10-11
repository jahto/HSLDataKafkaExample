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
package fi.ahto.example.traffic.data.gtfs.feeder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.github.jahto.utils.CommonFSTConfiguration;
import com.github.jahto.utils.FSTSerde;
import fi.ahto.example.traffic.data.contracts.internal.RouteData;
import fi.ahto.example.traffic.data.contracts.internal.ServiceList;
import fi.ahto.example.traffic.data.contracts.internal.ServiceTrips;
import fi.ahto.example.traffic.data.contracts.internal.ShapeSet;
import fi.ahto.example.traffic.data.contracts.internal.StopData;
import fi.ahto.example.traffic.data.contracts.internal.TripStopSet;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.nustaq.serialization.FSTConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerde;

/**
 *
 * @author Jouni Ahto
 */
@Configuration
@EnableKafka
public class KafkaConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConfiguration.class);
 //172.18.0.4
    @Value("${BOOTSTRAP_SERVERS:localhost:9092}")
    private String bootstrapServers;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Map<String, Object> configs;

    @Autowired
    private ProducerFactory<String, Object> producerFactory;

    private final JsonSerde<Object> serde = new JsonSerde<>(objectMapper);
        
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "kafka-gtfs-feeder");
        props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "fi.ahto.example.traffic.data.contracts.internal.CustomPartitioner");
        LOG.debug("ProducerConfigs");
        return props;
    }
    
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        LOG.debug("KafkaTempate");
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public Producer<String, Object> kafkaProducer() {
        LOG.debug("KafkaProducer");
        Producer<String, Object> pr = producerFactory.createProducer();
        return pr;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        LOG.debug("ProducerFactory");
        DefaultKafkaProducerFactory<String, Object> factory = new DefaultKafkaProducerFactory<>(producerConfigs());
        /*
        final JsonSerde<Object> serde = new JsonSerde<>(objectMapper);
        Serializer<Object> ser =  serde.serializer();
        factory.setValueSerializer(ser);
        */
        return factory;
    }

    @Bean
    public ObjectMapper customizedObjectMapper() {
        // ObjectMapper mapper = new ObjectMapper();
        ObjectMapper mapper = new ObjectMapper(new SmileFactory());
        mapper.registerModule(new AfterburnerModule());
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
        mapper.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        LOG.debug("customizedObjectMapper constructed");
        return mapper;
    }
    
    @Bean
    public FSTConfiguration getFSTConfiguration() {
        FSTConfiguration conf = CommonFSTConfiguration.getCommonFSTConfiguration();
        return conf;
    }
    @Autowired
    private FSTConfiguration conf;

    @Bean
    FSTSerde<ServiceList> fstslserde() {
        return new FSTSerde<>(ServiceList.class, conf);
    }

    @Bean
    FSTSerde<ServiceTrips> fststserde() {
        return new FSTSerde<>(ServiceTrips.class, conf);
    }

    @Bean
    FSTSerde<TripStopSet> fsttsserde() {
        return new FSTSerde<>(TripStopSet.class, conf);
    }

    @Bean
    FSTSerde<StopData> fstsdserde() {
        return new FSTSerde<>(StopData.class, conf);
    }
    @Bean
    FSTSerde<RouteData> fstrtserde() {
        return new FSTSerde<>(RouteData.class, conf);
    }
    @Bean
    FSTSerde<ShapeSet> fstshapeserde() {
        return new FSTSerde<>(ShapeSet.class, conf);
    }
}
