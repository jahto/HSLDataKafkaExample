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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 *
 * @author Jouni Ahto
 */
public class VehicleHistoryRecord {

    public VehicleHistoryRecord() { }
    
    public VehicleHistoryRecord(VehicleActivity other) {
        this.vehicleId = other.getVehicleId();
        this.lineId = other.getLineId();
        this.direction = other.getDirection();
        this.longitude = other.getLongitude();
        this.latitude = other.getLatitude();
        this.delay = other.getDelay();
        this.stopPoint = other.getStopPoint();
        this.recordTime = other.getRecordTime();
        this.transitType = other.getTransitType();
        // this.delayChange = other.delayChange;
        // this.measurementLength = other.measurementLength;
        this.bearing = other.getBearing();
        this.speed = other.getSpeed();
        this.nextStopId = other.getNextStopId();
        this.nextStopName = other.getNextStopName();
        this.atRouteStart = other.isAtRouteStart();
        this.atRouteEnd = other.isAtRouteEnd();
        this.operatingDate = other.getOperatingDate();
        this.startTime = other.getStartTime();
    }

    public LocalDate getOperatingDate() {
        return operatingDate;
    }

    public void setOperatingDate(LocalDate operatingDate) {
        this.operatingDate = operatingDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
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

    /*
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
    */
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

    public void setTransitType(TransitType transitType) {
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
    @JsonProperty("StopPoint")
    private String stopPoint;
    @JsonProperty("RecordTime")
    private Instant recordTime;
    @JsonProperty("TransitType")
    private TransitType transitType;
    /*
    @JsonProperty("DelayChange")
    private Integer delayChange;
    @JsonProperty("MeasurementLength")
    private Integer measurementLength;
    */
    @JsonProperty("Bearing")
    private Double bearing;
    @JsonProperty("Speed")
    private Double speed;
    @JsonProperty("NextStopId")
    private String nextStopId;
    @JsonProperty("NextStopName")
    private String nextStopName;
    @JsonProperty("AtRouteStart")
    private boolean atRouteStart;
    @JsonProperty("AtRouteEnd")
    private boolean atRouteEnd;
    @JsonProperty("OperatingDate")
    private LocalDate operatingDate;
    @JsonProperty("StartTime")
    private LocalTime startTime;
}
