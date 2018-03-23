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
package fi.ahto.example.hsl.data.contracts.siri;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.ZonedDateTime;

/**
 *
 * @author Jouni Ahto
 */
public class VehicleActivityFlattened {

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

    public String getJoreCode() {
        return joreCode;
    }

    public void setJoreCode(String joreCode) {
        this.joreCode = joreCode;
    }

    public TransitType getTransitType() {
        return transitType;
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
    @JsonProperty("JoreCode")
    private String joreCode;
    @JsonProperty("StopPoint")
    private String stopPoint;
    @JsonProperty("RecordTime")
    private Instant recordTime;
    @JsonProperty("TransitType")
    private TransitType transitType;
    @JsonProperty("TripStart")
    private ZonedDateTime tripStart;
    @JsonProperty("DelayChange")
    private Integer delayChange;
    @JsonProperty("MeasurementLength")
    private Integer measurementLength;
    
}
