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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;

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
public class KryoDeserializer<T> implements Deserializer<T> {

    private final Kryo conf;

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

    public KryoDeserializer() {
        this((Class<T>) null, null);
    }

    @SuppressWarnings("unchecked")
    public KryoDeserializer(Class<T> targetType) {
        this(targetType, null);
    }

    @SuppressWarnings("unchecked")
    public KryoDeserializer(Class<T> targetType, Kryo conf) {
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
        T result = null; 
        Input input = new Input(data);
        if (targetType != null) {
            result = conf.readObject(input, targetType);
        } else {
            result = (T) conf.readClassAndObject(input);
        }
        return result;
    }

    @Override
    public void close() {
        // No-op
    }
}
