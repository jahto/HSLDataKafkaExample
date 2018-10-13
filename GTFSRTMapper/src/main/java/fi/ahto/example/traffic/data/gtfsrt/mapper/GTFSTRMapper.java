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
package fi.ahto.example.traffic.data.gtfsrt.mapper;

import com.google.transit.realtime.GtfsRealtime;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jouni Ahto
 */
public class GTFSTRMapper {
    private static final Logger LOG = LoggerFactory.getLogger(GTFSTRMapper.class);
    Map<String, VehicleActivity> vehicles = new HashMap<>();
    private final String source;
    private final String prefix;
    private final ZoneId zone;
    private final LocalTime cutoff;
    
    public GTFSTRMapper(String source, ZoneId zone, LocalTime cutoff) {
        this.source = source;
        this.prefix = source + ":";
        this.zone = zone;
        this.cutoff = cutoff;
    }
    
    public List<VehicleActivity> combineData(List<GtfsRealtime.VehiclePosition> positions,
                                             List<GtfsRealtime.TripUpdate> trips) {
        for (GtfsRealtime.VehiclePosition vp : positions) {
            parseVehiclePosition(vp);
        }
        
        for (GtfsRealtime.TripUpdate tu : trips) {
            parseTripUpdate(tu);
        }
        
        List<VehicleActivity> ret = new ArrayList<>();
        for (VehicleActivity va : vehicles.values()) {
            if (va.validateHasEnoughData()) {
                ret.add(va);
            }
        }
        
        return ret;
    }

    public VehicleActivity getVehicle(GtfsRealtime.TripUpdate tu) {
        if (!tu.hasVehicle()) {
            return null;
        }
        
        GtfsRealtime.VehicleDescriptor vd = tu.getVehicle();
        if (!vd.hasId()) {
            return null;
        }

        VehicleActivity va = vehicles.get(prefix + vd.getId());
        if (va == null) {
            va = new VehicleActivity();
            String id = vd.getId();
            va.setVehicleId(prefix + id);
            va.setSource(source);
            vehicles.put(prefix + id, va);
        }
        
        if (tu.hasTrip()) {
            parseTripDescriptor(tu.getTrip(), va);
        }
        
        if (tu.hasDelay()) {
            va.setDelay(tu.getDelay());
        }

        if (va.getRecordTime() == null && tu.hasTimestamp()) {
            va.setRecordTime(Instant.ofEpochSecond(tu.getTimestamp()));
        }
                
        return va;
    }

    public void parseTripUpdate(GtfsRealtime.TripUpdate tu) {
        // We handle currently only scheduled trips.
        if (tu.getTrip().getScheduleRelationship() != GtfsRealtime.TripDescriptor.ScheduleRelationship.SCHEDULED) {
            return;
        }

        VehicleActivity va = getVehicle(tu);
        if (va == null) {
            // We don't currently handle trip updates not associated with a vehicle. Maybe later.-
            return;
        }

        if (va.getNextStopId() == null) {
            addStopId(tu, va);
        }
    }

    public void addStopId(GtfsRealtime.TripUpdate tu, VehicleActivity va) {
        // Find the first StopTimeUpdate not in the past.
        GtfsRealtime.TripUpdate.StopTimeUpdate first = null;
        if (!(tu.getStopTimeUpdateList() == null || tu.getStopTimeUpdateList().isEmpty())) {
            long cmp = va.getRecordTime().getEpochSecond();

            // What happens if we go to the end of list of list
            // without ever breaking? Is that actually possible?
            for (int j = 0; j < tu.getStopTimeUpdateList().size(); j++) {
                long cur = 0;
                first = tu.getStopTimeUpdateList().get(j);
                if (first.hasArrival()) {
                    cur = first.getArrival().getTime();
                } else {
                    cur = first.getDeparture().getTime();
                }

                if (cur >= cmp) {
                    break;
                }
            }
        }

        if (first != null) {
            if (va.getNextStopSequence() == null && first.hasStopSequence()) {
                va.setNextStopSequence(first.getStopSequence());
            } 
            if (va.getNextStopId() == null && first.hasStopId()) {
                va.setNextStopId(prefix + first.getStopId());
            } 
            if (va.getDelay() == null) {
                if (first.hasArrival() && first.getArrival().hasDelay()) {
                    va.setDelay(first.getArrival().getDelay());
                } else if (first.hasDeparture() && first.getDeparture().hasDelay()) {
                    va.setDelay(first.getDeparture().getDelay());
                }
            }
        }
        // Could fill onwarcalls here, if they are reliable.
        // Must add a flag for that, they often aren't.
    }
    
    public void parseVehiclePosition(GtfsRealtime.VehiclePosition vp) {
        if (!vp.hasVehicle()) {
            return;
        }

        GtfsRealtime.VehicleDescriptor vd = vp.getVehicle();
        // No use for a vehicle we can't identify
        if (!vd.hasId() || vd.getId().isEmpty()) {
            return;
        }

        VehicleActivity va = new VehicleActivity();
        String id = vd.getId();
        va.setVehicleId(prefix + id);
        va.setSource(source);

        if (vp.hasTimestamp()) {
            va.setRecordTime(Instant.ofEpochSecond(vp.getTimestamp()));
        } else {
            va.setRecordTime(Instant.now());
        }
        if (vp.hasCurrentStopSequence()) {
            va.setNextStopSequence(vp.getCurrentStopSequence());
        }
        if (vp.hasStopId()) {
            va.setNextStopId(prefix + vp.getStopId());
        }
        if (vp.hasPosition()) {
            parsePosition(vp.getPosition(), va);
        }
        if (vp.hasTrip()) {
            parseTripDescriptor(vp.getTrip(), va);
        }

        vehicles.put(va.getVehicleId(), va);
    }

    public void parseTripDescriptor(GtfsRealtime.TripDescriptor td, VehicleActivity va) {
        if (va.getTripID() == null && td.hasTripId()) {
            va.setTripID(prefix + td.getTripId());
        }
        if (va.getInternalLineId() == null && td.hasRouteId()) {
            va.setInternalLineId(prefix + td.getRouteId());
            va.setLineId(td.getRouteId());
        }
        if (va.getDirection() == null && td.hasDirectionId()) {
            int dir = td.getDirectionId() + 1;
            va.setDirection(Integer.toString(dir));
        }
        if (va.getStartTime() == null && td.hasStartTime()) {
            String time = td.getStartTime();
            va.setStartTime(LocalTime.parse(time));
        }
        if (va.getOperatingDate() == null && td.hasStartDate()) {
            String date = td.getStartDate();
            va.setOperatingDate(LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE));
        }
        if (va.getOperatingDate() != null && va.getStartTime() != null && va.getTripStart() == null) {
            LocalDate date = va.getOperatingDate();
            LocalTime time = va.getStartTime();
            if (time.isBefore(cutoff)) {
                date = date.plusDays(1);
            }
            va.setTripStart(ZonedDateTime.of(date, time, zone));
        }
    }

    public void parsePosition(GtfsRealtime.Position pos, VehicleActivity va) {
        va.setLatitude(pos.getLatitude());
        va.setLongitude(pos.getLongitude());
        if (pos.hasBearing()) {
            va.setBearing(pos.getBearing());
        }
        if (pos.hasSpeed()) {
            va.setSpeed(pos.getSpeed());
        }
    }
}