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
package fi.ahto.example.traffic.data.database.feeder;

/**
 *
 * @author Jouni Ahto
 *
 * Based on an example from
 * https://www.mkyong.com/mongodb/spring-data-mongodb-jsr-310-or-java-8-new-date-apis/
 */
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
@ComponentScan(basePackages = {"fi.ahto.example.traffic.data.contracts.internal"})
public class MongoConfiguration {

    @Autowired
    MongoDbFactory mongoDbFactory;

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory, getDefaultMongoConverter());
        return mongoTemplate;
    }

    @Bean
    public MappingMongoConverter getDefaultMongoConverter() throws Exception {
        MappingMongoConverter converter = new MappingMongoConverter(
                new DefaultDbRefResolver(mongoDbFactory), new MongoMappingContext());
        return converter;
    }
}
