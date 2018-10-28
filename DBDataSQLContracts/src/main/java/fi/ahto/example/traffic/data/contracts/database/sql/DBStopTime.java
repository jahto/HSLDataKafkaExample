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

import fi.ahto.example.traffic.data.contracts.internal.GTFSLocalTimeConverter;
import fi.ahto.example.traffic.data.contracts.internal.GTFSLocalTime;
import java.io.Serializable;
import javax.persistence.*;
import lombok.Data;
import org.onebusaway.gtfs.model.StopTime;

/**
 *
 * @author Jouni Ahto
 */
@Data
@Entity
@javax.persistence.Table(name = "stop_times")
@org.springframework.data.relational.core.mapping.Table(value = "stop_times")
public class DBStopTime implements Serializable {
    
    private static final long serialVersionUID = -175539650314949501L;

    @javax.persistence.Id
    @org.springframework.data.annotation.Id
    @GeneratedValue
    @javax.persistence.Column(name = "stoptime_num")
    @org.springframework.data.relational.core.mapping.Column(value = "stoptime_num")
    private Long stopTimeNum;

    @javax.persistence.Column(name = "trip_id")
    @org.springframework.data.relational.core.mapping.Column(value = "trip_id")
    private String tripId;
    @javax.persistence.Column(name = "stop_sequence")
    @org.springframework.data.relational.core.mapping.Column(value = "stop_sequence")
    private int stopSequence;
    @javax.persistence.Column(name = "stop_id")
    @org.springframework.data.relational.core.mapping.Column(value = "stop_id")
    private String stopId;
    @javax.persistence.Column(name = "headsign")
    @org.springframework.data.relational.core.mapping.Column(value = "headsign")
    private String headsign;
    @javax.persistence.Column(name = "arrival")
    @org.springframework.data.relational.core.mapping.Column(value = "arrival")
    @Convert(converter = GTFSLocalTimeConverter.class)
    private GTFSLocalTime arrival;
    @javax.persistence.Column(name = "departure")
    @org.springframework.data.relational.core.mapping.Column(value = "departure")
    @Convert(converter = GTFSLocalTimeConverter.class)
    private GTFSLocalTime departure;
    @javax.persistence.Column(name = "pickup_type")
    @org.springframework.data.relational.core.mapping.Column(value = "pickup_type")
    private short pickupType;
    @javax.persistence.Column(name = "dropoff_type")
    @org.springframework.data.relational.core.mapping.Column(value = "dropoff_type")
    private short dropoffType;
    @javax.persistence.Column(name = "timepoint")
    @org.springframework.data.relational.core.mapping.Column(value = "timepoint")
    private Short timepoint;
    @javax.persistence.Column(name = "dist_traveled")
    @org.springframework.data.relational.core.mapping.Column(value = "dist_traveled")
    private Float distTraveled;
    
    protected DBStopTime() {}
    
    public DBStopTime(String prefix, StopTime src) {
        this.tripId = prefix + src.getTrip().getId().getId();
        this.stopSequence = src.getStopSequence();
        this.stopId = prefix + src.getStop().getId().getId();
        this.headsign = src.getStopHeadsign();
        this.arrival = GTFSLocalTime.ofSecondOfDay(src.getArrivalTime());
        this.departure = GTFSLocalTime.ofSecondOfDay(src.getDepartureTime());
        this.pickupType = (short) src.getPickupType();
        this.dropoffType = (short) src.getDropOffType();
        if (src.isTimepointSet()) {
            this.timepoint = (short) src.getTimepoint();
        }
        if (src.isShapeDistTraveledSet()) {
            this.distTraveled = (float) src.getShapeDistTraveled();
        }
        // fare_units_traveled
    }
}
