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
package fi.ahto.example.traffic.data.contracts.database.sql;

import java.io.Serializable;
import javax.persistence.*;
import lombok.Data;
import org.onebusaway.gtfs.model.Trip;

/**
 *
 * @author Jouni Ahto
 */
@Data
@Entity
@javax.persistence.Table(name = "trips")
@org.springframework.data.relational.core.mapping.Table(value = "trips")
public class DBTrip implements Serializable {
    
    private static final long serialVersionUID = 4713983106084304945L;
    
    @javax.persistence.Id
    @org.springframework.data.annotation.Id
    @GeneratedValue
    @javax.persistence.Column(name = "trip_num")
    @org.springframework.data.relational.core.mapping.Column(value = "trip_num")
    private Long tripNum;

    @javax.persistence.Column(name = "route_id")
    @org.springframework.data.relational.core.mapping.Column(value = "route_id")
    private String routeId;
    @javax.persistence.Column(name = "service_id")
    @org.springframework.data.relational.core.mapping.Column(value = "service_id")
    private String serviceId;
    @javax.persistence.Column(name = "trip_id")
    @org.springframework.data.relational.core.mapping.Column(value = "trip_id")
    private String tripId;
    @javax.persistence.Column(name = "headsign")
    @org.springframework.data.relational.core.mapping.Column(value = "headsign")
    private String headsign;
    @javax.persistence.Column(name = "direction")
    @org.springframework.data.relational.core.mapping.Column(value = "direction")
    private short direction;
    @javax.persistence.Column(name = "shape_id")
    @org.springframework.data.relational.core.mapping.Column(value = "shape_id")
    private String shapeId;
    @javax.persistence.Column(name = "block_id")
    @org.springframework.data.relational.core.mapping.Column(value = "block_id")
    private String blockId;
    @javax.persistence.Column(name = "wheelchair_accessible")
    @org.springframework.data.relational.core.mapping.Column(value = "wheelchair_accessible")
    private short wheelchairAccessible;
    @javax.persistence.Column(name = "bikes_allowed")
    @org.springframework.data.relational.core.mapping.Column(value = "bikes_allowed")
    private short bikesAllowed;
    
    protected DBTrip() {}
    
    public DBTrip(String prefix, Trip src) {
        this.routeId = prefix + src.getRoute().getId().getId();
        this.serviceId = prefix + src.getServiceId().getId();
        this.tripId = prefix + src.getId().getId();
        this.headsign = src.getTripHeadsign();
        // Could crash? Maybe should add some value for an unknown direction.
        this.direction = Short.parseShort(src.getDirectionId());
        if (src.getShapeId() != null) {
            this.shapeId = prefix + src.getShapeId().getId();
        }
        this.wheelchairAccessible = (short) src.getWheelchairAccessible();
        this.bikesAllowed = (short) src.getBikesAllowed();
        if (src.getBlockId() != null && !src.getBlockId().isEmpty()) {
            src.getBlockId();
        }
        // src.getTripShortName();
        // src.getRouteShortName();
    }
}
