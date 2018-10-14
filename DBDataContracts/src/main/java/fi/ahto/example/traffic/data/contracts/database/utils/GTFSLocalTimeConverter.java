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
package fi.ahto.example.traffic.data.contracts.database.utils;

import fi.ahto.example.traffic.data.contracts.internal.GTFSLocalTime;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 *
 * @author Jouni Ahto
 */
@Converter
public class GTFSLocalTimeConverter implements AttributeConverter<GTFSLocalTime, Integer> {

    @Override
    public Integer convertToDatabaseColumn(GTFSLocalTime x) {
        return x.getSecs();
    }

    @Override
    public GTFSLocalTime convertToEntityAttribute(Integer y) {
        if (y == null) {
            return null;
        }
        return new GTFSLocalTime(y.intValue());
    }
}
