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

    @Column(name = "trip_id")
    private String tripId;
    @Column(name = "stop_sequence")
    private int stopSequence;
    @Column(name = "stop_id")
    private String stopId;
    @Column(name = "headsign")
    private String headsign;
    @Column(name = "arrival")
    @Convert(converter = GTFSLocalTimeConverter.class)
    private GTFSLocalTime arrival;
    @Column(name = "departure")
    @Convert(converter = GTFSLocalTimeConverter.class)
    private GTFSLocalTime departure;
    @Column(name = "pickup_type")
    private short pickupType;
    @Column(name = "dropoff_type")
    private short dropoffType;
    @Column(name = "timepoint")
    private short timepoint;
    @Column(name = "dist_traveled")
    private float distTraveled;
    
    protected DBStopTimeBase() {}
    
    public DBStopTimeBase(String prefix, StopTime src) {
        this.tripId = prefix + src.getTrip().getId().getId();
        this.stopSequence = src.getStopSequence();
        this.stopId = prefix + src.getStop().getId().getId();
        this.headsign = src.getStopHeadsign();
        this.arrival = GTFSLocalTime.ofSecondOfDay(src.getArrivalTime());
        this.departure = GTFSLocalTime.ofSecondOfDay(src.getDepartureTime());
        this.pickupType = (short) src.getPickupType();
        this.dropoffType = (short) src.getDropOffType();
        this.timepoint = (short) src.getTimepoint();
        this.distTraveled = (float) src.getShapeDistTraveled();
        // fare_units_traveled
    }

    //@Column(name = "trip_id")
    public String getTripId() {
        return tripId;
    }
    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    //@Column(name = "stop_sequence")
    public int getStopSequence() {
        return stopSequence;
    }
    public void setStopSequence(int stopSequence) {
        this.stopSequence = stopSequence;
    }

    //@Column(name = "stop_id")
    public String getStopId() {
        return stopId;
    }
    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    //@Column(name = "headsign")
    public String getHeadsign() {
        return headsign;
    }
    public void setHeadsign(String headsign) {
        this.headsign = headsign;
    }

    //@Column(name = "arrival")
    //@Convert(converter = GTFSLocalTimeConverter.class)
    public GTFSLocalTime getArrival() {
        return arrival;
    }
    public void setArrival(GTFSLocalTime arrival) {
        this.arrival = arrival;
    }

    //@Column(name = "departure")
    //@Convert(converter = GTFSLocalTimeConverter.class)
    public GTFSLocalTime getDeparture() {
        return departure;
    }
    public void setDeparture(GTFSLocalTime departure) {
        this.departure = departure;
    }

    //@Column(name = "pickup_type")
    public short getPickupType() {
        return pickupType;
    }
    public void setPickupType(short pickupType) {
        this.pickupType = pickupType;
    }

    //@Column(name = "dropoff_type")
    public short getDropoffType() {
        return dropoffType;
    }
    public void setDropoffType(short dropoffType) {
        this.dropoffType = dropoffType;
    }

    //@Column(name = "timepoint")
    public short getTimepoint() {
        return timepoint;
    }
    public void setTimepoint(short timepoint) {
        this.timepoint = timepoint;
    }

    //@Column(name = "dist_traveled")
    public float getDistTraveled() {
        return distTraveled;
    }
    public void setDistTraveled(float distTraveled) {
        this.distTraveled = distTraveled;
    }
}
