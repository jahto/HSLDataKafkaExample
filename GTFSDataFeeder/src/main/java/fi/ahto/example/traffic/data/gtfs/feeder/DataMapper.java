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

import java.util.HashMap;
import java.util.Map;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.StopTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jouni Ahto
 */
public class DataMapper {

    private static final Logger LOG = LoggerFactory.getLogger(DataMapper.class);
    public Map<String, ServiceCalendarExt> services = new HashMap<>();

    public void add(String prefix, ServiceCalendar sc) {
        String key = prefix + sc.getServiceId().getId();
        ServiceCalendarExt sd = services.get(key);
        if (sd == null) {
            sd = new ServiceCalendarExt(prefix, sc);
            services.put(key, sd);
        } else {
            sd.add(prefix, sc);
        }
    }

    public void add(String prefix, ServiceCalendarDate sct) {
        String key = prefix + sct.getServiceId().getId();
        ServiceCalendarExt sd = services.get(key);
        if (sd == null) {
            sd = new ServiceCalendarExt(prefix, sct);
            services.put(key, sd);
        } else {
            sd.add(prefix, sct);
        }
    }

    public void add(String prefix, Frequency freq) {
        String key = prefix + freq.getTrip().getServiceId().getId();
        ServiceCalendarExt scde = services.get(key);
        if (scde != null) {
            String tkey = prefix + freq.getTrip().getId().getId();
            TripExt tc = scde.getTrip(tkey);
            if (tc != null) {
                tc.getFrequencies().add(new FrequencyExt(freq));
            }
        }

    }

    public void add(String prefix, StopTime st) {
        dataFixer(prefix, st);
        String serviceid = prefix + st.getTrip().getServiceId().getId(); // + ":" + routeid;

        ServiceCalendarExt sce = services.get(serviceid);
        if (sce == null) {
            sce = new ServiceCalendarExt(prefix, st);
            services.put(serviceid, sce);
        } else {
            sce.add(prefix, st);
        }
        
        /*
        String dir = st.getTrip().getDirectionId();
        if (dir.equals("1")) {
            dir = "2";
        }
        if (dir.equals("0")) {
            dir = "1";
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

        if (stop.routesserved.contains(routeid) == false) {
            stop.routesserved.add(routeid);
        }
        */
    }

    // Fix some observed anomalies or deviations from the standard.
    private void dataFixer(String prefix, StopTime st) {

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
