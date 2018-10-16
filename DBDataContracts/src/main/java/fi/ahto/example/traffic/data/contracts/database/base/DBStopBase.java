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
package fi.ahto.example.traffic.data.contracts.database.base;

import java.io.Serializable;
import javax.persistence.*;
import org.onebusaway.gtfs.model.Stop;

/**
 *
 * @author Jouni Ahto
 */
@MappedSuperclass
public class DBStopBase implements Serializable {
    
    private static final long serialVersionUID = -5765784657797522975L;

    @Column(name = "stop_id")
    private String stopId;
    @Column(name = "stop_code")
    private String stopCode;
    @Column(name = "stop_name")
    private String stopName;
    @Column(name = "stop_description")
    private String stopDescription;
    @Column(name = "latitude")
    private float latitude;
    @Column(name = "longitude")
    private float longitude;
    @Column(name = "url")
    private String url;
    @Column(name = "location_type")
    private short locationType;
    @Column(name = "parent_station")
    private String parentStation;
    @Column(name = "wheelchair_boarding")
    private short wheelchairBoarding; // CHECK: probable boolean in reality.
    @Column(name = "platform_code")
    private String platformCode;
    
    protected DBStopBase() {}
    
    public DBStopBase(String prefix, Stop src) {
        this.stopId = prefix + src.getId().getId();
        this.stopCode = prefix + src.getCode();
        this.stopName = src.getName();
        this.stopDescription = src.getDesc();
        this.latitude = (float) src.getLat();
        this.longitude = (float) src.getLon();
        // src.getTimezone(); // CHECK: Do we need this?
        this.url = src.getUrl();
        this.locationType = (short) src.getLocationType();
        this.parentStation = prefix + src.getParentStation();
        this.wheelchairBoarding = (short) src.getWheelchairBoarding();
        this.platformCode = src.getPlatformCode();
        // src.getVehicleType(); // CHECK: Do we need this?
        // src.getDirection(); // CHECK: Do we need this?
    }

    //@Column(name = "stop_id")
    public String getStopId() {
        return stopId;
    }
    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    //@Column(name = "stop_code")
    public String getStopCode() {
        return stopCode;
    }
    public void setStopCode(String stopCode) {
        this.stopCode = stopCode;
    }

    //@Column(name = "stop_name")
    public String getStopName() {
        return stopName;
    }
    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    //@Column(name = "stop_description")
    public String getStopDescription() {
        return stopDescription;
    }
    public void setStopDescription(String stopDescription) {
        this.stopDescription = stopDescription;
    }

    //@Column(name = "latitude")
    public float getLatitude() {
        return latitude;
    }
    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    //@Column(name = "longitude")
    public float getLongitude() {
        return longitude;
    }
    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    //@Column(name = "url")
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    //@Column(name = "location_type")
    public short getLocationType() {
        return locationType;
    }
    public void setLocationType(short locationType) {
        this.locationType = locationType;
    }

    //@Column(name = "parent_station")
    public String getParentStation() {
        return parentStation;
    }
    public void setParentStation(String parentStation) {
        this.parentStation = parentStation;
    }

    //@Column(name = "wheelchair_boarding")
    public short getWheelchairBoarding() {
        return wheelchairBoarding;
    }
    public void setWheelchairBoarding(short wheelchairBoarding) {
        this.wheelchairBoarding = wheelchairBoarding;
    }

    //@Column(name = "platform_code")
    public String getPlatformCode() {
        return platformCode;
    }
    public void setPlatformCode(String platformCode) {
        this.platformCode = platformCode;
    }
}
