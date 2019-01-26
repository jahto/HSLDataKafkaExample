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
package fi.ahto.example.traffic.data.gtfs.feeder;

import fi.ahto.example.traffic.data.contracts.internal.GTFSLocalTime;
import fi.ahto.example.traffic.data.contracts.internal.StopData;
import fi.ahto.example.traffic.data.contracts.internal.StopTimeData;
import org.onebusaway.gtfs.model.StopTime;

/**
 *
 * @author Jouni Ahto
 */
public class StopTimeExt {

    private final String prefix;
    private final StopTime st;

    public StopTimeExt(String prefix, StopTime st) {
        this.prefix = prefix;
        this.st = st;
    }

    public StopTimeData toStopTimeData() {
        StopTimeData std = new StopTimeData();

        std.setArrival(this.getArrivalTime());
        std.setDeparture(this.getDepartureTime());
        std.setDistTraveled(this.getShapeDistTraveled());
        std.setDropoffType(this.getDropOffType());
        std.setHeadsign(this.getStopHeadsign());
        std.setPickupType(this.getPickupType());
        std.setStop(this.getStop());
        std.setStopId(this.getStopId());
        std.setStopSequence(this.getStopSequence());
        std.setTimepoint(this.getTimepoint());
        return std;
    }

    public short getStopSequence() {
        return (short) st.getStopSequence();
    }

    public String getStopId() {
        return prefix + st.getStop().getId().getId();
    }

    public StopData getStop() {
        StopExt ste = new StopExt(prefix, st.getStop());
        return ste.toStopData();
    }

    public boolean isArrivalTimeSet() {
        return st.isArrivalTimeSet();
    }

    public GTFSLocalTime getArrivalTime() {
        return GTFSLocalTime.ofSecondOfDay(st.getArrivalTime());
    }

    public boolean isDepartureTimeSet() {
        return st.isDepartureTimeSet();
    }

    public GTFSLocalTime getDepartureTime() {
        return GTFSLocalTime.ofSecondOfDay(st.getDepartureTime());
    }

    public boolean isTimepointSet() {
        return st.isTimepointSet();
    }

    public short getTimepoint() {
        if (st.isTimepointSet()) {
            return (short) st.getTimepoint();
        }
        return 1;
    }

    public String getStopHeadsign() {
        return st.getStopHeadsign();
    }

    public String getRouteShortName() {
        return st.getRouteShortName();
    }

    public short getPickupType() {
        return (short) st.getPickupType();
    }

    public short getDropOffType() {
        return (short) st.getDropOffType();
    }

    public boolean isShapeDistTraveledSet() {
        return st.isShapeDistTraveledSet();
    }

    public Double getShapeDistTraveled() {
        if (st.isShapeDistTraveledSet()) {
            return st.getShapeDistTraveled();
        }
        return null;
    }
}
