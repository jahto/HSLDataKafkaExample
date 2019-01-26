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

import fi.ahto.example.traffic.data.contracts.internal.StopData;
import org.onebusaway.gtfs.model.Stop;

/**
 *
 * @author Jouni Ahto
 */
public class StopExt {

    private final String key;
    private final String prefix;
    private final Stop st;

    public StopExt(String prefix, Stop st) {
        this.prefix = prefix;
        this.key = prefix + st.getId().getId();
        this.st = st;
    }

    public StopData toStopData() {
        StopData stop = new StopData();
        stop.setStopid(key);
        stop.setStopname(this.getName());
        stop.setLatitude(this.getLat());
        stop.setLongitude(this.getLon());
        stop.setStopcode(this.getCode());
        stop.setDesc(this.getDesc());
        stop.setUrl(this.getUrl());
        stop.setLocationType(this.getLocationType());
        stop.setParentStation(this.getParentStation());
        stop.setWheelchairBoarding(this.getWheelchairBoarding());
        stop.setPlatformCode(this.getPlatformCode());
        return stop;
    }

    public String getKey() {
        return key;
    }

    public String getCode() {
        return st.getCode();
    }

    public String getName() {
        return st.getName();
    }

    public String getDesc() {
        return st.getDesc();
    }

    public double getLat() {
        return st.getLat();
    }

    public double getLon() {
        return st.getLon();
    }

    public String getZoneId() {
        return st.getZoneId();
    }

    public String getUrl() {
        return st.getUrl();
    }

    public short getLocationType() {
        return (short) st.getLocationType();
    }

    public String getParentStation() {
        if (st.getParentStation() != null) {
            return prefix + st.getParentStation();
        }
        return null;
    }

    public short getWheelchairBoarding() {
        return (short) st.getWheelchairBoarding();
    }

    public String getDirection() {
        return st.getDirection();
    }

    public String getTimezone() {
        return st.getTimezone();
    }

    public boolean isVehicleTypeSet() {
        return st.isVehicleTypeSet();
    }

    public int getVehicleType() {
        return st.getVehicleType();
    }

    public String getPlatformCode() {
        return st.getPlatformCode();
    }
}
