/*
 * Copyright 2017-2018 the original author or authors.
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

import java.util.Map;

import com.esotericsoftware.kryo.Kryo;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.nustaq.serialization.FSTConfiguration;

import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

/**
 * A {@link org.apache.kafka.common.serialization.Serde} that provides
 * serialization and deserialization using native Java serialization.
 * <p>
 * The implementation delegates to underlying {@link JavaSerializer} and
 * {@link JavaDeserializer} implementations.
 *
 * @param <T> target class for serialization/deserialization
 *
 * @author Jouni Ahto
 * @author Marius Bogoevici
 * @author Elliot Kennedy
 */
public class KryoSerde<T> implements Serde<T> {

    private final KryoSerializer<T> kryoSerializer;

    private final KryoDeserializer<T> kryoDeserializer;

    public KryoSerde() {
        this((Class<T>) null, null);
    }

    @SuppressWarnings("unchecked")
    public KryoSerde(Class<T> targetType) {
        this(targetType, null);
    }

    @SuppressWarnings("unchecked")
    public KryoSerde(Kryo conf) {
        this(null, conf);
    }

    @SuppressWarnings("unchecked")
    public KryoSerde(Class<T> targetType, Kryo conf) {
        if (conf == null) {
            conf = new Kryo();
            conf.setRegistrationRequired(false);
        }
        /*
        if (targetType != null) {
            conf.register(targetType);
        }
        */
        // conf.register(java.time.Instant.class);
        this.kryoSerializer = new KryoSerializer<>(conf);
        this.kryoDeserializer = new KryoDeserializer<>(targetType, conf);
    }

    public KryoSerde(KryoSerializer<T> kryoSerializer, KryoDeserializer<T> kryoDeserializer) {
        Assert.notNull(kryoSerializer, "'kryoSerializer' must not be null.");
        Assert.notNull(kryoDeserializer, "'kryoDeserializer' must not be null.");
        this.kryoSerializer = kryoSerializer;
        this.kryoDeserializer = kryoDeserializer;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        this.kryoSerializer.configure(configs, isKey);
        this.kryoDeserializer.configure(configs, isKey);
    }

    @Override
    public void close() {
        this.kryoSerializer.close();
        this.kryoDeserializer.close();
    }

    @Override
    public Serializer<T> serializer() {
        return this.kryoSerializer;
    }

    @Override
    public Deserializer<T> deserializer() {
        return this.kryoDeserializer;
    }
}
