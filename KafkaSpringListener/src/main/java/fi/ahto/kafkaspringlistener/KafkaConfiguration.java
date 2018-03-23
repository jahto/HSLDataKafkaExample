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
package fi.ahto.kafkaspringlistener;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.StreamsBuilderFactoryBean;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

/**
 *
 * @author Jouni Ahto
 */
@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaConfiguration.class);

    @Value("${BOOTSTRAP_SERVERS:172.17.0.1:9092}")
    private String bootstrapServers;

    // Using Kafka Streams
    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public StreamsConfig kStreamsConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-test-webserver");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class.getName());
        return new StreamsConfig(props);
    }
    
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return (Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) -> {
            LOG.info("Customizing jacksonObjectMapperBuilder");
            // Module jackson-datatype-jsr310 is automatically registered if found on classpath.
            // Unfortunately, it's not the one we want, so that's why we install the newer JavaTimeModule later.
            jacksonObjectMapperBuilder.featuresToDisable(
                    SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS,
                    DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
            jacksonObjectMapperBuilder.featuresToEnable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            jacksonObjectMapperBuilder.modulesToInstall(new JavaTimeModule());
        };
    }

    /*
    @Bean
    KafkaStreams getStreams(StreamsBuilderFactoryBean streamBuilder) {
        if (streamBuilder.isRunning() == false) {
        LOG.info("Starting StreamsBuilderFactoryBean");
            streamBuilder.start();
        }
        LOG.info("Starting KafkaStreams");
        KafkaStreams streams = streamBuilder.getKafkaStreams();
        // streams.start();
        return streams;
    }
    */
}
