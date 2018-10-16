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
@Table(name = "routes",
        uniqueConstraints
        = @UniqueConstraint(name = "routes_route_id_idx", columnNames = {"route_id"})
)
public interface DBRoute extends Serializable {
    @Id
    @Column(name = "generated_id")
    String getGeneratedId();
    void setGeneratedId(String generatedId);

    @Column(name = "description")
    String getDescription();
    void setDescription(String description);

    @Column(name = "long_name", nullable = false)
    String getLongName();
    void setLongName(String longName);

    @NaturalId
    @Column(name = "route_id", nullable = false)
    String getRouteId();
    void setRouteId(String routeId);

    @Column(name = "short_name", nullable = false)
    String getShortName();
    void setShortName(String shortName);

    @Column(name = "route_type", nullable = false)
    RouteTypeExtended getType();
    void setType(RouteTypeExtended type);

    @Column(name = "url")
    String getUrl();
    void setUrl(String url);
}
