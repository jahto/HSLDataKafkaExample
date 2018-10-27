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

import fi.ahto.example.traffic.data.contracts.internal.RouteType;
import java.io.Serializable;
import javax.persistence.*;
import lombok.Data;
import org.onebusaway.gtfs.model.Route;

/**
 *
 * @author Jouni Ahto
 */
@Data
@Entity
public class DBRoute implements Serializable{
    
    private static final long serialVersionUID = 8822391347291155647L;

    @Id
    @Column(name = "route_num")
    private Long routeNum;

    @Column(name = "route_id")
    private String routeId;
    @Column(name = "short_name")
    private String shortName;
    @Column(name = "long_name")
    private String longName;
    @Column(name = "route_type")
    private RouteType type;
    @Column(name = "description")
    private String description;
    @Column(name = "url")
    private String url;
    
    protected DBRoute() {}
    
    public DBRoute(String prefix, Route src) {
        this.routeId = prefix + src.getId().getId();
        this.shortName = src.getShortName();
        this.longName = src.getLongName();
        this.type = RouteType.from(src.getType());
        this.description = src.getDesc();
        this.url = src.getUrl();
        
        // There are a few cases where incoming data doesn't have
        // both shortname and longname, although both are required
        // in GTFS specification. Try to fix the situation, so at
        // least the database can have both fields as NOT NULL.
        
        if (this.shortName == null && this.longName != null) {
            this.shortName = this.longName;
        }

        if (this.longName == null && this.shortName != null) {
            this.longName = this.shortName;
        }
    }
    /*
    //@Column(name = "route_id")
    public String getRouteId() {
        return routeId;
    }
    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    //@Column(name = "short_name")
    public String getShortName() {
        return shortName;
    }
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    //@Column(name = "long_name")
    public String getLongName() {
        return longName;
    }
    public void setLongName(String longName) {
        this.longName = longName;
    }

    //@Column(name = "route_type")
    public RouteType getType() {
        return type;
    }
    public void setType(RouteType type) {
        this.type = type;
    }

    //@Column(name = "description")
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    //@Column(name = "url")
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    */
}
