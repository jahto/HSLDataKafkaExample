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

import fi.ahto.example.traffic.data.contracts.internal.GTFSLocalTime;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 *
 * @author Jouni Ahto
 */
// @Entity
public interface DBStopTime extends Serializable {
    @Id
    @Column(name = "generated_id")
            
    String getGeneratedId();

    void setGeneratedId(String generatedId);

    GTFSLocalTime getArrival();

    GTFSLocalTime getDeparture();

    float getDistTraveled();

    short getDropOffType();

    String getHeadSign();

    short getPickupType();

    String getStopId();

    int getStopSequence();

    short getTimepoint();

    String getTripId();

    void setArrival(GTFSLocalTime arrival);

    void setDeparture(GTFSLocalTime departure);

    void setDistTraveled(float distTraveled);

    void setDropOffType(short dropOffType);

    void setHeadSign(String headSign);

    void setPickupType(short pickupType);

    void setStopId(String stopId);

    void setStopSequence(int stopSequence);

    void setTimepoint(short timepoint);

    void setTripId(String tripId);
    
}
