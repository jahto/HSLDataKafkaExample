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

import com.sangupta.murmur.Murmur2;
import fi.ahto.example.traffic.data.contracts.internal.GTFSLocalTime;
import fi.ahto.example.traffic.data.contracts.internal.RouteData;
import fi.ahto.example.traffic.data.contracts.internal.ServiceList;
import fi.ahto.example.traffic.data.contracts.internal.ServiceTrips;
import fi.ahto.example.traffic.data.contracts.internal.StopData;
import fi.ahto.example.traffic.data.contracts.internal.RouteType;
import fi.ahto.example.traffic.data.contracts.internal.TripStop;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.onebusaway.gtfs.model.Frequency;
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
    public Map<String, TripStopSetExt> trips = new HashMap<>();
    public Map<String, ServiceDataExt> services = new HashMap<>();
    public Map<String, ServiceTrips> servicetrips = new HashMap<>();
    public Map<String, ServiceTrips> blocks = new HashMap<>();

    final Map<String, ServiceList> routeservices = new HashMap<>();

    public void add(String prefix, ServiceCalendar sc) {
        String key = prefix + sc.getServiceId().getId();
        ServiceDataExt sd = services.get(key);
        if (sd == null) {
            sd = new ServiceDataExt();
            sd.serviceId = key;
            services.put(key, sd);
        }

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
        String key = prefix + sct.getServiceId().getId();
        ServiceDataExt sd = services.get(key);
        if (sd == null) {
            sd = new ServiceDataExt();
            sd.serviceId = key;
            services.put(key, sd);
        }

        ServiceDate sdt = sct.getDate();
        LocalDate dt = LocalDate.of(sdt.getYear(), sdt.getMonth(), sdt.getDay());
        if (sct.getExceptionType() == 2) {
            sd.notinuse.add(dt);
        }
        if (sct.getExceptionType() == 1) {
            sd.inuse.add(dt);
        }
    }

    public void add(String prefix, Frequency freq) {
        int i = 0;
    }

    public void add(String prefix, StopTime st) {
        dataFixer(prefix, st);
        String stopid = prefix + st.getStop().getId().getId();
        String routeid = prefix + st.getTrip().getRoute().getId().getId();
        String serviceid = prefix + st.getTrip().getServiceId().getId(); // + ":" + routeid;
        String tripid = prefix + st.getTrip().getId().getId();

        String dir = st.getTrip().getDirectionId();
        if (dir.equals("1")) {
            dir = "2";
        }
        if (dir.equals("0")) {
            dir = "1";
        }

        String servicetripid = serviceid + ":" + routeid + ":" + dir;
        String blockid = null;
        if (st.getTrip().getBlockId() != null) {
            blockid = prefix + st.getTrip().getBlockId() + ":" + routeid; // + ":" + dir;
        }
        //serviceid = compressedId(serviceid);
        //tripid = compressedId(tripid);

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
            route.type = RouteType.from(rt.getType());
            routes.put(routeid, route);
            LOG.debug("Added route " + routeid);
        }
        
        RouteData.RouteStop rst = new RouteData.RouteStop(stopid);
        if (route.stops.contains(rst) == false) {
            route.stops.add(rst);
        }
        
        ServiceTrips servicetripmap = servicetrips.get(servicetripid);
        if (servicetripmap == null) {
            servicetripmap = new ServiceTrips();
            servicetripmap.route = routeid;
            servicetrips.put(servicetripid, servicetripmap);
            LOG.debug("Added servicetrip " + servicetripid + " to route " + routeid);

            // Dirty hack for TKL, so we get something that can be found
            // in the maps, but will finally not match.
            String servicetripidopposite = serviceid + ":" + routeid + ":";
            if (dir.equals("1")) {
                servicetripidopposite += "2";
            }
            if (dir.equals("2")) {
                servicetripidopposite += "1";
            }
            ServiceTrips servicetripmapopposite = servicetrips.get(servicetripidopposite);
            if (servicetripmapopposite == null) {
                servicetripmapopposite = new ServiceTrips();
                servicetripmapopposite.route = routeid;
                servicetrips.put(servicetripidopposite, servicetripmapopposite);
                LOG.debug("Added servicetrip " + servicetripidopposite + " to route " + routeid);
            }
        }
        if (blockid != null) {
            ServiceTrips blocktripmap = servicetrips.get(blockid);
            if (blocktripmap == null) {
                blocktripmap = new ServiceTrips();
                blocktripmap.route = routeid;
                servicetrips.put(blockid, blocktripmap);
                LOG.debug("Added block " + blockid + " to route " + routeid);
            }
        }

        ServiceDataExt service = services.get(serviceid);
        if (service == null) {
            service = new ServiceDataExt();
            service.serviceId = serviceid;
            services.put(serviceid, service);
            LOG.debug("Added service " + serviceid);
        }
        if (service.routeIds.contains(routeid) == false) {
            service.routeIds.add(routeid);
        }
        if (blockid != null) {
            if (service.blockIds.contains(blockid) == false) {
                service.blockIds.add(blockid);
            }
        }

        // Need to know later if the service is in one direction only.
        if (dir.equals("1")) {
            service.extra |= 0x1;
        }
        if (dir.equals("2")) {
            service.extra |= 0x2;
        }
        // Have to think how to handle these ones.
        TripStopSetExt tr = trips.get(tripid);
        if (tr == null) {
            tr = new TripStopSetExt();
            tr.route = routeid;
            tr.service = serviceid;
            tr.direction = dir;
            tr.block = blockid;
            trips.put(tripid, tr);
            LOG.debug("Added trip " + tripid);
        }

        TripStop ts = new TripStop();
        ts.stopid = stopid;
        ts.seq = st.getStopSequence();
        ts.arrivalTime = GTFSLocalTime.ofSecondOfDay(st.getArrivalTime());
        if (tr.contains(ts) == false) {
            tr.add(ts);
        }
    }
    
    // Fix some observed anomalies or deviations from the standard.

    private void dataFixer(String prefix, StopTime st) {
        /* Not needed anymore after switcing to use GTFSLocalTime.
        int start = st.getArrivalTime();
        if (start > 86399) {
            start -= 86400;
            st.setArrivalTime(start);
        }
        */
        Route route = st.getTrip().getRoute();
        if ("FI:HSL:".equals(prefix)) {
        }
        // FOLI realtime feed identifies the line with shortname, not with the route id in routes.txt
        if ("FI:FOLI:".equals(prefix)) {
            route.getId().setId(route.getShortName());
        }
        if ("FI:TKL:".equals(prefix)) {
        }
        if ("NO:".equals(prefix)) {
        }
        if ("DE:VBB".equals(prefix)) {
        }
        if ("HU:BKK".equals(prefix)) {
        }
    }
}
