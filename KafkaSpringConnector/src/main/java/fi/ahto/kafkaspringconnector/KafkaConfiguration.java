/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.kafkaspringconnector;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fi.ahto.kafkaspringdatacontracts.FakeTestMessage;
import fi.ahto.kafkaspringdatacontracts.siri.*;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
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
public class KafkaConfiguration {
    private static final Logger log = LoggerFactory.getLogger(KafkaConfiguration.class);

    @Value("${BOOTSTRAP_SERVERS:172.17.0.1:9092}")
    private String bootstrapServers;

    @Autowired
    private ObjectMapper objectMapper;

    // Using tradional method, Kafka Streams does not support yet anything more than Kafka topics as sources and sinks.
    @Bean
    public Map<String, Object> producerConfigs() {
        final JsonSerde<VehicleActivityFlattened> serdeinfinal = new JsonSerde<>(VehicleActivityFlattened.class, objectMapper);
        
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, VehicleActivityFlattened.class);
        // props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, serdeinfinal.serializer().getClass().getName());
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "kafka-test");
        return props;
    }
    /*
    @Bean
    public ObjectMapper customizeObjectMapper(ObjectMapper om) {
        log.info("Return customizeObjectMapper");
        return om;
    }
    */
    @Bean
    public ProducerFactory<String, FakeTestMessage> producerFactory() {
        log.info("Return producerFactory");
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, FakeTestMessage> kafkaTemplate() {
        log.info("Return kafkaTemplate");
        return new KafkaTemplate<String, FakeTestMessage>(producerFactory());
    }

    @Bean
    // @DependsOn("jackson2ObjectMapperBuilderCustomizer")
    public ProducerFactory<String, VehicleActivityFlattened> vehicleActivityProducerFactory() {
        // This seems to work and will really use the customized objectMapper.
        final JsonSerde<VehicleActivityFlattened> serde = new JsonSerde<>(VehicleActivityFlattened.class, objectMapper);
        Serializer<VehicleActivityFlattened> ser =  serde.serializer();
        DefaultKafkaProducerFactory<String, VehicleActivityFlattened> factory = new DefaultKafkaProducerFactory<>(producerConfigs());
        factory.setValueSerializer(ser);
        log.info("Return vehicleActivityProducerFactory");
        return factory;
    }

    @Bean
    // @DependsOn("jackson2ObjectMapperBuilderCustomizer")
    public KafkaTemplate<String, VehicleActivityFlattened> vehicleActivityKafkaTemplate() {
        log.info("Return vehicleActivityKafkaTemplate");
        return new KafkaTemplate<>(vehicleActivityProducerFactory());
    }
    
    @Bean
    @Primary
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return (Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) -> {
            log.info("Customizing jacksonObjectMapperBuilder");
            // Module jackson-datatype-jsr310 is automatically registered if found on classpath.
            // Unfortunately, it's not the one we want, so that's why we install the newer JavaTimeModule later.
            jacksonObjectMapperBuilder.featuresToDisable(
                    SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS,
                    DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
            jacksonObjectMapperBuilder.featuresToEnable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            jacksonObjectMapperBuilder.modulesToInstall(new JavaTimeModule());
        };
    }

    // To keep the tests happy...
    @Bean
    public SiriDataPoller siriDataPoller() {
        return new SiriDataPoller();
    }
    /*
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
    
    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
        return new RestTemplateBuilder();
    }
    */
}
