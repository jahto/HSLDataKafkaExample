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

import fi.ahto.example.traffic.data.contracts.internal.RouteData;
import fi.ahto.example.traffic.data.contracts.internal.ServiceData;
import fi.ahto.example.traffic.data.contracts.internal.TripData;
import fi.ahto.example.traffic.data.contracts.internal.TripStop;
import fi.ahto.example.traffic.data.contracts.internal.TripStopSet;
import java.util.ArrayList;
import java.util.List;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;

/**
 *
 * @author Jouni Ahto
 */
public class TripExt {
    private final String key;
    private final String prefix;
    private final Trip tr;
    private final StopTimeExtSet stopTimes = new StopTimeExtSet();
    private final List<FrequencyExt> frequencies = new ArrayList<>();
    
    public TripExt(String prefix, Trip tr) {
        this.prefix = prefix;
        this.key = prefix + tr.getId().getId();
        this.tr = tr;
    }

    public void add(String prefix, StopTime st) {
        StopTimeExt stc = new StopTimeExt(prefix, st);
        this.stopTimes.add(stc);
    }

    public TripData toTripData(ServiceData sd) {
        TripData td = new TripData();
        
        td.setService(sd);
        td.setServiceId(sd.getServiceId());
        td.setBikesAllowed((short) this.getBikesAllowed());
        td.setBlockId(this.getBlockId());
        td.setDirection(this.getDirection());
        td.setHeadsign(this.getTripHeadsign());
        td.setRoute(this.getRoute());
        td.setRouteId(this.getRouteId());
        td.setShapeId(this.getShapeId());
        td.setShortName(this.getRouteShortName());
        td.setTripId(this.getKey());
        td.setWheelchairAccessible(this.getWheelchairAccessible());
        td.setStartTime(this.stopTimes.first().getArrivalTime());

        for (StopTimeExt ste : stopTimes) {
            td.getStopTimes().add(ste.toStopTimeData());
        }
        
        for (FrequencyExt fr : frequencies) {
            td.getFrequencies().add(fr.toFrequencyData());
        }
        
        return td;
    }
    
    public TripStopSet toTripStopSet() {
        TripStopSet set = new TripStopSet();
        
        for (StopTimeExt stc : stopTimes) {
            TripStop st = new TripStop();
            st.arrivalTime = stc.getArrivalTime();
            st.seq = stc.getStopSequence();
            st.stopid = stc.getStopId();
            set.add(st);
        }
        return set;
    }
    
    public String getRouteId() {
        return prefix + tr.getRoute().getId().getId();
    }

    public StopTimeExtSet getStopTimes() {
        return stopTimes;
    }

    public List<FrequencyExt> getFrequencies() {
        return frequencies;
    }

    public String getKey() {
        return key;
    }

    public RouteData getRoute() {
        RouteExt rte = new RouteExt(this.prefix, tr.getRoute());
        return rte.toRouteData();
    }

    public String getTripShortName() {
        return tr.getTripShortName();
    }

    public String getTripHeadsign() {
        return tr.getTripHeadsign();
    }

    public String getRouteShortName() {
        return tr.getRouteShortName();
    }

    public short getDirection() {
        String dir = tr.getDirectionId();
        if (dir.equals("1")) {
            return 2;
        }
        if (dir.equals("0")) {
            return 1;
        }
        return 0;
    }
    
    public String getDirectionId() {
        String dir = tr.getDirectionId();
        if (dir.equals("1")) {
            return "2";
        }
        if (dir.equals("0")) {
            return "1";
        }
        return "0";
    }

    public String getBlockId() {
        if (tr.getBlockId() != null && !tr.getBlockId().isEmpty()) {
            return prefix + tr.getBlockId();
        }
        return null;
    }

    public String getShapeId() {
        if (tr.getShapeId() != null) {
            return prefix + tr.getShapeId().getId();
        }
        return null;
    }

    public short getWheelchairAccessible() {
        return (short) tr.getWheelchairAccessible();
    }

    public short getBikesAllowed() {
        return (short) tr.getBikesAllowed();
    }
}
