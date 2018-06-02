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
import fi.ahto.example.traffic.data.contracts.internal.ServiceTrips;
import fi.ahto.example.traffic.data.contracts.internal.ServiceStopSet;
import fi.ahto.example.traffic.data.contracts.internal.StopData;
import fi.ahto.example.traffic.data.contracts.internal.TransitType;
import fi.ahto.example.traffic.data.contracts.internal.TripStop;
import fi.ahto.example.traffic.data.contracts.internal.TripStopSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jouni Ahto
 */
public class DataMapper {

    private static final Logger LOG = LoggerFactory.getLogger(DataMapper.class);

    public Map<String, RouteData> routes = new HashMap<>();
    public Map<String, StopData> stops = new HashMap<>();
    public Map<TripKey, TripStopSet> trips = new HashMap<>();
    public Map<String, ServiceData> services = new HashMap<>();
    public Map<String, ServiceTrips> servicetrips = new HashMap<>();

    private static final Map<Integer, Integer> routeFixes = new HashMap<>();

    static {
        routeFixes.put(109, 2);
        routeFixes.put(700, 3);
        routeFixes.put(701, 3);
        routeFixes.put(704, 3);
    }

    public void add(String prefix, ServiceCalendar sc) {
        ServiceData sd = services.get(prefix + sc.getServiceId().getId());
        if (sd == null) {
            LOG.warn("Service not found " + sc.getServiceId().getId());
            return;
        }

        // sd.serviceId = sc.getServiceId().getId();
        ServiceDate start = sc.getStartDate();
        ServiceDate end = sc.getEndDate();
        LocalDate validfrom = LocalDate.of(start.getYear(), start.getMonth(), start.getDay());
        LocalDate validuntil = LocalDate.of(end.getYear(), end.getMonth(), end.getDay());
        sd.validfrom = validfrom;
        sd.validuntil = validuntil;
        if (sc.getMonday() == 1) {
            sd.weekdays |= 0x1;
        }
        if (sc.getTuesday() == 1) {
            sd.weekdays |= 0x2;
        }
        if (sc.getWednesday() == 1) {
            sd.weekdays |= 0x4;
        }
        if (sc.getThursday() == 1) {
            sd.weekdays |= 0x8;
        }
        if (sc.getFriday() == 1) {
            sd.weekdays |= 0x10;
        }
        if (sc.getSaturday() == 1) {
            sd.weekdays |= 0x20;
        }
        if (sc.getSunday() == 1) {
            sd.weekdays |= 0x40;
        }
    }

    public void add(String prefix, ServiceCalendarDate sct) {
        if (sct.getExceptionType() == 1) {
            ServiceData sd = services.get(prefix + sct.getServiceId().getId());
            if (sd != null) {
                ServiceDate dt = sct.getDate();
                LocalDate notinuse = LocalDate.of(dt.getYear(), dt.getMonth(), dt.getDay());
                sd.notinuse.add(notinuse);
            }
        }
    }

    public void add(String prefix, StopTime st) {
        dataFixer(prefix, st);
        String stopid = prefix + st.getStop().getId().getId();
        String routeid = prefix + st.getTrip().getRoute().getId().getId();
        String serviceid = prefix + st.getTrip().getServiceId().getId();

        StopData stop = stops.get(stopid);
        if (stop == null) {
            stop = new StopData();
            stop.stopid = stopid;
            stop.stopname = st.getStop().getName();
            stop.latitude = st.getStop().getLat();
            stop.longitude = st.getStop().getLon();
            stop.stopcode = st.getStop().getCode();
            stop.desc = st.getStop().getDesc();
            stop.routesserved.add(routeid);
            stops.put(stopid, stop);
            LOG.debug("Added stop " + stopid);
        }
        if (stop.routesserved.contains(routeid) == false) {
            stop.routesserved.add(routeid);
        }

        RouteData route = routes.get(routeid);
        if (route == null) {
            Route rt = st.getTrip().getRoute();
            route = new RouteData();
            route.routeid = routeid;
            route.longname = rt.getLongName();
            route.shortname = rt.getShortName();
            route.type = TransitType.from(rt.getType());
            routes.put(routeid, route);
            LOG.debug("Added route " + routeid);
        }

        ServiceTrips servicetripmap = servicetrips.get(serviceid);
        if (servicetripmap == null) {
            servicetripmap = new ServiceTrips();
            servicetrips.put(serviceid, servicetripmap);
            LOG.debug("Added service " + serviceid + " to route " + routeid);
        }

        ServiceData service = services.get(serviceid);
        if (service == null) {
            service = new ServiceData();
            service.serviceId = serviceid;
            service.routeId = routeid;
            services.put(serviceid, service);
            LOG.debug("Added service " + serviceid + " to route " + routeid);
        }

        // Have to think how to handle these ones.
        String tripid = st.getTrip().getId().getId();
        TripKey trkey = new TripKey(tripid, routeid);
        TripStopSet tr = trips.get(trkey);
        if (tr == null) {
            tr = new TripStopSet();
            tr.route = routeid;
            tr.service = serviceid;
            tr.direction = st.getTrip().getDirectionId();
            trips.put(trkey, tr);
            LOG.debug("Added trip " + tripid);
        }
        
        TripStop ts = new TripStop();
        ts.stopid = stopid;
        ts.seq = st.getStopSequence();
        ts.arrivalTime = LocalTime.ofSecondOfDay(st.getArrivalTime());
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
        if ("FI:TKL:".equals(prefix)) {
            // TODO: TKL maybe needs special handling also.
        }
        if ("NO:".equals(prefix)) {
            // TODO: So could Norway...
        }
    }
}
