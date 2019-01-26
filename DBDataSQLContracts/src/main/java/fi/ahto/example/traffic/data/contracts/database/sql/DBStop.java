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

import fi.ahto.example.traffic.data.contracts.internal.StopData;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import lombok.Data;
/**
 *
 * @author Jouni Ahto
 */
@Data
@Entity
@javax.persistence.Table(name = "stops")
@org.springframework.data.relational.core.mapping.Table(value = "stops")
public class DBStop implements Serializable {
    
    private static final long serialVersionUID = -5765784657797522975L;

    @javax.persistence.Id
    @org.springframework.data.annotation.Id
    @GeneratedValue
    @javax.persistence.Column(name = "stop_num")
    @org.springframework.data.relational.core.mapping.Column(value = "stop_num")
    private Long stopNum;

    @javax.persistence.Column(name = "stop_id")
    @org.springframework.data.relational.core.mapping.Column(value = "stop_id")
    private String stopId;
    @javax.persistence.Column(name = "stop_code")
    @org.springframework.data.relational.core.mapping.Column(value = "stop_code")
    private String stopCode;
    @javax.persistence.Column(name = "stop_name")
    @org.springframework.data.relational.core.mapping.Column(value = "stop_name")
    private String stopName;
    @javax.persistence.Column(name = "stop_description")
    @org.springframework.data.relational.core.mapping.Column(value = "stop_description")
    private String stopDescription;
    @javax.persistence.Column(name = "latitude")
    @org.springframework.data.relational.core.mapping.Column(value = "latitude")
    private float latitude;
    @javax.persistence.Column(name = "longitude")
    @org.springframework.data.relational.core.mapping.Column(value = "longitude")
    private float longitude;
    @javax.persistence.Column(name = "url")
    @org.springframework.data.relational.core.mapping.Column(value = "url")
    private String url;
    @javax.persistence.Column(name = "location_type")
    @org.springframework.data.relational.core.mapping.Column(value = "location_type")
    private short locationType;
    @javax.persistence.Column(name = "parent_station")
    @org.springframework.data.relational.core.mapping.Column(value = "parent_station")
    private String parentStation;
    @javax.persistence.Column(name = "wheelchair_boarding")
    @org.springframework.data.relational.core.mapping.Column(value = "wheelchair_boarding")
    private short wheelchairBoarding; // CHECK: probable boolean in reality.
    @javax.persistence.Column(name = "platform_code")
    @org.springframework.data.relational.core.mapping.Column(value = "platform_code")
    private String platformCode;
    
    protected DBStop() {}
    
    public DBStop(StopData src) {
        this.stopId = src.getStopid();
        this.stopCode = src.getStopcode();
        //if (src.getCode() != null && !src.getCode().isEmpty()) {
        //    this.stopCode = prefix + src.getCode();
        //}
        this.stopName = src.getStopname();
        this.stopDescription = src.getDesc();
        this.latitude = (float) src.getLatitude();
        this.longitude = (float) src.getLongitude();
        // src.getTimezone(); // CHECK: Do we need this?
        this.url = src.getUrl();
        this.locationType = (short) src.getLocationType();
        this.parentStation = src.getParentStation();
        //if (src.getParentStation() != null && !src.getParentStation().isEmpty()) {
        //    this.parentStation = prefix + src.getParentStation();
        //}
        this.wheelchairBoarding = (short) src.getWheelchairBoarding();
        this.platformCode = src.getPlatformCode();
        // src.getVehicleType(); // CHECK: Do we need this?
        // src.getDirection(); // CHECK: Do we need this?
    }
}
