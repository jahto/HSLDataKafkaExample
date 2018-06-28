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
public class FSTSerde<T> implements Serde<T> {

    private final FSTSerializer<T> fstSerializer;

    private final FSTDeserializer<T> fstDeserializer;

    public FSTSerde() {
        this((Class<T>) null, null);
    }

    @SuppressWarnings("unchecked")
    public FSTSerde(Class<T> targetType) {
        this(targetType, null);
    }

    @SuppressWarnings("unchecked")
    public FSTSerde(FSTConfiguration conf) {
        this(null, conf);
    }

    @SuppressWarnings("unchecked")
    public FSTSerde(Class<T> targetType, FSTConfiguration conf) {
        if (conf == null) {
            conf = FSTConfiguration.createDefaultConfiguration();
        }
        if (targetType != null) {
            conf.registerClass(targetType);
        }
        this.fstSerializer = new FSTSerializer<>(conf);
        this.fstDeserializer = new FSTDeserializer<>(targetType, conf);
    }

    public FSTSerde(FSTSerializer<T> fstSerializer, FSTDeserializer<T> fstDeserializer) {
        Assert.notNull(fstSerializer, "'fstSerializer' must not be null.");
        Assert.notNull(fstDeserializer, "'fstDeserializer' must not be null.");
        this.fstSerializer = fstSerializer;
        this.fstDeserializer = fstDeserializer;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        this.fstSerializer.configure(configs, isKey);
        this.fstDeserializer.configure(configs, isKey);
    }

    @Override
    public void close() {
        this.fstSerializer.close();
        this.fstDeserializer.close();
    }

    @Override
    public Serializer<T> serializer() {
        return this.fstSerializer;
    }

    @Override
    public Deserializer<T> deserializer() {
        return this.fstDeserializer;
    }
}
