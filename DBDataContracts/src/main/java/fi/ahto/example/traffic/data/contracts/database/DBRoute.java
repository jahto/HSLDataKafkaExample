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

/**
 *
 * @author Jouni Ahto
 */
// @Entity
@Entity
@Table(name = "routes")
public interface DBRoute extends Serializable {
    @Id
    @Column(name = "generated_id")
            
    String getGeneratedId();

    void setGeneratedId(String generatedId);

    String getDescription();

    String getLongName();

    String getRouteId();

    String getShortName();

    RouteTypeExtended getType();

    String getUrl();

    void setDescription(String description);

    void setLongName(String longName);

    void setRouteId(String routeId);

    void setShortName(String shortName);

    void setType(RouteTypeExtended type);

    void setUrl(String url);

}
