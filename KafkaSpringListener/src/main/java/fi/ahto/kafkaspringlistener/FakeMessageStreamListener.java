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

import fi.ahto.kafkaspringdatacontracts.FakeTestMessage;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.StreamsBuilderFactoryBean;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Service;

/**
 *
 * @author Jouni Ahto
 */
@Service
public class FakeMessageStreamListener {

    private static final Logger LOG = LoggerFactory.getLogger(FakeMessageStreamListener.class);
    private static final JsonSerde<FakeTestMessage> fakeMessageSerde = new JsonSerde<>(FakeTestMessage.class);
    private static final String storeName = "data-fake-store";
    private static final String streamName = "data-fake-raw";

    @Autowired
    private StreamsBuilderFactoryBean myStreamsBuilderFactoryBean;

    // These two must be initialized lazily. Otherwise, they seem to prevent
    // the needed initialization of kafka streams and stores to proceed.
    // Now they are only referenced in methods called from webserver, which
    // happily depends on this component to initialize itself, and the store
    // on the other hand is only needed to serve web requests.
    // That was an interesting and tricky dependency problem to solve.
    @Autowired
    @Lazy(true)
    ReadOnlyKeyValueStore<String, FakeTestMessage> fakestore;

    @Bean
    @Lazy(true)
    @DependsOn("constructFakeMessageTable")
    public ReadOnlyKeyValueStore<String, FakeTestMessage> getFakeMessageStoreBean() {
        LOG.info("Constructing getFakeMessageStoreBean");

        while (true) {
            try {
                try {
                    KafkaStreams streams = myStreamsBuilderFactoryBean.getKafkaStreams();
                    if (streams == null) {
                        LOG.info("Waiting for streams to be constructed and ready");
                        Thread.sleep(100);
                        continue;
                    }
                    ReadOnlyKeyValueStore<String, FakeTestMessage> store = streams.store(storeName, QueryableStoreTypes.keyValueStore());
                    LOG.info("Store is now open for querying");
                    return store;
                } catch (InvalidStateStoreException ex) { // store not yet open for querying
                    LOG.info("Waiting for store to open... " + ex.getMessage());
                    Thread.sleep(100);
                }
            } catch (InterruptedException ex) {
                continue;
            }
        }
    }

    /*
    Not very interesting. Just storing the state of the stream into a GlobalKTable,
    so the webserver can serve one interactive query from it. GlobalKTable because
    of course we'll get 10000000 request per seconds, need to serve all possible requests,
    and have this running behind a load-balancer on a big server farm.
     */
    @Bean
    public GlobalKTable<String, FakeTestMessage> constructFakeMessageTable(StreamsBuilder streamBuilder) {
        LOG.info("Constructing " + storeName + " with StreamsBuilder");
        GlobalKTable<String, FakeTestMessage> table
                = streamBuilder.globalTable(streamName,
                        Consumed.with(Serdes.String(), fakeMessageSerde),
                        Materialized.<String, FakeTestMessage, KeyValueStore<Bytes, byte[]>>as(storeName));
        return table;
    }

    public FakeTestMessage getLatestFakeFromStore() {
        FakeTestMessage msg = fakestore.get("latest");
        return msg;
    }

    // Just for testing that the webserver even works and returns something.
    public FakeTestMessage getLatestFake() {
        FakeTestMessage msg = new FakeTestMessage();
        msg.setMessage("This is a fake message");
        return msg;
    }
}
