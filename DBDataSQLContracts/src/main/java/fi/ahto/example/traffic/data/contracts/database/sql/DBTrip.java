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
public class DBTrip implements Serializable {
    
    private static final long serialVersionUID = 4713983106084304945L;
    
    @Id
    @Column(name = "trip_num")
    private Long tripNum;

    @Column(name = "route_id")
    private String routeId;
    @Column(name = "service_id")
    private String serviceId;
    @Column(name = "trip_id")
    private String tripId;
    @Column(name = "headsign")
    private String headsign;
    @Column(name = "direction")
    private short direction;
    @Column(name = "shape_id")
    private String shapeId;
    @Column(name = "block_id")
    private String blockId;
    @Column(name = "wheelchair_accessible")
    private short wheelchairAccessible;
    @Column(name = "bikes_allowed")
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
    /*
    //@Column(name = "route_id")
    public String getRouteId() {
        return routeId;
    }
    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    //@Column(name = "service_id")
    public String getServiceId() {
        return serviceId;
    }
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    //@Column(name = "trip_id")
    public String getTripId() {
        return tripId;
    }
    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    //@Column(name = "headsign")
    public String getHeadsign() {
        return headsign;
    }
    public void setHeadsign(String headsign) {
        this.headsign = headsign;
    }

    //@Column(name = "direction")
    public short getDirection() {
        return direction;
    }
    public void setDirection(short direction) {
        this.direction = direction;
    }

    //@Column(name = "shape_id")
    public String getShapeId() {
        return shapeId;
    }
    public void setShapeId(String shapeId) {
        this.shapeId = shapeId;
    }

    //@Column(name = "block_id")
    public String getBlockId() {
        return blockId;
    }
    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    //@Column(name = "wheelchair_accessible")
    public short getWheelchairAccessible() {
        return wheelchairAccessible;
    }
    public void setWheelchairAccessible(short wheelchairAccessible) {
        this.wheelchairAccessible = wheelchairAccessible;
    }

    //@Column(name = "bikes_allowed")
    public short getBikesAllowed() {
        return bikesAllowed;
    }
    public void setBikesAllowed(short bikesAllowed) {
        this.bikesAllowed = bikesAllowed;
    }
    */
}
