/*
 * Copyright 2016-2018 the original author or authors.
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
package com.github.jahto.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectOutput;

import org.springframework.util.Assert;

/**
 * Generic {@link Serializer} for sending Java objects to Kafka as JSON.
 *
 * @param <T> class of the entity, representing messages
 *
 * @author Jouni Ahto
 */
public class FSTSerializer<T> implements Serializer<T> {
    private final FSTConfiguration conf;
    
    public FSTSerializer() {
        this(null);
    }
    
    public FSTSerializer(FSTConfiguration conf) {
        if (conf == null) {
            conf = FSTConfiguration.createDefaultConfiguration();
        }
        this.conf = conf;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // No-op
    }

    @Override
    public byte[] serialize(String topic, T data) {
        if (data == null) {
            return null;
        }
        try {
            ByteArrayOutputStream bstream = new ByteArrayOutputStream();
            FSTObjectOutput out = conf.getObjectOutput(bstream);
            out.writeObject(out);
            out.flush();
            byte[] result = out.getBuffer();
            return result;
            // Alternative and simpler method
            // byte barray[] = conf.asByteArray(data);
            // return barray;
        } catch (IOException ex) {
            throw new SerializationException("Can't serialize data [" + data + "] for topic [" + topic + "]", ex);
        }
    }

    @Override
    public void close() {
        // No-op
    }
}
