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
package fi.ahto.example.traffic.data.line.transformer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.apache.kafka.streams.state.RocksDBConfigSetter;
// import org.msgpack.jackson.dataformat.MessagePackFactory;
// import org.rocksdb.BlockBasedTableConfig;
// import org.rocksdb.CompressionType;
// import org.rocksdb.IndexType;
// import org.rocksdb.MemTableConfig;
// import org.rocksdb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.support.serializer.JsonSerde;

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

    @Autowired
    private ObjectMapper objectMapper;

    // Using Kafka Streams
    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public StreamsConfig streamsConfig() {
        final JsonSerde<Object> jsonSerde = new JsonSerde<>(objectMapper);

        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-test-line-transformer");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, jsonSerde.getClass().getName());
        props.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class.getName());
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class);
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, "1");
        // props.put(StreamsConfig.PARTITION_GROUPER_CLASS_CONFIG, "fi.ahto.example.traffic.data.contracts.internal.CustomPartitioner");
        // Try some different tuning parameters
        // props.put(StreamsConfig.ROCKSDB_CONFIG_SETTER_CLASS_CONFIG, CustomRocksDBConfig.class);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 100 * 1024 * 1024L);
        return new StreamsConfig(props);
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
    /*
    public static class CustomRocksDBConfig implements RocksDBConfigSetter {

        @Override
        public void setConfig(final String storeName, final Options options, final Map<String, Object> configs) {
            LOG.info("Configuring state store {}", storeName);
            BlockBasedTableConfig tableConfig = new org.rocksdb.BlockBasedTableConfig();
            // Should this one be changed? Try with 10 times the default.
            tableConfig.setBlockCacheSize(80 * 1024 * 1024L);
            // Facebook says they mostly use something like 16-32 in production instead
            // of the default 4, test if it makes any difference for this project.
            // Yes, the the performance got worse...
            // tableConfig.setBlockSize(16 * 1024L);
            // tableConfig.setCacheIndexAndFilterBlocks(true);
            // No need for backwards compatibility, there's nothing to be compatible with...
            tableConfig.setFormatVersion(2);
            // IndexType indexType = tableConfig.indexType();
            long bcs = tableConfig.blockCacheSize();
            options.setTableFormatConfig(tableConfig);
            // int i = options.maxOpenFiles();
            // int b = options.bloomLocality();
            // CompressionType comp = options.compressionType();
            // MemTableConfig mtb = options.memTableConfig();
            // options.setMaxWriteBufferNumber(2);
            // The next 2 ones have a very small positive effect.
            // We do only put's and get's.
            options.optimizeForPointLookup(bcs);
            // boolean opt = options.optimizeFiltersForHits();
            // We definitely have more than 99,5% of hits... or this software doesn't work at all.
            options.setOptimizeFiltersForHits(true);
        }
    }
    */
}
