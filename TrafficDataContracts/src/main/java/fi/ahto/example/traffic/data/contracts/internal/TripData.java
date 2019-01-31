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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jouni Ahto
 */
public class TripData implements Serializable {
    public TripData() {}

    public RouteData getRoute() {
        return route;
    }

    public void setRoute(RouteData route) {
        this.route = route;
    }

    public ServiceData getService() {
        return service;
    }

    public void setService(ServiceData service) {
        this.service = service;
    }


    public void add(StopTimeData st) {
        this.stopTimes.add(st);
    }

    public void add(FrequencyData fr) {
        this.frequencies.add(fr);
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

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

    public GTFSLocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(GTFSLocalTime startTime) {
        this.startTime = startTime;
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

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @org.springframework.data.annotation.Id
    private String tripId;
    private String serviceId;
    private String routeId;
    private String headsign;
    private short direction;
    private GTFSLocalTime startTime;
    private String shapeId;
    private String blockId;
    private short wheelchairAccessible;
    private short bikesAllowed;
    private String shortName;
    private RouteData route;
    private ServiceData service;

    public final StopTimeDataSet stopTimes = new StopTimeDataSet();

    public StopTimeDataSet getStopTimes() {
        return stopTimes;
    }
    private final List<FrequencyData> frequencies = new ArrayList<>();

    public List<FrequencyData> getFrequencies() {
        return frequencies;
    }

    public TripStopSet toTripStopSet() {
        TripStopSet set = new TripStopSet();
        
        for (StopTimeData stc : stopTimes) {
            TripStop st = new TripStop();
            st.arrivalTime = stc.getArrival();
            st.seq = stc.getStopSequence();
            st.stopid = stc.getStopId();
            set.add(st);
        }
        return set;
    }
}
