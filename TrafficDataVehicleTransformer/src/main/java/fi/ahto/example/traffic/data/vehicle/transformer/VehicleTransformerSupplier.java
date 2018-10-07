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
package fi.ahto.example.traffic.data.vehicle.transformer;

import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import fi.ahto.kafka.streams.state.utils.TransformerSupplierWithStore;
import java.time.Instant;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jouni Ahto
 */
public class VehicleTransformerSupplier
        extends TransformerSupplierWithStore<String, VehicleActivity, KeyValue<String, VehicleActivity>> {

    private static final Logger LOG = LoggerFactory.getLogger(VehicleTransformerSupplier.class);

    public VehicleTransformerSupplier(StreamsBuilder builder, Serde<String> keyserde, Serde<VehicleActivity> valserde, String storeName) {
        super(builder, keyserde, valserde, storeName);
    }

    @Override
    public Transformer<String, VehicleActivity, KeyValue<String, VehicleActivity>> get() {
        return new TransformerImpl();
    }

    class TransformerImpl implements Transformer<String, VehicleActivity, KeyValue<String, VehicleActivity>> {

        protected KeyValueStore<String, VehicleActivity> store;
        protected ProcessorContext context;

        // See next method init. Almost impossible to debug
        // with old data if we clean it away every 60 seconds. 
        private static final boolean TESTING = true;

        // Unless, "now" happens to be the time of the last record received...
        // Won't cover the last minute, but good enough for testing samples 
        // over one hour.
        Instant now = Instant.EPOCH; // Must initialise to something...
        Instant previousnow = Instant.EPOCH; // Must initialise to something...

        @Override
        public void init(ProcessorContext context) {
            this.context = context;
            this.store = (KeyValueStore<String, VehicleActivity>) context.getStateStore(storeName);

            // Schedule a punctuator method every 20000 milliseconds based on wall-clock time.
            // The idea is to finally get rid of vehicles we haven't received any data for a while.
            // Like night-time.
            this.context.schedule(20000, PunctuationType.WALL_CLOCK_TIME, (timestamp) -> {
                cleanUpOutOfDateData(timestamp);
            });
        }

        private void cleanUpOutOfDateData(long timestamp) {
            KeyValueIterator<String, VehicleActivity> iter = this.store.all();
            while (iter.hasNext()) {
                KeyValue<String, VehicleActivity> entry = iter.next();
                if (entry.value != null) {
                    VehicleActivity va = entry.value;

                    if (!TESTING) {
                        now = Instant.ofEpochMilli(timestamp);
                    }
                    // Make sure that "now" gets incremented even after
                    // test data ends, so the remaining data gets removed.
                    /*
                        if (TESTING) {
                            if (previousnow.equals(Instant.EPOCH)) {
                                previousnow = now;
                            }
                            if (previousnow.equals(now)) {
                                now = now.plusSeconds(20);
                                previousnow = now;
                            }
                        }
                     */
                    if (va.getRecordTime().plusSeconds(20).isBefore(now)) {
                        // Have to be on some line...
                        if (!(va.getLineId() == null || va.getLineId().isEmpty())) {
                            va.setLineHasChanged(true);
                            context.forward(va.getVehicleId(), va);
                        }
                        this.store.delete(entry.key);
                        LOG.info("Cleared all data for vehicle {} and removed it from line {}", va.getVehicleId(), va.getInternalLineId());
                    }
                }
            }
            iter.close();
            context.commit();
        }

        @Override
        public KeyValue<String, VehicleActivity> transform(String k, VehicleActivity v) {
            VehicleActivity previous = store.get(k);

            VehicleActivity transformed = transform(v, previous);
            if (transformed == null) {
                return null;
            }
            store.put(k, v);
            if (v.getLastAddedToHistory() == null) {
                LOG.info("Did not add history stamp for vehicle {} at {}", k, v.getRecordTime());
            }
            if (v.getLineId() == null || v.getLineId().isEmpty() || v.isAtRouteEnd()) {
                LOG.debug("Didn't bother sending data forward for vehicle {}, it isn't on any line", v.getVehicleId());
                return null;
            }
            return KeyValue.pair(k, transformed);
        }

        VehicleActivity transform(VehicleActivity current, VehicleActivity previous) {
            LOG.debug("Transforming vehicle {}", current.getVehicleId());

            if (TESTING) {
                now = current.getRecordTime();
            }

            // Dont' bother with the vehicle until it's on some line. At least
            // FOLI sends data with no line information while the vehicle is changing
            // line, or staying at route end, or not operating anymore (I'd guess).
            // And some other observed cases too.
            if (current.getLineId() == null
                    || current.getLineId().isEmpty()
                    || current.getTripStart().toInstant().equals(Instant.EPOCH)) {
                return null;
            }

            if (previous == null) {
                if (current.isAtRouteEnd()) {
                    LOG.info("Vehicle {} is at the end of line {}", current.getVehicleId(), current.getInternalLineId());
                }
                current.setAddToHistory(true);
                current.setLastAddedToHistory(current.getRecordTime());
                return current;
            }
            /* There was something odd with FOLI data, don't remember anymore.
            Have to check it again, it' probably not solved yet.
            if (previous.getLineId() == null || previous.getLineId().isEmpty()) {
                int i = 0;
            }

            if (current.getInternalLineId().equals("FI:FOLI:53") && current.getVehicleId().equals("FI:FOLI:80025")) {
                int i = 0;
            }
            // */
            boolean calculate = true;

            // Always copy first last history addition
            current.setLastAddedToHistory(previous.getLastAddedToHistory());

            // We do not accept records coming in too late.
            if (current.getRecordTime().isBefore(previous.getRecordTime())) {
                return recordIsLate(previous, current);
            }

            // We get also duplicates quite often...
            if (current.getRecordTime().equals(previous.getRecordTime())) {
                return null;
            }

            // At least TKL seems to inform us in a funny way that a vehicle
            // is not currently operating on any line. It switches to a trip
            // that has a starting time *far before* what was in the previous
            // sample, which is somewhat impossible. But the vehicle still
            // keeps on sending data, not moving much and being many hours late.
            if (current.getTripStart().isBefore(previous.getTripStart())) {
                LOG.debug("Anomaly detected on line {}, vehicle {}, trip start time {} is before {}",
                        current.getInternalLineId(), current.getVehicleId(), current.getTripStart(), previous.getTripStart());
                return null;
            }

            // Vehicle is at end the of line, remove it immediately. It may come back
            // later, but not necessarily on the same line;
            if (current.isAtRouteEnd()) {
                return vehicleIsAtEOL(previous, current);
            }

            // Vehicle has changed line, useless to calculate the change of delay.
            // But we want a new history record now.
            if (current.getInternalLineId().equals(previous.getInternalLineId()) == false) {
                return vehicleChangedLine(current, previous);
            }

            // Change of direction, useless to calculate the change of delay.
            // But we want a new history record now.
            if (current.getDirection().equals(previous.getDirection()) == false) {
                return vehicleChangedDirection(current, previous);
            }

            // Not yet added to history? Find out when we added last time.
            // If more than 59 seconds, then add.
            if (current.AddToHistory() == false) {
                maybeAddToHistory(current, previous);
            }

            if (calculate) {
                // Calculate approximate bearing if missing. Must check later
                // if I got the direction right or 180 degrees wrong, and that
                // both samples actually have the needed coordinates.
                if (current.getBearing() == null) {
                    calculateBearing(current, previous);
                }
            }

            return current;
        }

        private void maybeAddToHistory(VehicleActivity current, VehicleActivity previous) {
            Instant compareto = current.getRecordTime().minusSeconds(59);
            // This shouldn't happen but happens anyway... Find out what's going on.
            // Seems not to happen anymore, probably found the culprit.
            if (previous.getLastAddedToHistory() == null) {
                LOG.info("Shouldn't happen, vehicle {}", current.getVehicleId());
                current.setAddToHistory(true);
                current.setLastAddedToHistory(current.getRecordTime());
            } else if (previous.getLastAddedToHistory().isBefore(compareto)) {
                current.setAddToHistory(true);
                current.setLastAddedToHistory(current.getRecordTime());
            }
        }

        private VehicleActivity vehicleChangedDirection(VehicleActivity current, VehicleActivity previous) {
            // This can happen when testing, the sample data seems to contain
            // also out-of-date records...
            if (current.getStartTime().isBefore(previous.getStartTime()) == false) {
                current.setAddToHistory(true);
                current.setLastAddedToHistory(current.getRecordTime());
                // It actually hasn't, but this will remove it from the line, and it will be
                // re-added again, now with a different direction. Also possible left-over
                // remaining stops will be cleared.
                previous.setLineHasChanged(true);
                context.forward(previous.getVehicleId(), previous);
                LOG.info("Vehicle {} has changed direction ", current.getVehicleId());
                return current;
            } else {
                return previous;
            }
        }

        private VehicleActivity vehicleChangedLine(VehicleActivity current, VehicleActivity previous) {
            current.setAddToHistory(true);
            current.setLastAddedToHistory(current.getRecordTime());
            previous.setLineHasChanged(true);
            // Was on some line, inform forward...
            if (!(previous.getLineId() == null || previous.getLineId().isEmpty())) {
                context.forward(previous.getVehicleId(), previous);
            }
            LOG.info("Vehicle {} has changed line from {} to {}", current.getVehicleId(),
                    previous.getInternalLineId(), current.getInternalLineId());
            // Changing to no line...
            /*
                if (current.getLineId() == null || current.getLineId().isEmpty()) {
                    return null;
                }
             */
            return current;
        }

        private VehicleActivity vehicleIsAtEOL(VehicleActivity previous, VehicleActivity current) {
            if (!previous.isAtRouteEnd()) {
                LOG.info("Vehicle {} is at the end of line {}", current.getVehicleId(), previous.getInternalLineId());
                current.setAddToHistory(true);
                current.setLastAddedToHistory(current.getRecordTime());
                current.setLineHasChanged(true);
                // It doesn't always contain lineid, but we need it to
                // be able to inform the correct line that the vehicle
                // is not operating there anymore.
                current.setInternalLineId(previous.getInternalLineId());
                return current;
            } else {
                // Don't bother sending information every second that a vehicle is
                // standing at the end of line, once is enough. It will stay there...
                return null;
            }
        }

        private VehicleActivity recordIsLate(VehicleActivity previous, VehicleActivity current) {
            // unless we are testing...
            if (TESTING) {
                // But, in that case, must guard against some oddities in incoming data.
                if (previous.isAtRouteEnd() && !current.isAtRouteEnd()) {
                    // Prevent the vehicle from flip-flopping between states, because
                    // data seems to come in out-of-order and multiple times when
                    // the vehicle is at the end of line. Handle only after a new trip starts.
                    if (!current.getStartTime().isAfter(previous.getStartTime())) {
                        LOG.info("Copying vehicle {}", current.getVehicleId());
                        current.copy(previous);
                        // Should already have been added to history the first time it was at eol.
                        current.setAddToHistory(false);
                        return null;
                    }

                }
                current.setAddToHistory(true);
                current.setLastAddedToHistory(current.getRecordTime());
                return current;
            }
            return null;
        }

        private void calculateBearing(VehicleActivity current, VehicleActivity previous) {
            double lat1 = Math.toRadians(current.getLatitude());
            double long1 = Math.toRadians(current.getLongitude());
            double lat2 = Math.toRadians(previous.getLatitude());
            double long2 = Math.toRadians(previous.getLongitude());

            double bearingradians = Math.atan2(Math.asin(long2 - long1) * Math.cos(lat2), Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(long2 - long1));
            double bearingdegrees = Math.toDegrees(bearingradians);

            if (bearingdegrees < 0) {
                bearingdegrees = 360 + bearingdegrees;
            }
            current.setBearing((float) bearingdegrees);
        }

        @Override
        public void close() {
            // Note: The store should NOT be closed manually here via `stateStore.close()`!
            // The Kafka Streams API will automatically close stores when necessary.
        }
    }
}
