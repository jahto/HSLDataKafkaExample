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
import java.time.format.DateTimeFormatter;
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
    Map<String, VehicleActivity> vehiclelist = new HashMap<>();

    @Autowired
    private GtfsRTDataPoller poller;

    private final static String prefix = "FI:VLK:";

    @Test
    public void testReadData() throws IOException {
        GtfsRealtime.FeedMessage tripfeed = null;
        GtfsRealtime.FeedMessage vehiclefeed = null;
        String dir = "../testdata/kuopio/";

        for (int i = 0; i < 951; i++) {
            String postfix = Integer.toString(i) + ".data";
            File vehicles = new File(dir + "vehicles-" + postfix);
            File trips = new File(dir + "trips-" + postfix);
            try (InputStream stream = new FileInputStream(vehicles)) {
                vehiclefeed = GtfsRealtime.FeedMessage.parseFrom(stream);
                List<FeedEntity> list = vehiclefeed.getEntityList();
                for (FeedEntity ent : list) {
                    if (ent.hasVehicle()) {
                        parseVehicle(ent.getVehicle(), prefix);
                    }
                }
            } catch (IOException ex) {
                LOG.debug("Problem with file", ex);
            }
            try (InputStream stream = new FileInputStream(trips)) {
                tripfeed = GtfsRealtime.FeedMessage.parseFrom(stream);
                List<FeedEntity> list = tripfeed.getEntityList();
                for (FeedEntity ent : list) {
                    if (ent.hasTripUpdate()) {
                        parseTripUpdate(ent.getTripUpdate(), vehiclelist, prefix);
                    }
                }
            } catch (IOException ex) {
                LOG.debug("Problem with file", ex);
            }
            poller.putDataToQueue(vehiclelist.values());
        }
    }

    public void parseTripUpdate(TripUpdate tu, Map<String, VehicleActivity> map, String prefix) {
        // We handle currently only scheduled trips.
        if (tu.getTrip().getScheduleRelationship() != ScheduleRelationship.SCHEDULED) {
            return;
        }

        if (!tu.hasVehicle()) {
            return;
        }

        VehicleDescriptor vd = tu.getVehicle();
        if (!vd.hasId()) {
            return;
        }

        VehicleActivity va = map.get(prefix + vd.getId());
        if (va == null) {
            // Currently, just return. Although we could also check if there's
            // enough information available to construct a new VehicleActivity
            // instance. Perhaps later.
            return;
        }

        if (tu.hasDelay()) {
            va.setDelay(tu.getDelay());
        }

        // Find the first StopTimeUpdate not in the past.
        StopTimeUpdate first = null;
        if (!(tu.getStopTimeUpdateList() == null || tu.getStopTimeUpdateList().isEmpty())) {
            long cmp = va.getRecordTime().getEpochSecond();

            // What happens if go to the end of list of list without ever
            // breaking? Is that acually possible?
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
            if (!tu.hasDelay()) {
                if (first.hasArrival() && first.getArrival().hasDelay()) {
                    va.setDelay(first.getArrival().getDelay());
                } else if (first.hasDeparture() && first.getDeparture().hasDelay()) {
                    va.setDelay(first.getDeparture().getDelay());
                }
            }
            int i = 0;
        }
    }

    public void parseVehicle(VehiclePosition vp, String prefix) {
        if (vp.hasVehicle()) {
            VehicleActivity va = new VehicleActivity();
            VehicleDescriptor vd = vp.getVehicle();
            if (vd.hasId()) {
                String id = vd.getId();
                if (id == null) {
                    // No use for a vehicle we can't identify
                    return;
                }
                va.setVehicleId(prefix + id);

                if (vp.hasTimestamp()) {
                    va.setRecordTime(Instant.ofEpochSecond(vp.getTimestamp()));
                } else {
                    va.setRecordTime(Instant.now());
                }

                if (vp.hasPosition()) {
                    Position pos = vp.getPosition();
                    va.setLatitude(pos.getLatitude());
                    va.setLongitude(pos.getLongitude());
                    if (pos.hasBearing()) {
                        va.setBearing(pos.getBearing());
                    }
                    if (pos.hasSpeed()) {
                        va.setSpeed(pos.getSpeed());
                    }
                }
                if (vp.hasTrip()) {
                    TripDescriptor td = vp.getTrip();
                    if (!td.hasTripId()) {
                        if (!(td.hasRouteId()
                                && td.hasDirectionId()
                                && td.hasStartTime()
                                && td.hasStartDate())) {
                            return;
                        }
                    }
                    if (td.hasTripId()) {
                        va.setTripID(prefix + td.getTripId());
                    }
                    if (td.hasRouteId()) {
                        va.setInternalLineId(prefix + td.getRouteId());
                        va.setLineId(td.getRouteId());
                    }
                    if (td.hasDirectionId()) {
                        int dir = td.getDirectionId() + 1;
                        va.setDirection(Integer.toString(dir));
                    }
                    if (td.hasStartTime()) {
                        String time = td.getStartTime();
                        va.setStartTime(LocalTime.parse(time));
                    }
                    if (td.hasStartDate()) {
                        String date = td.getStartDate();
                        va.setOperatingDate(LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE));
                    }
                }
            }
            vehiclelist.put(va.getVehicleId(), va);
        }
    }
}
