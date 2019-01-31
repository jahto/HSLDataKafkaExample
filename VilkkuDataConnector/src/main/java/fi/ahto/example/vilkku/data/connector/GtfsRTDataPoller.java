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
package fi.ahto.example.vilkku.data.connector;

import com.github.jahto.utils.CommonFSTConfiguration;
import com.github.jahto.utils.FSTSerde;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serializer;
import org.nustaq.serialization.FSTConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Jouni Ahto
 */
public class GtfsRTDataPoller {

    private static final Logger LOG = LoggerFactory.getLogger(GtfsRTDataPoller.class);
    private static final Lock LOCK = new ReentrantLock();
    private static final String SOURCE = "FI:VLK";
    private static final String PREFIX = SOURCE + ":";

    @Autowired
    private Producer<String, Object> producer;
    
    private final FSTConfiguration conf = CommonFSTConfiguration.getCommonFSTConfiguration();
    private final FSTSerde<VehicleActivity> fstvaserde = new FSTSerde<>(VehicleActivity.class, conf);

    void sendRecord(String topic, String key, VehicleActivity value) {
        Serializer ser = fstvaserde.serializer();
        byte[] msg = ser.serialize(topic, value);
        ProducerRecord record = new ProducerRecord(topic, key, msg);
        producer.send(record);
    }
    
    public void putDataToQueue(Collection<VehicleActivity> vehiclelist) {
        for (VehicleActivity va : vehiclelist) {
            LOG.info("Sending...");
            sendRecord("data-by-vehicleid", va.getVehicleId(), va);
        }
    }

}
