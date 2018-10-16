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
package fi.ahto.example.traffic.data.contracts.database.base;

import fi.ahto.example.traffic.data.contracts.database.utils.GTFSLocalTimeConverter;
import fi.ahto.example.traffic.data.contracts.internal.GTFSLocalTime;
import java.io.Serializable;
import javax.persistence.*;
import org.onebusaway.gtfs.model.Frequency;

/**
 *
 * @author Jouni Ahto
 */
@MappedSuperclass
public class DBFrequencyBase implements Serializable {
    
    private static final long serialVersionUID = 598721321515940744L;

    @Column(name = "trip_id")
    private String tripId;
    @Column(name = "start_time")
    @Convert(converter = GTFSLocalTimeConverter.class)
    private GTFSLocalTime startTime;
    @Column(name = "end_time")
    @Convert(converter = GTFSLocalTimeConverter.class)
    private GTFSLocalTime endTime;
    @Column(name = "headway_secs")
    private short headwaySecs;
    @Column(name = "exact_times")
    private short exactTimes; // TODO: Check if this actually boolean.

    protected DBFrequencyBase() {}
    
    public DBFrequencyBase(String prefix, Frequency src) {
        this.tripId = prefix + src.getTrip();
        this.startTime = new GTFSLocalTime(src.getStartTime());
        this.endTime = new GTFSLocalTime(src.getEndTime());
        this.headwaySecs = (short) src.getHeadwaySecs();
        this.exactTimes = (short) src.getExactTimes();
    }

    //@Column(name = "trip_id")
    public String getTripId() {
        return tripId;
    }
    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    //@Column(name = "start_time")
    //@Convert(converter = GTFSLocalTimeConverter.class)
    public GTFSLocalTime getStartTime() {
        return startTime;
    }
    public void setStartTime(GTFSLocalTime startTime) {
        this.startTime = startTime;
    }

    //@Column(name = "end_time")
    //@Convert(converter = GTFSLocalTimeConverter.class)
    public GTFSLocalTime getEndTime() {
        return endTime;
    }
    public void setEndTime(GTFSLocalTime endTime) {
        this.endTime = endTime;
    }

    //@Column(name = "headway_secs")
    public short getHeadwaySecs() {
        return headwaySecs;
    }
    public void setHeadwaySecs(short headwaySecs) {
        this.headwaySecs = headwaySecs;
    }

    //@Column(name = "exact_times")
    public short getExactTimes() {
        return exactTimes;
    }
    public void setExactTimes(short exactTimes) {
        this.exactTimes = exactTimes;
    }
}
