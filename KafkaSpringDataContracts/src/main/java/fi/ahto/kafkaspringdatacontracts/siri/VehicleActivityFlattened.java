/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.kafkaspringdatacontracts.siri;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.ZonedDateTime;

/**
 *
 * @author Jouni Ahto
 */
public class VehicleActivityFlattened {

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
    
}
