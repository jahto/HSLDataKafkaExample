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
package fi.ahto.example.traffic.data.database.repositories.mongo;

import fi.ahto.example.traffic.data.contracts.internal.TripData;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 *
 * @author Jouni Ahto
 */
public class TripRepositoryExtendedImpl implements TripRepositoryExtended {

    @Autowired
    private MongoTemplate mongoTemplate;

    @PostConstruct
    @Override
    public void initIndexes() {
        mongoTemplate.indexOps(TripData.class)
                .ensureIndex(new Index().on("routeId", Sort.Direction.ASC));
        mongoTemplate.indexOps(TripData.class)
                .ensureIndex(new Index().on("service.validFrom", Sort.Direction.ASC));
        mongoTemplate.indexOps(TripData.class)
                .ensureIndex(new Index().on("service.validUntil", Sort.Direction.ASC));
        mongoTemplate.indexOps(TripData.class)
                .ensureIndex(new Index().on("service.inuse", Sort.Direction.ASC));
        mongoTemplate.indexOps(TripData.class)
                .ensureIndex(new Index().on("service.notinuse", Sort.Direction.ASC));
        mongoTemplate.indexOps(TripData.class)
                .ensureIndex(new Index().on("stopTimes.stopId", Sort.Direction.ASC));
        mongoTemplate.indexOps(TripData.class)
                .ensureIndex(new Index().on("stopTimes.stopSequence", Sort.Direction.ASC));
        mongoTemplate.indexOps(TripData.class)
                .ensureIndex(new Index().on("stopTimes.arrival.secs", Sort.Direction.ASC));
        mongoTemplate.indexOps(TripData.class)
                .ensureIndex(new Index().on("startTime.secs", Sort.Direction.ASC));
    }
}
