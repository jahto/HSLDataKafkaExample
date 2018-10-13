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

import fi.ahto.example.traffic.data.contracts.internal.RouteTypeExtended;
import java.io.Serializable;
import javax.persistence.*;
import org.onebusaway.gtfs.model.Route;

/**
 *
 * @author Jouni Ahto
 */
@Entity
public class DBRoute implements Serializable {
    
    private static final long serialVersionUID = 8822391347291155647L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long generatedId;
    
    private String routeId;
    private String shortName;
    private String longName;
    private RouteTypeExtended type;
    private String description;
    private String url;
    
    protected DBRoute() {}
    
    public DBRoute(String prefix, Route src) {
        this.routeId = prefix + src.getId().getId();
        this.shortName = src.getShortName();
        this.longName = src.getLongName();
        this.type = RouteTypeExtended.from(src.getType());
        this.description = src.getDesc();
        this.url = src.getUrl();
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

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public RouteTypeExtended getType() {
        return type;
    }

    public void setType(RouteTypeExtended type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
