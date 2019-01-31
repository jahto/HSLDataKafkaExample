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
package fi.ahto.example.traffic.data.contracts.internal;

import fi.ahto.example.traffic.data.contracts.utils.GTFSLocalTimeConverter;
import java.io.Serializable;
import javax.persistence.Convert;

/**
 *
 * @author Jouni Ahto
 */
public class StopTimeData implements Serializable {

    public StopTimeData() {}

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

    public String getHeadsign() {
        return headsign;
    }

    public void setHeadsign(String headsign) {
        this.headsign = headsign;
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

    public short getDropoffType() {
        return dropoffType;
    }

    public void setDropoffType(short dropoffType) {
        this.dropoffType = dropoffType;
    }

    public short getTimepoint() {
        return timepoint;
    }

    public void setTimepoint(short timepoint) {
        this.timepoint = timepoint;
    }

    public Double getDistTraveled() {
        return distTraveled;
    }

    public void setDistTraveled(Double distTraveled) {
        this.distTraveled = distTraveled;
    }

    public StopData getStop() {
        return stop;
    }

    public void setStop(StopData stop) {
        this.stop = stop;
    }

    private int stopSequence;
    private String stopId;
    private String headsign;
    @Convert(converter = GTFSLocalTimeConverter.class)
    private GTFSLocalTime arrival;
    @Convert(converter = GTFSLocalTimeConverter.class)
    private GTFSLocalTime departure;
    private short pickupType = 0;
    private short dropoffType = 0;
    private short timepoint = 1;
    private Double distTraveled;
    private StopData stop;
}
