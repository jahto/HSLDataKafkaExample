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
package fi.ahto.example.traffic.data.contracts.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

/**
 *
 * @author Jouni Ahto
 */
public class StopData implements Serializable {
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

    public short getWheelchairBoarding() {
        return wheelchairBoarding;
    }

    public void setWheelchairBoarding(short wheelchairBoarding) {
        this.wheelchairBoarding = wheelchairBoarding;
    }

    public String getPlatformCode() {
        return platformCode;
    }

    public void setPlatformCode(String platformCode) {
        this.platformCode = platformCode;
    }

    public String getStopid() {
        return stopid;
    }

    public void setStopid(String stopid) {
        this.stopid = stopid;
    }

    public String getStopname() {
        return stopname;
    }

    public void setStopname(String stopname) {
        this.stopname = stopname;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getStopcode() {
        return stopcode;
    }

    public void setStopcode(String stopcode) {
        this.stopcode = stopcode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
    private static final long serialVersionUID = 195474500820358925L;
    @JsonProperty("StopId")
    @org.springframework.data.annotation.Id
    private String stopid;
    @JsonProperty("StopName")
    private String stopname;
    @JsonProperty("Latitude")
    private double latitude;
    @JsonProperty("Longitude")
    private double longitude;
    @JsonProperty("StopCode")
    private String stopcode;
    @JsonProperty("Description")
    private String desc;
    private String url;
    private short locationType;
    private String parentStation;
    private short wheelchairBoarding; // CHECK: probable boolean in reality.
    private String platformCode;

    // @JsonProperty("RoutesServed")
    // public List<String> routesserved = new ArrayList<>();
}
