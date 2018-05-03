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
import fi.ahto.example.traffic.data.contracts.internal.TripStop;
import fi.ahto.example.traffic.data.contracts.internal.TripStopSet;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.StopTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jouni Ahto
 */
public class RoutesAndStopsMapper {

    private static final Logger LOG = LoggerFactory.getLogger(RoutesAndStopsMapper.class);

    public Map<String, RouteData> routes = new HashMap<>();
    public Map<String, StopData> stops = new HashMap<>();
    public Map<TripKey, TripStopSet> trips = new HashMap<>();
    public Map<String, HashSet<String>> guesses = new HashMap<>();

    private static final Map<Integer, Integer> routeFixes = new HashMap<>();

    static {
        routeFixes.put(109, 2);
        routeFixes.put(700, 3);
        routeFixes.put(701, 3);
        routeFixes.put(704, 3);
    }

    public void add(String prefix, StopTime st) {
        dataFixer(prefix, st);
        RouteStop rs = new RouteStop();
        TripStop ts = new TripStop();
        rs.stopid = prefix + st.getStop().getId().getId();
        ts.stopid = prefix + st.getStop().getId().getId();

        String routeid = prefix + st.getTrip().getRoute().getId().getId();
        RouteStopSet set = null;

        StopData stop = stops.get(rs.stopid);
        if (stop == null) {
            stop = new StopData();
            stop.stopid = rs.stopid;
            stop.stopname = st.getStop().getName();
            stop.latitude = st.getStop().getLat();
            stop.longitude = st.getStop().getLon();
            stop.stopcode = st.getStop().getCode();
            stop.desc = st.getStop().getDesc();
            stop.routesserved.add(routeid);
            stops.put(rs.stopid, stop);
        }
        if (stop.routesserved.contains(routeid) == false) {
            stop.routesserved.add(routeid);
        }

        rs.seq = st.getStopSequence();
        rs.name = st.getStop().getName();
        ts.seq = st.getStopSequence();
        // ts.name = st.getStop().getName();

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
            if (set.contains(rs) == false) {
                set.add(rs);
                // System.out.println("Added stop " + rs.routeid + " to route " + routeid);
            } else {
                // System.out.println("Skipping...");
            }
        } else {
            LOG.warn("Unknown direction " + st.getTrip().getDirectionId());
        }

        // Have to think how to handle these ones.
        String tripid = st.getTrip().getId().getId();
        TripKey trkey = new TripKey(tripid, routeid);
        TripStopSet tr = trips.get(trkey);
        if (tr == null) {
            tr = new TripStopSet();
            trips.put(trkey, tr);
            LOG.info("Added trip " + tripid);
        }
        ts.arrivalTime = st.getArrivalTime();
        if (tr.contains(ts) == false) {
            tr.add(ts);
        }

    }

    // Fix some observed anomalies or deviations from the standard.
    private void dataFixer(String prefix, StopTime st) {
        int start = st.getArrivalTime();
        if (start > 86399) {
            start -= 86400;
            st.setArrivalTime(start);
        }

        Route route = st.getTrip().getRoute();
        // Unknown codes in HSL data
        if ("FI:HSL:".equals(prefix)) {
            route.setType(routeFixes.getOrDefault(route.getType(), route.getType()));
        }
        // FOLI realtime feed identifies the line with shortname, not with the route id in routes.txt
        if ("FI:FOLI:".equals(prefix)) {
            route.getId().setId(route.getShortName());
        }
        // HSL real time feed does currently not contain trip information.
        // Make a map of possible alternatives and try to guess the right one...
        if ("FI:HSL:".equals(prefix)) {
            try {
                if (st.getStopSequence() == 1 && st.getTimepoint() == 1) {
                    String dir = st.getTrip().getDirectionId();
                    if ("1".equals(dir)) {
                        dir = "2";
                    }
                    if ("0".equals(dir)) {
                        dir = "1";
                    }
                    String routeid = route.getId().getId();
                    String stopid = st.getStop().getId().getId();

                    LocalTime lt = LocalTime.ofSecondOfDay(st.getArrivalTime());

                    String namefinal = routeid + "_" + stopid + "_" + dir + "_" + lt.toString().replace(":", "");

                    HashSet<String> set = guesses.get(namefinal);
                    if (set == null) {
                        set = new HashSet<>();
                        guesses.put(namefinal, set);
                    }

                    String tripid = st.getTrip().getId().getId();

                    if (set.contains(tripid) == false) {
                        set.add(tripid);
                    }
                }
            } catch (Exception e) {
                LOG.error(prefix, e);
            }
        }
        if ("FI:TKL:".equals(prefix)) {
            // TODO: TKL need special handling also.
        }
        if ("NO:".equals(prefix)) {
            // TODO: So does Norway...
        }
    }
}
