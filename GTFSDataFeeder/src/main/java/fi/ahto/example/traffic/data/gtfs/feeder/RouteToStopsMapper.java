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

import fi.ahto.example.traffic.data.contracts.internal.RouteStops;
import fi.ahto.example.traffic.data.contracts.internal.RouteStopSet;
import fi.ahto.example.traffic.data.contracts.internal.RouteStop;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.onebusaway.gtfs.model.StopTime;

/**
 *
 * @author Jouni Ahto
 */
public class RouteToStopsMapper {
    Map<String, RouteStops> routes = new HashMap();
    Map<String, List<String>> routesserved = new HashMap<>();

    public void add(String prefix, StopTime st) {
        RouteStop si = new RouteStop();
        si.stopid = prefix + st.getStop().getId().getId();
        String routeid = prefix + st.getTrip().getRoute().getId().getId();
        RouteStopSet set = null;
        
        List<String> list = routesserved.get(si.stopid);
        if (list == null) {
            list = new ArrayList<>();
            routesserved.put(si.stopid, list);
        }
        if (list.contains(routeid) == false) {
            list.add(routeid);
            // System.out.println("Added route " + routeid + " to stop " + si.stopid);
        }

        si.seq = st.getStopSequence();

        RouteStops route = routes.get(routeid);
        if (route == null) {
            route = new RouteStops();
            route.routeid = routeid;
            routes.put(routeid, route);
        }
        
        if (st.getTrip().getDirectionId().equals("0")) {
            set = route.forward;
            if (set == null) {
                set = new RouteStopSet();
                route.forward = set;
            }
        }
        
        if (st.getTrip().getDirectionId().equals("1")) {
            set = route.backward;
            if (set == null) {
                set = new RouteStopSet();
                route.backward = set;
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
