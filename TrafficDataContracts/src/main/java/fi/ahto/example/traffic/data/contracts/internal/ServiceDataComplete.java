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
package fi.ahto.example.traffic.data.contracts.internal;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Convert;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.springframework.data.annotation.Id;

/**
 *
 * @author Jouni Ahto
 */
public class ServiceDataComplete {

    public ServiceDataComplete() {
    }

    public ServiceDataComplete(String prefix, ServiceCalendar st) {
        String serviceid = prefix + st.getServiceId().getId();
        this.serviceId = serviceid;
        this.add(prefix, st);
    }

    public ServiceDataComplete(String prefix, ServiceCalendarDate st) {
        String serviceid = prefix + st.getServiceId().getId();
        this.serviceId = serviceid;
        this.add(prefix, st);
    }
    
    public ServiceDataComplete(String prefix, StopTime st) {
        String serviceid = prefix + st.getTrip().getServiceId().getId();
        this.serviceId = serviceid;
        this.add(prefix, st);
    }

    public void add(String prefix, StopTime st) {
        String routeid = prefix + st.getTrip().getRoute().getId().getId();
        String tripid = prefix + st.getTrip().getId().getId();

        TripComplete ti = this.getTrip(tripid);
        if (ti == null) {
            ti = new TripComplete(prefix, st);
            this.getTrips().add(ti);
        } else {
            ti.add(prefix, st);
        }
        if (this.routes.contains(routeid) == false) {
            this.routes.add(routeid);
        }
    }

    public void add(String prefix, ServiceCalendar sc) {
        ServiceDate start = sc.getStartDate();
        ServiceDate end = sc.getEndDate();
        LocalDate validfrom = LocalDate.of(start.getYear(), start.getMonth(), start.getDay());
        LocalDate validuntil = LocalDate.of(end.getYear(), end.getMonth(), end.getDay());
        this.validFrom = validfrom;
        this.validUntil = validuntil;
        if (sc.getMonday() == 1) {
            this.monday = true;
            this.weekdays |= 0x1;
        }
        if (sc.getTuesday() == 1) {
            this.tuesday = true;
            this.weekdays |= 0x2;
        }
        if (sc.getWednesday() == 1) {
            this.wednesday = true;
            this.weekdays |= 0x4;
        }
        if (sc.getThursday() == 1) {
            this.thursday = true;
            this.weekdays |= 0x8;
        }
        if (sc.getFriday() == 1) {
            this.friday = true;
            this.weekdays |= 0x10;
        }
        if (sc.getSaturday() == 1) {
            this.saturday = true;
            this.weekdays |= 0x20;
        }
        if (sc.getSunday() == 1) {
            this.sunday = true;
            this.weekdays |= 0x40;
        }
    }

    public void add(String prefix, ServiceCalendarDate sct) {
        ServiceDate sdt = sct.getDate();
        LocalDate dt = LocalDate.of(sdt.getYear(), sdt.getMonth(), sdt.getDay());
        if (sct.getExceptionType() == 2) {
            this.notInUse.add(dt);
        }
        if (sct.getExceptionType() == 1) {
            this.inUse.add(dt);
        }
    }

    public void add(String prefix, Frequency fr) {
        String tripid = prefix + fr.getTrip().getId().getId();
        TripComplete ti = this.getTrip(tripid);
        if (ti != null) {
            FrequencyComplete fc = new FrequencyComplete(prefix, fr);
            ti.add(prefix, fc);
        }
    }

    @Id
    private String serviceId;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public boolean isMonday() {
        return monday;
    }

    public void setMonday(boolean monday) {
        this.monday = monday;
    }

    public boolean isTuesday() {
        return tuesday;
    }

    public void setTuesday(boolean tuesday) {
        this.tuesday = tuesday;
    }

    public boolean isWednesday() {
        return wednesday;
    }

    public void setWednesday(boolean wednesday) {
        this.wednesday = wednesday;
    }

    public boolean isThursday() {
        return thursday;
    }

    public void setThursday(boolean thursday) {
        this.thursday = thursday;
    }

    public boolean isFriday() {
        return friday;
    }

    public void setFriday(boolean friday) {
        this.friday = friday;
    }

    public boolean isSaturday() {
        return saturday;
    }

    public void setSaturday(boolean saturday) {
        this.saturday = saturday;
    }

    public boolean isSunday() {
        return sunday;
    }

    public void setSunday(boolean sunday) {
        this.sunday = sunday;
    }

    private LocalDate validFrom;
    private LocalDate validUntil;
    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;
    private boolean sunday;
    public byte weekdays = 0;

    private List<LocalDate> inUse = new ArrayList<>();
    private List<LocalDate> notInUse = new ArrayList<>();
    // private Map<String, TripComplete> trips = new HashMap<>();
    private List<TripComplete> trips = new ArrayList<>();
    private List<String> routes = new ArrayList<>();

    public TripComplete getTrip(String key) {
        for (int i = 0; i < trips.size(); i++) {
            TripComplete tc = trips.get(i);
            if (tc.getTripId().equals(key)) {
                return tc;
            }
        }
        return null;
    }

    public List<LocalDate> getInUse() {
        return inUse;
    }

    public List<LocalDate> getNotInUse() {
        return notInUse;
    }
   
    public List<TripComplete> getTrips() {
        return trips;
    }

    public List<String> getRoutes() {
        return routes;
    }
}
