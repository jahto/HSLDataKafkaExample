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
package fi.ahto.example.traffic.data.database.utils;

import fi.ahto.example.traffic.data.contracts.internal.GTFSLocalTime;
import fi.ahto.example.traffic.data.contracts.internal.RouteType;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
/**
 *
 * @author Jouni Ahto
 */
public class Converters {

    public static List<Converter<?, ?>> getConvertersToRegister() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(IntegerToGTFSLocalTimeConverter.INSTANCE);
        converters.add(GTFSLocalTimeToIntegerConverter.INSTANCE);
        converters.add(IntegerToRouteTypeConverter.INSTANCE);
        converters.add(RouteTypeToIntegerConverter.INSTANCE);
        return converters;
    }
    
    @ReadingConverter
    public static enum IntegerToGTFSLocalTimeConverter implements Converter<Integer, GTFSLocalTime> {
        INSTANCE;
        // @Nonnull
        @Override
        public GTFSLocalTime convert(Integer source) {
            return GTFSLocalTime.ofSecondOfDay(source);
        }
    }

    @WritingConverter
    public static enum GTFSLocalTimeToIntegerConverter implements Converter<GTFSLocalTime, Integer> {
        INSTANCE;
        // @Nonnull
        @Override
        public Integer convert(GTFSLocalTime source) {
            return source.toSecondOfDay();
        }
    }

    @ReadingConverter
    public static enum IntegerToRouteTypeConverter implements Converter<Integer, RouteType> {
        INSTANCE;
        // @Nonnull
        @Override
        public RouteType convert(Integer source) {
            return RouteType.from(source);
        }
    }

    @WritingConverter
    public static enum RouteTypeToIntegerConverter implements Converter<RouteType, Integer> {
        INSTANCE;
        // @Nonnull
        @Override
        public Integer convert(RouteType source) {
            return source.toInteger();
        }
    }
}
