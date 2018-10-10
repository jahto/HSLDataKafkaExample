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
package fi.ahto.example.traffic.data.web.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import com.github.jahto.utils.CommonFSTConfiguration;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.nustaq.serialization.FSTConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

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
    public KafkaStreamsConfiguration kStreamsConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-test-webserver");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class.getName());
        props.put(StreamsConfig.RETRY_BACKOFF_MS_CONFIG, "1000");
        props.put(StreamsConfig.RETRIES_CONFIG, "10");
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    @Qualifier("json")
    @Primary
    public ObjectMapper customizedObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
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
    @Qualifier("binary")
    public ObjectMapper customizedSmileMapper() {
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
        return CommonFSTConfiguration.getCommonFSTConfiguration();
    }

    // @Autowired
    // private FSTConfiguration conf;

    /*
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return (Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) -> {
            LOG.debug("Customizing jacksonObjectMapperBuilder");
            // Module jackson-datatype-jsr310 is automatically registered if found on classpath.
            // Unfortunately, it's not the one we want, so that's why we install the newer JavaTimeModule later.
            jacksonObjectMapperBuilder.featuresToDisable(
                    SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS,
                    DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS);
            jacksonObjectMapperBuilder.featuresToEnable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            jacksonObjectMapperBuilder.modulesToInstall(new JavaTimeModule());
        };
    }
     */
}
