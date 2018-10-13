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
import javax.persistence.*;
import org.onebusaway.gtfs.model.Trip;

/**
 *
 * @author Jouni Ahto
 */
@Entity
public class DBTrip implements Serializable {
    
    private static final long serialVersionUID = 4713983106084304945L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long generatedId;
    
    private String routeId;
    private String serviceId;
    private String tripId;
    private String headSign;
    private short direction;
    private String shapeId;
    private String blockId;
    private short wheelChairAccessible;
    private short bikesAllowed;
    
    protected DBTrip() {}
    
    public DBTrip(String prefix, Trip src) {
        this.routeId = prefix + src.getRoute().getId().getId();
        this.serviceId = prefix + src.getServiceId().getId();
        this.tripId = prefix + src.getId().getId();
        this.headSign = src.getTripHeadsign();
        // Could crash? Maybe should add some value for an unknown direction.
        this.direction = Short.parseShort(src.getDirectionId());
        this.shapeId = prefix + src.getShapeId().getId();
        this.wheelChairAccessible = (short) src.getWheelchairAccessible();
        this.bikesAllowed = (short) src.getBikesAllowed();
        if (src.getBlockId() != null && !src.getBlockId().isEmpty()) {
            src.getBlockId();
        }
        // src.getTripShortName();
        // src.getRouteShortName();
    }

    public Long getGeneratedId() {
        return generatedId;
    }

    public void setGeneratedId(Long generatedId) {
        this.generatedId = generatedId;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getHeadSign() {
        return headSign;
    }

    public void setHeadSign(String headSign) {
        this.headSign = headSign;
    }

    public short getDirection() {
        return direction;
    }

    public void setDirection(short direction) {
        this.direction = direction;
    }

    public String getShapeId() {
        return shapeId;
    }

    public void setShapeId(String shapeId) {
        this.shapeId = shapeId;
    }

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public short getWheelChairAccessible() {
        return wheelChairAccessible;
    }

    public void setWheelChairAccessible(short wheelChairAccessible) {
        this.wheelChairAccessible = wheelChairAccessible;
    }

    public short getBikesAllowed() {
        return bikesAllowed;
    }

    public void setBikesAllowed(short bikesAllowed) {
        this.bikesAllowed = bikesAllowed;
    }
}
