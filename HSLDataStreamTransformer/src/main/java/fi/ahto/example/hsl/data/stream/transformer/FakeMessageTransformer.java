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
package fi.ahto.example.hsl.data.stream.transformer;

import fi.ahto.kafkaspringdatacontracts.FakeTestMessage;
import java.time.LocalDateTime;
import java.util.Collections;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

/**
 *
 * @author Jouni Ahto
 */
@Component
public class FakeMessageTransformer {
/*

    private static final Logger log = LoggerFactory.getLogger(FakeMessageTransformer.class);
    private static final JsonSerde<FakeTestMessage> serde = new JsonSerde<>(FakeTestMessage.class);

    void MessageTransformer() {
        log.info("MessageTransformer created");
    }
   
    @Bean
    public KStream<String, FakeTestMessage> kStream(StreamsBuilder streamBuilder) {
        log.info("Constructing stream");
        KStream<String, FakeTestMessage> stream = streamBuilder.stream("data-fake-raw", Consumed.with(Serdes.String(), serde));
        stream.flatMapValues(val -> {
                    FakeTestMessage msg = new FakeTestMessage();
                    log.info(val.getMessage());
                    msg.setMessage("Received message from kafka queue at " + LocalDateTime.now());
                    return Collections.singletonList(msg);
                })
                .to("data-fake-transformed", Produced.with(Serdes.String(), serde));
        return stream;
    }

    private FakeTestMessage transformFakeMessage() {
        return null;
    }
*/
}
