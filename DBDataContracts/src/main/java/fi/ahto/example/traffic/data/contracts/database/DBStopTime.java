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
package fi.ahto.example.traffic.data.contracts.database;

import fi.ahto.example.traffic.data.contracts.database.utils.GTFSLocalTimeConverter;
import fi.ahto.example.traffic.data.contracts.internal.GTFSLocalTime;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Jouni Ahto
 */
@Entity
@Table(name = "stop_times")
public interface DBStopTime extends Serializable {
    @Id
    @Column(name = "generated_id")
    String getGeneratedId();
    void setGeneratedId(String generatedId);

    @Column(name = "arrival", nullable = false)
    @Convert(converter = GTFSLocalTimeConverter.class)
    GTFSLocalTime getArrival();
    void setArrival(GTFSLocalTime arrival);

    @Column(name = "departure", nullable = false)
    @Convert(converter = GTFSLocalTimeConverter.class)
    GTFSLocalTime getDeparture();
    void setDeparture(GTFSLocalTime departure);

    @Column(name = "dist_traveled")
    float getDistTraveled();
    void setDistTraveled(float distTraveled);

    @Column(name = "dropoff_type")
    short getDropoffType();
    void setDropoffType(short dropoffType);

    @Column(name = "headsign")
    String getHeadsign();
    void setHeadsign(String headsign);

    @Column(name = "pickup_type")
    short getPickupType();
    void setPickupType(short pickupType);

    @Column(name = "stop_id", nullable = false)
    String getStopId();
    void setStopId(String stopId);

    @Column(name = "stop_sequence", nullable = false)
    int getStopSequence();
    void setStopSequence(int stopSequence);

    @Column(name = "timepoint")
    short getTimepoint();
    void setTimepoint(short timepoint);

    @Column(name = "trip_id", nullable = false)
    String getTripId();
    void setTripId(String tripId);
}
