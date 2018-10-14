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
package fi.ahto.example.traffic.data.contracts.database;

import fi.ahto.example.traffic.data.contracts.internal.GTFSLocalTime;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author Jouni Ahto
 */
@Entity
public interface DBFrequency extends Serializable {
    @Id
    @Column(name = "generated_id")
            
    String getGeneratedId();

    void setGeneratedId(String generatedId);

    GTFSLocalTime getEndTime();

    short getExactTimes();

    short getHeadwaySecs();

    GTFSLocalTime getStartTime();

    String getTripId();

    void setEndTime(GTFSLocalTime endTime);

    void setExactTimes(short exactTimes);

    void setHeadwaySecs(short headwaySecs);

    void setStartTime(GTFSLocalTime startTime);

    void setTripId(String tripId);
    
}