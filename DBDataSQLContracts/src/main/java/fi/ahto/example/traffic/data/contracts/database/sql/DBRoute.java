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

import fi.ahto.example.traffic.data.contracts.internal.RouteData;
import fi.ahto.example.traffic.data.contracts.internal.RouteType;
import fi.ahto.example.traffic.data.contracts.utils.RouteTypeConverter;
import java.io.Serializable;
import javax.persistence.*;
import lombok.Data;

/**
 *
 * @author Jouni Ahto
 */
@Data
@Entity
@javax.persistence.Table(name = "routes")
@org.springframework.data.relational.core.mapping.Table(value = "routes")
public class DBRoute implements Serializable{
    
    private static final long serialVersionUID = 8822391347291155647L;

    @javax.persistence.Id
    @org.springframework.data.annotation.Id
    @GeneratedValue
    @javax.persistence.Column(name = "route_num")
    @org.springframework.data.relational.core.mapping.Column(value = "route_num")
    private Long routeNum;

    @javax.persistence.Column(name = "route_id")
    @org.springframework.data.relational.core.mapping.Column(value = "route_id")
    private String routeId;
    @javax.persistence.Column(name = "short_name")
    @org.springframework.data.relational.core.mapping.Column(value = "short_name")
    private String shortName;
    @javax.persistence.Column(name = "long_name")
    @org.springframework.data.relational.core.mapping.Column(value = "long_name")
    private String longName;
    @Convert(converter = RouteTypeConverter.class)
    @javax.persistence.Column(name = "route_type")
    @org.springframework.data.relational.core.mapping.Column(value = "route_type")
    private RouteType type;
    @javax.persistence.Column(name = "description")
    @org.springframework.data.relational.core.mapping.Column(value = "description")
    private String description;
    @javax.persistence.Column(name = "url")
    @org.springframework.data.relational.core.mapping.Column(value = "url")
    private String url;
    
    protected DBRoute() {}
    
    public DBRoute(RouteData src) {
        this.routeId = src.routeid;
        this.shortName = src.shortname;
        this.longName = src.longname;
        this.type = src.type;
        this.description = src.description;
        this.url = src.url;
        
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
}
