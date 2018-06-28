/*
 * Copyright 2015-2018 the original author or authors.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;

import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;

/**
 * Generic {@link Deserializer} for receiving JSON from Kafka and return Java
 * objects.
 *
 * @param <T> class of the entity, representing messages
 *
 * @author Jouni Ahto
 */
public class FSTDeserializer<T> implements Deserializer<T> {

    private final FSTConfiguration conf;

    /**
     * Kafka config property for the default key type if no header.
     */
    public static final String KEY_DEFAULT_TYPE = "spring.json.key.default.type";

    /**
     * Kafka config property for the default value type if no header.
     */
    public static final String VALUE_DEFAULT_TYPE = "spring.json.value.default.type";

    /**
     * Kafka config property for trusted deserialization packages.
     */
    public static final String TRUSTED_PACKAGES = "spring.json.trusted.packages";

    protected Class<T> targetType;

    public FSTDeserializer() {
        this((Class<T>) null, null);
    }

    @SuppressWarnings("unchecked")
    public FSTDeserializer(Class<T> targetType) {
        this(targetType, null);
    }

    @SuppressWarnings("unchecked")
    public FSTDeserializer(Class<T> targetType, FSTConfiguration conf) {
        if (conf == null) {
            conf = FSTConfiguration.createDefaultConfiguration();
            if (targetType != null) {
                conf.registerClass(targetType);
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

    @SuppressWarnings("unchecked")
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        try {
            if (isKey && configs.containsKey(KEY_DEFAULT_TYPE)) {
                if (configs.get(KEY_DEFAULT_TYPE) instanceof Class) {
                    this.targetType = (Class<T>) configs.get(KEY_DEFAULT_TYPE);
                } else if (configs.get(KEY_DEFAULT_TYPE) instanceof String) {
                    this.targetType = (Class<T>) ClassUtils.forName((String) configs.get(KEY_DEFAULT_TYPE), null);
                } else {
                    throw new IllegalStateException(KEY_DEFAULT_TYPE + " must be Class or String");
                }
            } else if (!isKey && configs.containsKey(VALUE_DEFAULT_TYPE)) {
                if (configs.get(VALUE_DEFAULT_TYPE) instanceof Class) {
                    this.targetType = (Class<T>) configs.get(VALUE_DEFAULT_TYPE);
                } else if (configs.get(VALUE_DEFAULT_TYPE) instanceof String) {
                    this.targetType = (Class<T>) ClassUtils.forName((String) configs.get(VALUE_DEFAULT_TYPE), null);
                } else {
                    throw new IllegalStateException(VALUE_DEFAULT_TYPE + " must be Class or String");
                }
            }
        } catch (ClassNotFoundException | LinkageError e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            ByteArrayInputStream bstream = new ByteArrayInputStream(data);
            FSTObjectInput in = conf.getObjectInput(bstream);
            T result = null;
            if (targetType != null) {
                result = (T) in.readObject(targetType);
            } else {
                result = (T) in.readObject();
            }
            return result;

        } catch (IOException | ClassNotFoundException e) {
            throw new SerializationException("Can't deserialize data [" + Arrays.toString(data)
                    + "] from topic [" + topic + "]", e);
        } catch (Exception ex) {
            throw new SerializationException("Can't deserialize data [" + Arrays.toString(data)
                    + "] from topic [" + topic + "]", ex);
        }
    }

    @Override
    public void close() {
        // No-op
    }
}
