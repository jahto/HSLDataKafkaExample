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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.NaturalId;

/**
 *
 * @author Jouni Ahto
 */
@Entity
@Table(name = "stops",
        uniqueConstraints = {
            @UniqueConstraint(name = "stops_stop_id_idx", columnNames = {"stop_id"})
        }
)
public interface DBStop extends Serializable {
    @Id
    @Column(name = "generated_id")
    String getGeneratedId();
    void setGeneratedId(String generatedId);

    @Column(name = "latitude", nullable = false)
    float getLatitude();
    void setLatitude(float latitude);

    @Column(name = "location_type", nullable = false)
    short getLocationType();
    void setLocationType(short locationType);

    @Column(name = "longitude")
    float getLongitude();
    void setLongitude(float longitude);

    @Column(name = "parent_station")
    String getParentStation();
    void setParentStation(String parentStation);

    @Column(name = "platform_code")
    String getPlatformCode();
    void setPlatformCode(String platformCode);

    @Column(name = "stop_code")
    String getStopCode();
    void setStopCode(String stopCode);

    @Column(name = "stop_description")
    String getStopDescription();
    void setStopDescription(String stopDescription);

    @NaturalId
    @Column(name = "stop_id", nullable = false)
    String getStopId();
    void setStopId(String stopId);

    @Column(name = "stop_name", nullable = false)
    String getStopName();
    void setStopName(String stopName);

    @Column(name = "url")
    String getUrl();
    void setUrl(String url);

    @Column(name = "wheelchair_boarding")
    short getWheelchairBoarding();
    void setWheelchairBoarding(short wheelchairBoarding);
    }
