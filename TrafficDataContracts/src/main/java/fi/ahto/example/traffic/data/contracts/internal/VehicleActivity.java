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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Jouni Ahto
 */
public class VehicleActivity implements Serializable {

    private static final long serialVersionUID = 2224272976594814200L;

    /**
     * @return the blockId
     */
    public String getBlockId() {
        return blockId;
    }

    /**
     * @param blockId the blockId to set
     */
    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.vehicleId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VehicleActivity other = (VehicleActivity) obj;
        if (!Objects.equals(this.vehicleId, other.vehicleId)) {
            return false;
        }
        return true;
    }

    public VehicleActivity() {
        onwardCalls = new TripStopSet();
    }

    public VehicleActivity(VehicleActivity other) {
        this.vehicleId = other.vehicleId;
        this.lineId = other.lineId;
        this.direction = other.direction;
        this.longitude = other.longitude;
        this.latitude = other.latitude;
        this.delay = other.delay;
        this.internalLineId = other.internalLineId;
        this.stopPoint = other.stopPoint;
        this.recordTime = other.recordTime;
        this.transitType = other.transitType;
        this.tripStart = other.tripStart;
        this.delayChange = other.delayChange;
        this.measurementLength = other.measurementLength;
        this.addToHistory = other.addToHistory;
        this.lastAddToHistory = other.lastAddToHistory;
        this.lineHasChanged = other.lineHasChanged;
        this.bearing = other.bearing;
        this.speed = other.speed;
        this.source = other.source;
        this.nextStopId = other.nextStopId;
        this.nextStopId = other.nextStopId;
        this.nextStopName = other.nextStopName;
        this.onwardCalls = other.onwardCalls;
        this.atRouteStart = other.atRouteStart;
        this.atRouteEnd = other.atRouteEnd;
        this.serviceID = other.serviceID;
        this.tripID = other.tripID;
        this.operatingDate = other.operatingDate;
        this.startTime = other.startTime;
        this.blockId = other.blockId;
    }

    public void copy(VehicleActivity other) {
        this.vehicleId = other.vehicleId;
        this.lineId = other.lineId;
        this.direction = other.direction;
        this.longitude = other.longitude;
        this.latitude = other.latitude;
        this.delay = other.delay;
        this.internalLineId = other.internalLineId;
        this.stopPoint = other.stopPoint;
        this.recordTime = other.recordTime;
        this.transitType = other.transitType;
        this.tripStart = other.tripStart;
        this.delayChange = other.delayChange;
        this.measurementLength = other.measurementLength;
        this.addToHistory = other.addToHistory;
        this.lastAddToHistory = other.lastAddToHistory;
        this.lineHasChanged = other.lineHasChanged;
        this.bearing = other.bearing;
        this.speed = other.speed;
        this.source = other.source;
        this.nextStopId = other.nextStopId;
        this.nextStopId = other.nextStopId;
        this.nextStopName = other.nextStopName;
        this.onwardCalls.addAll(other.onwardCalls);
        this.atRouteStart = other.atRouteStart;
        this.atRouteEnd = other.atRouteEnd;
        this.serviceID = other.serviceID;
        this.tripID = other.tripID;
        this.operatingDate = other.operatingDate;
        this.startTime = other.startTime;
        this.blockId = other.blockId;
    }

    public LocalDate getOperatingDate() {
        return operatingDate;
    }

    public void setOperatingDate(LocalDate operatingDate) {
        this.operatingDate = operatingDate;
    }

    public GTFSLocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(GTFSLocalTime startTime) {
        this.startTime = startTime;
    }

    public String getServiceID() {
        return serviceID;
    }

    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

    public boolean isAtRouteStart() {
        return atRouteStart;
    }

    public void setAtRouteStart(boolean atRouteStart) {
        this.atRouteStart = atRouteStart;
    }

    public boolean isAtRouteEnd() {
        return atRouteEnd;
    }

    public void setAtRouteEnd(boolean atRouteEnd) {
        this.atRouteEnd = atRouteEnd;
    }

    public String getNextStopId() {
        return nextStopId;
    }

    public void setNextStopId(String nextStopId) {
        this.nextStopId = nextStopId;
    }

    public String getNextStopName() {
        return nextStopName;
    }

    public void setNextStopName(String nextStopName) {
        this.nextStopName = nextStopName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Double getBearing() {
        return bearing;
    }

    public void setBearing(Double bearing) {
        this.bearing = bearing;
    }

    public boolean AddToHistory() {
        return addToHistory;
    }

    public void setAddToHistory(boolean addToHistory) {
        this.addToHistory = addToHistory;
    }

    public boolean LineHasChanged() {
        return lineHasChanged;
    }

    public void setLineHasChanged(boolean lineHasChanged) {
        this.lineHasChanged = lineHasChanged;
    }

    public Integer getDelayChange() {
        return delayChange;
    }

    public void setDelayChange(Integer delayChange) {
        this.delayChange = delayChange;
    }

    public Integer getMeasurementLength() {
        return measurementLength;
    }

    public void setMeasurementLength(Integer measurementLength) {
        this.measurementLength = measurementLength;
    }

    public String getStopPoint() {
        return stopPoint;
    }

    public void setStopPoint(String stopPoint) {
        this.stopPoint = stopPoint;
    }

    public Instant getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(Instant recordTime) {
        this.recordTime = recordTime;
    }

    public ZonedDateTime getTripStart() {
        return tripStart;
    }

    public void setTripStart(ZonedDateTime TripStart) {
        this.tripStart = TripStart;
    }

    public String getInternalLineId() {
        return internalLineId;
    }

    public void setInternalLineId(String internalLineId) {
        this.internalLineId = internalLineId;
    }

    public RouteType getTransitType() {
        return transitType;
    }

    public void setTransitType(RouteType transitType) {
        this.transitType = transitType;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    @JsonProperty("VehicleId")
    private String vehicleId;
    @JsonProperty("LineId")
    private String lineId;
    @JsonProperty("Direction")
    private String direction;
    @JsonProperty("Longitude")
    private Double longitude;
    @JsonProperty("Latitude")
    private Double latitude;
    @JsonProperty("Delay")
    private Integer delay;
    @JsonProperty("InternalLineId")
    private String internalLineId;
    @JsonProperty("StopPoint")
    private String stopPoint;
    @JsonProperty("RecordTime")
    private Instant recordTime;
    @JsonProperty("TransitType")
    private RouteType transitType;
    @JsonProperty("TripStart")
    private ZonedDateTime tripStart;
    @JsonProperty("DelayChange")
    private Integer delayChange;
    @JsonProperty("MeasurementLength")
    private Integer measurementLength;
    @JsonProperty("AddToHistory")
    private boolean addToHistory;
    @JsonProperty("LastAddToHistory")
    private Instant lastAddToHistory;
    @JsonProperty("LineHasChanged")
    private boolean lineHasChanged;
    @JsonProperty("Bearing")
    private Double bearing;
    @JsonProperty("Speed")
    private Double speed;
    @JsonProperty("Source")
    private String source;
    @JsonProperty("NextStopSequence")
    private Integer nextStopSequence;
    @JsonProperty("NextStopId")
    private String nextStopId;
    @JsonProperty("NextStopName")
    private String nextStopName;
    @JsonProperty("OnwardCalls")
    private final TripStopSet onwardCalls;
    @JsonProperty("AtRouteStart")
    private boolean atRouteStart;
    @JsonProperty("AtRouteEnd")
    private boolean atRouteEnd;
    @JsonProperty("ServiceID")
    private String serviceID;
    @JsonProperty("TripID")
    private String tripID;
    @JsonProperty("OperatingDate")
    private LocalDate operatingDate;
    @JsonProperty("StartTime")
    private GTFSLocalTime startTime;
    @JsonProperty("BlockId")
    private String blockId;
    private List<String> possibilities;

    public TripStopSet getOnwardCalls() {
        return onwardCalls;
    }

    /*
    public void setOnwardCalls(ServiceStopSet onwardCalls) {
        this.onwardCalls = onwardCalls;
    }
     */

    public String getTripID() {
        return tripID;
    }

    public void setTripID(String tripID) {
        this.tripID = tripID;
    }

    public Instant getLastAddedToHistory() {
        return lastAddToHistory;
    }

    public void setLastAddedToHistory(Instant lastAddToHistory) {
        this.lastAddToHistory = lastAddToHistory;
    }

    /**
     * @return the possibilities
     */
    public List<String> getServicePossibilities() {
        return possibilities;
    }

    /**
     * @param possibilities the possibilities to set
     */
    public void setServicePossibilities(List<String> possibilities) {
        this.possibilities = possibilities;
    }

    // Does it have enough data so we can fill in the missing pieces later?
    public boolean validateHasEnoughData() {
        // Must have timestamp
        if (this.recordTime == null) {
            return false;
        }
        // Must have vehicleid
        if (this.vehicleId == null || this.vehicleId.isEmpty()) {
            return false;
        }

        if (this.tripID != null && !this.tripID.isEmpty()) {
            // Should check here for frequency base trips, maybe not possible,
            // perhaps in validatePost()? Anything else can be filled later
            // from trip data.
            return true;
        }

        if (this.internalLineId != null && !this.internalLineId.isEmpty()
                && this.direction != null && !this.direction.isEmpty()
                && this.operatingDate != null && this.startTime != null) {
            return true;
        }

        return false;
    }

    // Does it have enough data so we can actually process it?
    public boolean validateAbleToProcess() {
        if (!validateHasEnoughData()) {
            return false;
        }
        return true;
    }

    public Integer getNextStopSequence() {
        return nextStopSequence;
    }

    public void setNextStopSequence(Integer nextStopSequence) {
        this.nextStopSequence = nextStopSequence;
    }
}
