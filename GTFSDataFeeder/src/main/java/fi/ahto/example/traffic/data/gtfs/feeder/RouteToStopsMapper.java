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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;

/**
 *
 * @author Jouni Ahto
 */
public class RouteToStopsMapper {

    Map<String, StopSet> forward = new HashMap<>();
    Map<String, StopSet> backward = new HashMap<>();
    Map<String, List<String>> routes = new HashMap<>();

    class StopInfo {
        String id;
        int seq;
        Stop stop;

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 23 * hash + Objects.hashCode(this.id);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final StopInfo other = (StopInfo) obj;
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            return true;
        }
    }

    class StopSet extends TreeSet<StopInfo> {

        public StopSet() {
            super((StopInfo o1, StopInfo o2) -> Integer.compare(o1.seq, o2.seq));
        }
    }

    public void add(String prefix, StopTime st) {
        StopInfo si = new StopInfo();
        si.id = prefix + st.getStop().getId().getId();
        si.seq = st.getStopSequence();
        si.stop = st.getStop();
        String routeid = prefix + st.getTrip().getRoute().getId().getId();
        StopSet set = null;
        
        if (st.getTrip().getDirectionId().equals("0")) {
            set = forward.get(routeid);
            if (set == null) {
                set = new StopSet();
                forward.put(routeid, set);
            }
        }
        
        if (st.getTrip().getDirectionId().equals("1")) {
            set = backward.get(routeid);
            if (set == null) {
                set = new StopSet();
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
