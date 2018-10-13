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
import org.onebusaway.gtfs.model.Stop;

/**
 *
 * @author Jouni Ahto
 */
@Entity
public class DBStop implements Serializable {
    
    private static final long serialVersionUID = -5765784657797522975L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long generatedId;

    // stop_id,stop_code,stop_name,stop_desc,stop_lat,stop_lon,zone_id,stop_url,location_type,parent_station,wheelchair_boarding,platform_code,vehicle_type
    private String stopId;
    private String stopCode;
    private String stopName;
    private String stopDescription;
    private float latitude;
    private float longitude;
    private String url;
    private short locationType;
    private String parentStation;
    private short wheelChairBoarding; // CHECK: probable boolean in reality.
    private String platFormCode;
    
    protected DBStop() {}
    
    public DBStop(String prefix, Stop src) {
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
        this.wheelChairBoarding = (short) src.getWheelchairBoarding();
        this.platFormCode = src.getPlatformCode();
        // src.getVehicleType(); // CHECK: Do we need this?
        // src.getDirection(); // CHECK: Do we need this?
    }

    public Long getGeneratedId() {
        return generatedId;
    }

    public void setGeneratedId(Long generatedId) {
        this.generatedId = generatedId;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getStopCode() {
        return stopCode;
    }

    public void setStopCode(String stopCode) {
        this.stopCode = stopCode;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getStopDescription() {
        return stopDescription;
    }

    public void setStopDescription(String stopDescription) {
        this.stopDescription = stopDescription;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public short getLocationType() {
        return locationType;
    }

    public void setLocationType(short locationType) {
        this.locationType = locationType;
    }

    public String getParentStation() {
        return parentStation;
    }

    public void setParentStation(String parentStation) {
        this.parentStation = parentStation;
    }

    public short getWheelChairBoarding() {
        return wheelChairBoarding;
    }

    public void setWheelChairBoarding(short wheelChairBoarding) {
        this.wheelChairBoarding = wheelChairBoarding;
    }

    public String getPlatFormCode() {
        return platFormCode;
    }

    public void setPlatFormCode(String platFormCode) {
        this.platFormCode = platFormCode;
    }
}
