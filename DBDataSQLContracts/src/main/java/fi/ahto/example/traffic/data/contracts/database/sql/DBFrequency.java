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
package fi.ahto.example.traffic.data.contracts.database.sql;

import fi.ahto.example.traffic.data.contracts.internal.GTFSLocalTime;
import fi.ahto.example.traffic.data.contracts.utils.GTFSLocalTimeConverter;
import java.io.Serializable;
import javax.persistence.*;
import lombok.Data;
import org.onebusaway.gtfs.model.Frequency;

/**
 *
 * @author Jouni Ahto
 */
@Data
@Entity
@javax.persistence.Table(name = "frequencies")
@org.springframework.data.relational.core.mapping.Table(value = "frequencies")
public class DBFrequency implements Serializable {
    
    private static final long serialVersionUID = 598721321515940744L;
    @javax.persistence.Id
    @org.springframework.data.annotation.Id
    @GeneratedValue
    @javax.persistence.Column(name = "frequency_num")
    @org.springframework.data.relational.core.mapping.Column(value = "frequency_num")
    private Long frequencyNum;

    @javax.persistence.Column(name = "trip_id")
    @org.springframework.data.relational.core.mapping.Column(value = "trip_id")
    private String tripId;
    @Convert(converter = GTFSLocalTimeConverter.class)
    @javax.persistence.Column(name = "start_time")
    @org.springframework.data.relational.core.mapping.Column(value = "start_time")
    private GTFSLocalTime startTime;
    @Convert(converter = GTFSLocalTimeConverter.class)
    @javax.persistence.Column(name = "end_time")
    @org.springframework.data.relational.core.mapping.Column(value = "end_time")
    private GTFSLocalTime endTime;
    @javax.persistence.Column(name = "headway_secs")
    @org.springframework.data.relational.core.mapping.Column(value = "headway_secs")
    private short headwaySecs;
    @javax.persistence.Column(name = "exact_times")
    @org.springframework.data.relational.core.mapping.Column(value = "exact_times")
    private short exactTimes; // TODO: Check if this actually boolean.

    protected DBFrequency() {}
    
    public DBFrequency(String prefix, Frequency src) {
        this.tripId = prefix + src.getTrip().getId().getId();
        this.startTime = GTFSLocalTime.ofSecondOfDay(src.getStartTime());
        this.endTime = GTFSLocalTime.ofSecondOfDay(src.getEndTime());
        this.headwaySecs = (short) src.getHeadwaySecs();
        this.exactTimes = (short) src.getExactTimes();
    }
}
