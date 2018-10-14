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
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import fi.ahto.example.traffic.data.gtfsrt.mapper.GTFSRTMapper;
import fi.ahto.example.vilkku.data.connector.GtfsRTDataPoller;
import fi.ahto.example.vilkku.data.connector.KafkaConfiguration;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
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
 * feeds too once it gets a bit more finished. Already done.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = KafkaConfiguration.class)
public class SimpleTests {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleTests.class);

    @Autowired
    private GtfsRTDataPoller poller;

    private final static String source = "FI:VLK";
    private final static String prefix = source + ":";
    private final static ZoneId zone = ZoneId.of("Europe/Helsinki");
    private final static LocalTime cutoff = LocalTime.of(0, 0);

    @Test
    public void testReadData() throws IOException {
        GTFSRTMapper mapper = new GTFSRTMapper(source, zone, cutoff);
        String dir = "../testdata/kuopio/";

        for (int i = 0; i < 951; i++) {
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
}
