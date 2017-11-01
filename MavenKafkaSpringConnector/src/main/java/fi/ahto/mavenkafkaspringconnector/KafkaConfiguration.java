/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.mavenkafkaspringconnector;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
/**
 *
 * @author jah
 */
@Configuration
@EnableKafka
public class KafkaConfiguration {
    // Using tradional method, Kafka Streams does not support yet anything more than Kafka topics as sources and sinks.
    @Bean
    public ProducerFactory<Integer, FakeTestMessage> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.17.0.1:32768");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);        // ...
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "kafka-test");
        return props;
    }

    @Bean
    public KafkaTemplate<Integer, FakeTestMessage> kafkaTemplate() {
        return new KafkaTemplate<Integer, FakeTestMessage>(producerFactory());
    }
}
