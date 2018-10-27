/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.example.traffic.data.contracts.internal;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Convert;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;

/**
 *
 * @author jah
 */
public class TripComplete {

    public List<FrequencyComplete> getFrequencies() {
        return frequencies;
    }

    public TripComplete() {

    }

    public TripComplete(String prefix, StopTime st) {
        Trip tr = st.getTrip();

        // ServiceId not needed here!
        // TripId not needed here!
        // String tripid = prefix + tr.getId().getId();
        // this.tripId = tripid;
        String routeid = prefix + tr.getRoute().getId().getId();
        this.routeId = routeid;

        this.headsign = tr.getTripHeadsign();
        this.shortName = tr.getTripShortName();

        String dir = tr.getDirectionId();
        if (dir.equals("1")) {
            this.direction = 2;
        } else if (dir.equals("0")) {
            this.direction = 1;
        } else {
            this.direction = 0;
        }

        if (tr.getBlockId() != null) {
            this.blockId = prefix + tr.getBlockId(); // + ":" + routeid; // + ":" + dir;
        }
        if (tr.getShapeId() != null) {
            this.shapeId = prefix + tr.getShapeId().getId();
        }

        this.wheelchairAccessible = (short) tr.getWheelchairAccessible();
        this.bikesAllowed = (short) tr.getBikesAllowed();

        this.add(prefix, st);
    }

    public void add(String prefix, StopTime st) {
        StopTimeComplete stc = new StopTimeComplete(prefix, st);
        this.stopTimes.add(stc);
    }

    public void add(String prefix, FrequencyComplete fr) {

    }

    private String routeId;

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    /*
        public String getTripId() {
            return tripId;
        }

        public void setTripId(String tripId) {
            this.tripId = tripId;
        }
     */
    public String getHeadsign() {
        return headsign;
    }

    public void setHeadsign(String headsign) {
        this.headsign = headsign;
    }

    public short getDirection() {
        return direction;
    }

    public void setDirection(short direction) {
        this.direction = direction;
    }

    public String getShapeId() {
        return shapeId;
    }

    public void setShapeId(String shapeId) {
        this.shapeId = shapeId;
    }

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public short getWheelchairAccessible() {
        return wheelchairAccessible;
    }

    public void setWheelchairAccessible(short wheelchairAccessible) {
        this.wheelchairAccessible = wheelchairAccessible;
    }

    public short getBikesAllowed() {
        return bikesAllowed;
    }

    public void setBikesAllowed(short bikesAllowed) {
        this.bikesAllowed = bikesAllowed;
    }

    // private String serviceId;
    // private String tripId;
    private String headsign;
    private short direction;
    private String shapeId;
    private String blockId;
    private short wheelchairAccessible;
    private short bikesAllowed;
    private String shortName;

    public List<StopTimeComplete> stopTimes = new ArrayList<>();

    public List<StopTimeComplete> getStopTimes() {
        return stopTimes;
    }
    private final List<FrequencyComplete> frequencies = new ArrayList<>();


}