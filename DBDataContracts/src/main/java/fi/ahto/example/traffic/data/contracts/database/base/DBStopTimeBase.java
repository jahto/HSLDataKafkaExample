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
import org.onebusaway.gtfs.model.StopTime;

/**
 *
 * @author Jouni Ahto
 */
@MappedSuperclass
public class DBStopTimeBase implements Serializable {
    
    private static final long serialVersionUID = -175539650314949501L;

    private String tripId;
    private int stopSequence;
    private String stopId;
    private String headSign;
    @Convert(converter = GTFSLocalTimeConverter.class)
    private GTFSLocalTime arrival;
    @Convert(converter = GTFSLocalTimeConverter.class)
    private GTFSLocalTime departure;
    private short pickupType;
    private short dropOffType;
    private short timepoint;
    private float distTraveled;
    
    protected DBStopTimeBase() {}
    
    public DBStopTimeBase(String prefix, StopTime src) {
        this.tripId = prefix + src.getTrip().getId().getId();
        this.stopSequence = src.getStopSequence();
        this.stopId = prefix + src.getStop().getId().getId();
        this.headSign = src.getStopHeadsign();
        this.arrival = new GTFSLocalTime(src.getArrivalTime());
        this.departure = new GTFSLocalTime(src.getDepartureTime());
        this.pickupType = (short) src.getPickupType();
        this.dropOffType = (short) src.getDropOffType();
        this.timepoint = (short) src.getTimepoint();
        this.distTraveled = (float) src.getShapeDistTraveled();
        // fare_units_traveled
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public int getStopSequence() {
        return stopSequence;
    }

    public void setStopSequence(int stopSequence) {
        this.stopSequence = stopSequence;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getHeadSign() {
        return headSign;
    }

    public void setHeadSign(String headSign) {
        this.headSign = headSign;
    }

    public GTFSLocalTime getArrival() {
        return arrival;
    }

    public void setArrival(GTFSLocalTime arrival) {
        this.arrival = arrival;
    }

    public GTFSLocalTime getDeparture() {
        return departure;
    }

    public void setDeparture(GTFSLocalTime departure) {
        this.departure = departure;
    }

    public short getPickupType() {
        return pickupType;
    }

    public void setPickupType(short pickupType) {
        this.pickupType = pickupType;
    }

    public short getDropOffType() {
        return dropOffType;
    }

    public void setDropOffType(short dropOffType) {
        this.dropOffType = dropOffType;
    }

    public short getTimepoint() {
        return timepoint;
    }

    public void setTimepoint(short timepoint) {
        this.timepoint = timepoint;
    }

    public float getDistTraveled() {
        return distTraveled;
    }

    public void setDistTraveled(float distTraveled) {
        this.distTraveled = distTraveled;
    }
}
