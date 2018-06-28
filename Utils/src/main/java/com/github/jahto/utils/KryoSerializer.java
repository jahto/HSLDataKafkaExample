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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;
import org.springframework.core.ResolvableType;

/**
 * Generic {@link Serializer} for sending Java objects to Kafka as JSON.
 *
 * @param <T> class of the entity, representing messages
 *
 * @author Jouni Ahto
 */
public class KryoSerializer<T> implements Serializer<T> {

    private final Kryo conf;

    protected Class<T> targetType;

    public KryoSerializer() {
        this((Class<T>) null, null);
    }

    @SuppressWarnings("unchecked")
    public KryoSerializer(Class<T> targetType) {
        this(targetType, null);
    }

    @SuppressWarnings("unchecked")
    public KryoSerializer(Class<T> targetType, Kryo conf) {
        if (conf == null) {
            conf = new Kryo();
            conf.setRegistrationRequired(false);
            if (targetType != null) {
                conf.register(targetType);
            }
        }
        /*
        if (targetType == null) {
            targetType = (Class<T>) ResolvableType.forClass(getClass()).getSuperType().resolveGeneric(0);
        }
         */
        this.targetType = targetType;
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
        ByteArrayOutputStream bstream = new ByteArrayOutputStream();
        Output out = new Output(bstream);
        if (targetType != null) {
            conf.writeObject(out, data);
        } else {
            conf.writeClassAndObject(out, data);
        }
        out.flush();
        // return out.getBuffer();
        return bstream.toByteArray();
    }

    @Override
    public void close() {
        // No-op
    }
}
