/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this trips except in compliance with the License.
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
package fi.ahto.example.vilkku.data.connector.tests;

import com.google.transit.realtime.GtfsRealtime;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.Position;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor.ScheduleRelationship;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import fi.ahto.example.vilkku.data.connector.GtfsRTDataPoller;
import fi.ahto.example.vilkku.data.connector.KafkaConfiguration;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import fi.ahto.example.traffic.data.gtfsrt.mapper.GTFSTRMapper;

/**
 *
 * @author Jouni Ahto
 */

/*
 * NOTE: This is just sketching how to convert GTFS-RT to our internal format.
 * The code will be moved to a separate project that can be used for other
 * feeds too once it gets a bit more finished.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = KafkaConfiguration.class)
public class SimpleTests {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleTests.class);
    // Map<String, VehicleActivity> vehicles = new HashMap<>();

    @Autowired
    private GtfsRTDataPoller poller;

    private final static String source = "FI:VLK";
    private final static String prefix = source + ":";
    private final static ZoneId zone = ZoneId.of("Europe/Helsinki");
    private final static LocalTime cutoff = LocalTime.of(0, 0);

    @Test
    public void testReadData() throws IOException {
        GTFSTRMapper mapper = new GTFSTRMapper(source, zone, cutoff);
        String dir = "../testdata/kuopio/";

        for (int i = 0; i < 951; i++) {
            // vehicles.clear();
            List<GtfsRealtime.VehiclePosition> poslist = new ArrayList<>();   
            List<GtfsRealtime.TripUpdate> triplist = new ArrayList<>();
            
            String postfix = Integer.toString(i) + ".data";
            File vehicles = new File(dir + "vehicles-" + postfix);
            File trips = new File(dir + "trips-" + postfix);
            
            try (InputStream stream = new FileInputStream(vehicles)) {
                GtfsRealtime.FeedMessage vehiclefeed = GtfsRealtime.FeedMessage.parseFrom(stream);
                List<FeedEntity> list = vehiclefeed.getEntityList();
                for (FeedEntity ent : list) {
                    if (ent.hasVehicle()) {
                        poslist.add(ent.getVehicle());
                    }
                }
            } catch (IOException ex) {
                LOG.debug("Problem with file", ex);
            }
            try (InputStream stream = new FileInputStream(trips)) {
                GtfsRealtime.FeedMessage tripfeed = GtfsRealtime.FeedMessage.parseFrom(stream);
                List<FeedEntity> list = tripfeed.getEntityList();
                for (FeedEntity ent : list) {
                    if (ent.hasTripUpdate()) {
                        triplist.add(ent.getTripUpdate());
                    }
                }
            } catch (IOException ex) {
                LOG.debug("Problem with file", ex);
            }
            
            List<VehicleActivity> result = mapper.combineData(poslist, triplist);
            poller.putDataToQueue(result);
        }
    }
    /*
    public List<VehicleActivity> combineData(List<GtfsRealtime.VehiclePosition> positions,
                                             List<GtfsRealtime.TripUpdate> trips) {
        for (VehiclePosition vp : positions) {
            parseVehiclePosition(vp);
        }
        
        for (TripUpdate tu : trips) {
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

    public VehicleActivity getVehicle(TripUpdate tu) {
        if (!tu.hasVehicle()) {
            return null;
        }
        
        VehicleDescriptor vd = tu.getVehicle();
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

    public void parseTripUpdate(TripUpdate tu) {
        // We handle currently only scheduled trips.
        if (tu.getTrip().getScheduleRelationship() != ScheduleRelationship.SCHEDULED) {
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

    public void addStopId(TripUpdate tu, VehicleActivity va) {
        // Find the first StopTimeUpdate not in the past.
        StopTimeUpdate first = null;
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
            String stid = first.getStopId();
            va.setNextStopId(prefix + stid);
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
    
    public void parseVehiclePosition(VehiclePosition vp) {
        if (!vp.hasVehicle()) {
            return;
        }

        VehicleDescriptor vd = vp.getVehicle();
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

        if (vp.hasStopId()) {
            va.setNextStopId(vp.getStopId());
        }

        if (vp.hasPosition()) {
            parsePosition(vp.getPosition(), va);
        }
        if (vp.hasTrip()) {
            parseTripDescriptor(vp.getTrip(), va);

        }

        vehicles.put(va.getVehicleId(), va);
    }

    public void parseTripDescriptor(TripDescriptor td, VehicleActivity va) {
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

    public void parsePosition(Position pos, VehicleActivity va) {
        va.setLatitude(pos.getLatitude());
        va.setLongitude(pos.getLongitude());
        if (pos.hasBearing()) {
            va.setBearing(pos.getBearing());
        }
        if (pos.hasSpeed()) {
            va.setSpeed(pos.getSpeed());
        }
    }
    */
}
