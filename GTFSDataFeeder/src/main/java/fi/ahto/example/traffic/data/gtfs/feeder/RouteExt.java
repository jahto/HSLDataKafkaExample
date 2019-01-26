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
package fi.ahto.example.traffic.data.gtfs.feeder;

import fi.ahto.example.traffic.data.contracts.internal.RouteData;
import fi.ahto.example.traffic.data.contracts.internal.RouteType;
import org.onebusaway.gtfs.model.Route;

/**
 *
 * @author Jouni Ahto
 */
public class RouteExt {

    private final String prefix;
    private final String key;
    private final Route rt;

    public RouteExt(String prefix, Route rt) {
        this.prefix = prefix;
        this.key = prefix + rt.getId().getId();
        this.rt = rt;
    }

    public RouteData toRouteData() {
        RouteData route = new RouteData();
        route.routeid = this.getKey();
        route.longname = this.getLongName();
        route.shortname = this.getShortName();
        route.type = RouteType.from(this.getType());
        route.description = this.getDesc();
        route.url = this.getUrl();
        return route;
    }

    public String getKey() {
        return key;
    }

    public String getShortName() {
        return rt.getShortName();
    }

    public String getLongName() {
        return rt.getLongName();
    }

    public String getDesc() {
        return rt.getDesc();
    }

    public int getType() {
        return rt.getType();
    }

    public String getUrl() {
        return rt.getUrl();
    }

    public String getColor() {
        return rt.getColor();
    }

    public String getTextColor() {
        return rt.getTextColor();
    }

    public int getBikesAllowed() {
        return rt.getBikesAllowed();
    }
}
