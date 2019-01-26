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

import fi.ahto.example.traffic.data.contracts.internal.ServiceData;
import fi.ahto.example.traffic.data.contracts.internal.StreamServiceData;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.StopTime;

/**
 *
 * @author Jouni Ahto
 */
public class ServiceCalendarExt {

    private final String key;
    private final String prefix;
    private byte weekdays = 0;
    // Oh hell, we need an extra field for special flags...
    private byte extra = 0;

    private final List<LocalDate> inUse = new ArrayList<>();
    private final List<LocalDate> notInUse = new ArrayList<>();
    private final List<TripExt> trips = new ArrayList<>();
    private final List<String> routes = new ArrayList<>();
    private final ServiceCalendar sc;

    public ServiceCalendarExt(String prefix, ServiceCalendar sc) {
        String serviceid = prefix + sc.getServiceId().getId();
        this.prefix = prefix;
        this.key = serviceid;
        this.sc = sc;
        this.add(prefix, sc);
    }

    public ServiceCalendarExt(String prefix, ServiceCalendarDate st) {
        String serviceid = prefix + st.getServiceId().getId();
        this.prefix = prefix;
        this.key = serviceid;
        this.sc = new ServiceCalendar();
        this.add(prefix, st);
    }

    public ServiceCalendarExt(String prefix, StopTime st) {
        String serviceid = prefix + st.getTrip().getServiceId().getId();
        this.prefix = prefix;
        this.key = serviceid;
        this.sc = new ServiceCalendar();
        this.add(prefix, st);
    }

    public void add(String prefix, ServiceCalendarDate sct) {
        LocalDate dt = OneBusAwayHelpers.from(sct.getDate());
        if (sct.getExceptionType() == 2) {
            this.notInUse.add(dt);
        }
        if (sct.getExceptionType() == 1) {
            this.inUse.add(dt);
        }
    }

    public void add(String prefix, ServiceCalendar sc) {
        this.sc.setStartDate(sc.getStartDate());
        this.sc.setEndDate(sc.getEndDate());
        this.sc.setMonday(sc.getMonday());
        this.sc.setTuesday(sc.getTuesday());
        this.sc.setWednesday(sc.getWednesday());
        this.sc.setThursday(sc.getThursday());
        this.sc.setFriday(sc.getFriday());
        this.sc.setSaturday(sc.getSaturday());
        this.sc.setSunday(sc.getSunday());

        if (this.getMonday()) {
            this.weekdays |= 0x1;
        }
        if (this.getTuesday()) {
            this.weekdays |= 0x2;
        }
        if (this.getWednesday()) {
            this.weekdays |= 0x4;
        }
        if (this.getThursday()) {
            this.weekdays |= 0x8;
        }
        if (this.getFriday()) {
            this.weekdays |= 0x10;
        }
        if (this.getSaturday()) {
            this.weekdays |= 0x20;
        }
        if (this.getSunday()) {
            this.weekdays |= 0x40;
        }
    }

    public void add(String prefix, StopTime st) {
        String routeid = prefix + st.getTrip().getRoute().getId().getId();
        String tripid = prefix + st.getTrip().getId().getId();

        TripExt ti = this.getTrip(tripid);
        if (ti == null) {
            ti = new TripExt(prefix, st.getTrip());
            this.getTrips().add(ti);
        }
        ti.add(prefix, st);
        
        if (this.routes.contains(routeid) == false) {
            this.routes.add(routeid);
        }

        // Need to know later if the service is in one direction only.
        // Why was this needed, don't remember anymore...
        if (ti.getDirection() == 1) {
            this.extra |= 0x1;
        }
        if (ti.getDirection() == 2) {
            this.extra |= 0x2;
        }
        /*
        if (dir.equals("1")) {
            service.extra |= 0x1;
        }
        if (dir.equals("2")) {
            service.extra |= 0x2;
        }
         */
    }

    public StreamServiceData toStreamServiceData() {
        StreamServiceData sd = new StreamServiceData();
        sd.getInUse().addAll(this.inUse);
        sd.getNotInUse().addAll(this.notInUse);
        sd.setServiceId(this.getKey());
        sd.setValidFrom(this.getStartDate());
        sd.setValidUntil(this.getEndDate());
        sd.setWeekdays(weekdays);
        sd.setExtra(extra);
        return sd;
    }

    public ServiceData toServiceData() {
        ServiceData sd = new ServiceData();
        sd.getInUse().addAll(this.inUse);
        sd.getNotInUse().addAll(this.notInUse);
        sd.setServiceId(this.getKey());
        sd.setValidFrom(this.getStartDate());
        sd.setValidUntil(this.getEndDate());
        sd.setWeekdays(weekdays);

        sd.setMonday(this.getMonday());
        sd.setTuesday(this.getTuesday());
        sd.setWednesday(this.getWednesday());
        sd.setThursday(this.getThursday());
        sd.setFriday(this.getFriday());
        sd.setSaturday(this.getSaturday());
        sd.setSunday(this.getSunday());
        sd.setExtra(extra);

        if (this.getStartDate() != null && this.getEndDate() != null) {
            return sd;
        }

        if (this.inUse.size() > 0 || this.notInUse.size() > 0) {
            Stream<LocalDate> s1 = this.inUse.stream();
            Stream<LocalDate> s2 = this.notInUse.stream();
            LocalDate min = Stream.concat(s1, s2).min(Comparator.comparing(LocalDate::toEpochDay)).get();

            Stream<LocalDate> s3 = this.inUse.stream();
            Stream<LocalDate> s4 = this.notInUse.stream();
            LocalDate max = Stream.concat(s3, s4).max(Comparator.comparing(LocalDate::toEpochDay)).get();

            if (sd.getValidFrom() == null) {
                sd.setValidFrom(min.minusDays(1));
            }
            if (sd.getValidUntil() == null) {
                sd.setValidUntil(max.plusDays(1));
            }
        }
        return sd;
    }

    public List<String> getRoutes() {
        return routes;
    }

    public List<TripExt> getTrips() {
        return trips;
    }

    public TripExt getTrip(String key) {
        for (int i = 0; i < trips.size(); i++) {
            TripExt tc = trips.get(i);
            if (tc.getKey().equals(key)) {
                return tc;
            }
        }
        return null;
    }

    public String getKey() {
        return key;
    }

    public List<LocalDate> getInUse() {
        return inUse;
    }

    public List<LocalDate> getNotInUse() {
        return notInUse;
    }

    public boolean getMonday() {
        return sc.getMonday() == 1;
    }

    public boolean getTuesday() {
        return sc.getTuesday() == 1;
    }

    public boolean getWednesday() {
        return sc.getWednesday() == 1;
    }

    public boolean getThursday() {
        return sc.getThursday() == 1;
    }

    public boolean getFriday() {
        return sc.getFriday() == 1;
    }

    public boolean getSaturday() {
        return sc.getSaturday() == 1;
    }

    public boolean getSunday() {
        return sc.getSunday() == 1;
    }

    public LocalDate getStartDate() {
        return OneBusAwayHelpers.from(sc.getStartDate());
    }

    public LocalDate getEndDate() {
        return OneBusAwayHelpers.from(sc.getEndDate());
    }
}
