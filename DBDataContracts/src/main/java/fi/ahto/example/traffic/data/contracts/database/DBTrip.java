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

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.NaturalId;

/**
 *
 * @author Jouni Ahto
 */
@Entity
@Table(name = "trips",
        uniqueConstraints = {
            @UniqueConstraint(name = "trips_trip_id_idx", columnNames = {"trip_id"})
        },
        indexes = {
            @Index(columnList = ("block_id"), name = "trips_block_id_idx"),
            @Index(columnList = ("route_id"), name = "trips_route_id_idx"),
            @Index(columnList = ("service_id"), name = "trips_service_id_idx")
        })

public interface DBTrip extends Serializable {

    @Id
    @Column(name = "generated_id")
    String getGeneratedId();
    void setGeneratedId(String generatedId);

    @Column(name = "bikes_allowed")
    short getBikesAllowed();
    void setBikesAllowed(short bikesAllowed);

    @Column(name = "block_id")
    String getBlockId();
    void setBlockId(String blockId);

    @Column(name = "direction")
    short getDirection();
    void setDirection(short direction);

    @Column(name = "headsign")
    String getHeadsign();
    void setHeadsign(String headsign);

    @Column(name = "route_id", nullable = false)
    String getRouteId();
    void setRouteId(String routeId);

    @Column(name = "service_id", nullable = false)
    String getServiceId();
    void setServiceId(String serviceId);

    @Column(name = "shape_id")
    String getShapeId();
    void setShapeId(String shapeId);

    @NaturalId
    @Column(name = "trip_id", nullable = false)
    String getTripId();
    void setTripId(String tripId);

    @Column(name = "wheelchair_accessible")
    short getWheelchairAccessible();
    void setWheelchairAccessible(short wheelchairAccessible);
}
