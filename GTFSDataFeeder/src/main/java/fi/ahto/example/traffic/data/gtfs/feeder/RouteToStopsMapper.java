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

import fi.ahto.example.traffic.data.contracts.internal.StopDataSet;
import fi.ahto.example.traffic.data.contracts.internal.StopData;
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

    Map<String, StopDataSet> forward = new HashMap<>();
    Map<String, StopDataSet> backward = new HashMap<>();
    Map<String, List<String>> routes = new HashMap<>();

    public void add(String prefix, StopTime st) {
        StopData si = new StopData();
        si.id = prefix + st.getStop().getId().getId();
        si.seq = st.getStopSequence();
        String routeid = prefix + st.getTrip().getRoute().getId().getId();
        StopDataSet set = null;
        
        if (st.getTrip().getDirectionId().equals("0")) {
            set = forward.get(routeid);
            if (set == null) {
                set = new StopDataSet();
                forward.put(routeid, set);
            }
        }
        
        if (st.getTrip().getDirectionId().equals("1")) {
            set = backward.get(routeid);
            if (set == null) {
                set = new StopDataSet();
                backward.put(routeid, set);
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
        
        List<String> list = routes.get(si.id);
        if (list == null) {
            list = new ArrayList<>();
            routes.put(si.id, list);
        }
        if (list.contains(routeid) == false) {
            list.add(routeid);
            // System.out.println("Added route " + routeid + " to stop " + si.id);
        }
    }
}
