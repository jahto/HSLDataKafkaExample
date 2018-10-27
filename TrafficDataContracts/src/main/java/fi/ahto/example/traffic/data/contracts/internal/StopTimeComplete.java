/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.example.traffic.data.contracts.internal;

import javax.persistence.Convert;
import org.onebusaway.gtfs.model.StopTime;

/**
 *
 * @author jah
 */
public class StopTimeComplete {

    public StopTimeComplete() {
    }

    public StopTimeComplete(String prefix, StopTime st) {
        this.arrival = GTFSLocalTime.ofSecondOfDay(st.getArrivalTime());
        this.departure = GTFSLocalTime.ofSecondOfDay(st.getDepartureTime());
        this.dropoffType = (short) st.getDropOffType();
        this.headsign = st.getStopHeadsign();
        this.pickupType = (short) st.getPickupType();
        this.stopId = prefix + ":" + st.getStop().getId().getId();
        this.stopSequence = st.getStopSequence();
        if (st.isTimepointSet()) {
            this.timepoint = (short) st.getTimepoint();
        }
        if (st.isShapeDistTraveledSet()) {
            this.distTraveled = st.getShapeDistTraveled();
        }

        // TripId not needed here!
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
}
