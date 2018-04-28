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
import fi.ahto.example.traffic.data.contracts.internal.RouteStopSet;
import fi.ahto.example.traffic.data.contracts.internal.RouteStop;
import fi.ahto.example.traffic.data.contracts.internal.StopData;
import fi.ahto.example.traffic.data.contracts.internal.TransitType;
import java.util.HashMap;
import java.util.Map;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.StopTime;

/**
 *
 * @author Jouni Ahto
 */
public class RoutesAndStopsMapper {
    Map<String, RouteData> routes = new HashMap<>();
    // Map<String, List<String>> routesserved = new HashMap<>();
    Map<String, StopData> stops = new HashMap<>();
    // Map<String, List<String>> trips = new HashMap<>();

    public void add(String prefix, StopTime st) {
        RouteStop si = new RouteStop();
        si.stopid = prefix + st.getStop().getId().getId();
        String routeid = prefix + st.getTrip().getRoute().getId().getId();
        RouteStopSet set = null;
        
        /* Have to think how to handle these ones.
        String tripid = st.getTrip().getId().getId();
        List<String> tr = trips.get(routeid);
        if (tr == null) {
            tr = new ArrayList<>();
            trips.put(routeid, tr);
        }
        if (tr.contains(tripid) == false) {
            tr.add(tripid);
        }
        */
        
        StopData stop = stops.get(si.stopid);
        if (stop == null) {
            stop = new StopData();
            stop.stopid = si.stopid;
            stop.stopname = st.getStop().getName();
            stop.latitude = st.getStop().getLat();
            stop.longitude = st.getStop().getLon();
            stop.stopcode = st.getStop().getCode();
            stop.desc = st.getStop().getDesc();
            stop.routesserved.add(routeid);
            stops.put(si.stopid, stop);
        }
        if (stop.routesserved.contains(routeid) == false) {
            stop.routesserved.add(routeid);
        }
        /*
        List<String> list = routesserved.get(si.stopid);
        if (list == null) {
            list = new ArrayList<>();
            routesserved.put(si.stopid, list);
        }
        if (list.contains(routeid) == false) {
            list.add(routeid);
            // System.out.println("Added route " + routeid + " to stop " + si.stopid);
        }
        */
        si.seq = st.getStopSequence();

        RouteData route = routes.get(routeid);
        if (route == null) {
            Route rt = st.getTrip().getRoute();
            route = new RouteData();
            route.routeid = routeid;
            route.longname = rt.getLongName();
            route.shortname = rt.getShortName();
            route.type = TransitType.from(rt.getType());
            routes.put(routeid, route);
        }
        
        if (st.getTrip().getDirectionId().equals("0")) {
            set = route.stopsforward;
            if (set == null) {
                set = new RouteStopSet();
                route.stopsforward = set;
                route.shapesforward = prefix + st.getTrip().getShapeId().getId();
            }
        }
        
        if (st.getTrip().getDirectionId().equals("1")) {
            set = route.stopsbackward;
            if (set == null) {
                set = new RouteStopSet();
                route.stopsbackward = set;
                route.shapesbackward = prefix + st.getTrip().getShapeId().getId();
            }
        }
        
        if (set != null) {
            if (set.contains(si) == false) {
                set.add(si);
                // System.out.println("Added stop " + si.routeid + " to route " + routeid);
            }
            else {
                // System.out.println("Skipping...");
            }
        }
        
        else {
            System.out.println("Unknown direction " + st.getTrip().getDirectionId());
        }
    }
}
