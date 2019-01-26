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
package fi.ahto.example.traffic.data.line.transformer;

import fi.ahto.example.traffic.data.contracts.internal.TripStop;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import fi.ahto.example.traffic.data.contracts.internal.VehicleAtStop;
import fi.ahto.kafka.streams.state.utils.TransformerSupplierWithStore;
import fi.ahto.kafka.streams.state.utils.TransformerWithStore;
import java.util.Iterator;
import java.util.NavigableSet;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jouni Ahto
 */
public class TimeTableComparerSupplier extends TransformerSupplierWithStore<String, VehicleActivity, KeyValue<String, VehicleAtStop>> {

    private static final Logger LOG = LoggerFactory.getLogger(TimeTableComparerSupplier.class);

    public TimeTableComparerSupplier(StreamsBuilder builder, Serde<String> keyserde, Serde<VehicleActivity> valserde, String storeName) {
        super(builder, keyserde, valserde, storeName);
    }

    @Override
    public Transformer<String, VehicleActivity, KeyValue<String, VehicleAtStop>> get() {
        return new TransformerImpl() {
            @Override
            public KeyValue<String, VehicleAtStop> transform(String key, VehicleActivity previous, VehicleActivity current) {
                return compareTimeTables(previous, current);
            }
        };
    }

    protected class TransformerImpl
            extends TransformerSupplierWithStore<String, VehicleActivity, KeyValue<String, VehicleAtStop>>.TransformerImpl
            implements TransformerWithStore<String, VehicleActivity, KeyValue<String, VehicleAtStop>> {

        @Override
        public KeyValue<String, VehicleAtStop> transform(String key, VehicleActivity previous, VehicleActivity current) {
            return compareTimeTables(previous, current);
        }

        KeyValue<String, VehicleAtStop> compareTimeTables(VehicleActivity previous, VehicleActivity current) {
            boolean fixed = false;

            LOG.debug("Comparing timetables.");
            if (current.getOnwardCalls().isEmpty()) {
                // There's still a rare strange problem with the logic for FOLI.
                // Vehicles that have changed line or direction, but not any
                // stop information, so we haven't any list of stops the vehicle
                // should be removed from.-
                LOG.warn("There should be onwardcalls now, vehicle {}, line {}",
                        current.getVehicleId(), current.getInternalLineId());
            }
            // This to skip some oddities at least in FI:TKL data,
            // maybe others too. Have to move elsewhere...
            /*
                LocalDate ld = left.getOperatingDate();
                LocalTime lt = left.getStartTime();
                ZoneId lz = left.getTripStart().getZone();
                ZonedDateTime zdt = ZonedDateTime.of(ld, lt, lz);
                Instant cmp = zdt.toInstant();
                if (cmp.isBefore(left.getRecordTime())) {
                    int i = 0;
                }
             */
            if (previous == null) {
                LOG.debug("Adding stop times first time.");
                Iterator<TripStop> iter = current.getOnwardCalls().descendingIterator();
                while (iter.hasNext()) {
                    TripStop curstop = iter.next();
                    VehicleAtStop vas = new VehicleAtStop();
                    vas.vehicleId = current.getVehicleId();
                    vas.lineId = current.getLineId();
                    vas.arrivalTime = curstop.arrivalTime;
                    context.forward(curstop.stopid, vas);
                }
                return null;
            }

            if (current.LineHasChanged()) {
                LOG.debug("Removing all remaining stops for vehicle {}", current.getVehicleId());
                for (TripStop ss : previous.getOnwardCalls()) {
                    LOG.debug("Removing vehicle {} from stop {}", current.getVehicleId(), ss.stopid);
                    VehicleAtStop vas = new VehicleAtStop();
                    vas.remove = true;
                    vas.vehicleId = previous.getVehicleId();
                    vas.lineId = previous.getLineId();
                    vas.arrivalTime = ss.arrivalTime;
                    context.forward(ss.stopid, vas);
                }
                return null;
            }

            Iterator<TripStop> iter = current.getOnwardCalls().descendingIterator();
            TripStop curstop = null;
            while (iter.hasNext()) {
                curstop = iter.next();
                TripStop prevstop = previous.getOnwardCalls().floor(curstop);
                if (prevstop != null) {
                    // Just skip until the reason has been found...
                    if (curstop.arrivalTime == null) {
                        LOG.warn("Current arrival time is null for stop {}", curstop.stopid);
                        continue;
                    }
                    if (prevstop.arrivalTime == null) {
                        LOG.warn("Previous arrival time is null for stop {}", curstop.stopid);
                        continue;
                    }
                    if (curstop.arrivalTime.compareTo(prevstop.arrivalTime) != 0) {
                        // Vehicle's estimated arriving time to these stops has changed.
                        // Push the information to some queue.
                        VehicleAtStop vas = new VehicleAtStop();
                        vas.vehicleId = current.getVehicleId();
                        vas.lineId = current.getLineId();
                        vas.arrivalTime = curstop.arrivalTime;
                        context.forward(curstop.stopid, vas);
                        fixed = true;
                    }
                }
            }

            if (fixed) {
                LOG.debug("Fixed estimated arrival times.");
            }

            if (curstop != null) {
                NavigableSet<TripStop> remove = previous.getOnwardCalls().headSet(curstop, false);
                if (remove != null && remove.size() > 0) {
                    // Vehicle has gone past these stops, so will not be arriving
                    // to them anymore. Push the information to some queue.
                    LOG.debug("Removing stops.");
                    for (TripStop ss : remove) {
                        LOG.debug("Removing vehicle {} from stop {}", current.getVehicleId(), ss.stopid);
                        VehicleAtStop vas = new VehicleAtStop();
                        vas.remove = true;
                        vas.vehicleId = current.getVehicleId();
                        vas.lineId = current.getLineId();
                        vas.arrivalTime = ss.arrivalTime;
                        context.forward(ss.stopid, vas);
                    }
                }
            }

            return null;
        }
    }
}
