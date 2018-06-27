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

import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

/**
 * A {@link org.apache.kafka.common.serialization.Serde} that provides serialization and
 * deserialization using native Java serialization.
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
public class JavaSerde<T> implements Serde<T> {

	private final JavaSerializer<T> javaSerializer;

	private final JavaDeserializer<T> javaDeserializer;

	public JavaSerde() {
		this(null);
	}

	@SuppressWarnings("unchecked")
	public JavaSerde(Class<T> targetType) {
		this.javaSerializer = new JavaSerializer<>();
		if (targetType == null) {
			targetType = (Class<T>) ResolvableType.forClass(getClass()).getSuperType().resolveGeneric(0);
		}
		this.javaDeserializer = new JavaDeserializer<>(targetType);
	}

	public JavaSerde(JavaSerializer<T> javaSerializer, JavaDeserializer<T> javaDeserializer) {
		Assert.notNull(javaSerializer, "'javaSerializer' must not be null.");
		Assert.notNull(javaDeserializer, "'avaDeserializer' must not be null.");
		this.javaSerializer = javaSerializer;
		this.javaDeserializer = javaDeserializer;
	}

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
		this.javaSerializer.configure(configs, isKey);
		this.javaDeserializer.configure(configs, isKey);
	}

	@Override
	public void close() {
		this.javaSerializer.close();
		this.javaDeserializer.close();
	}

	@Override
	public Serializer<T> serializer() {
		return this.javaSerializer;
	}

	@Override
	public Deserializer<T> deserializer() {
		return this.javaDeserializer;
	}
}
