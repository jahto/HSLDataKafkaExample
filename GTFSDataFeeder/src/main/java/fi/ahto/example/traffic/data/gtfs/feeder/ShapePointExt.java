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

import fi.ahto.example.traffic.data.contracts.internal.ShapeData;
import org.onebusaway.gtfs.model.ShapePoint;

/**
 *
 * @author Jouni Ahto
 */
public class ShapePointExt {

    private final String prefix;
    private final String key;
    private final ShapePoint rt;

    public ShapePointExt(String prefix, ShapePoint rt) {
        this.prefix = prefix;
        this.key = prefix + rt.getShapeId().getId();
        this.rt = rt;
    }

    public ShapeData toShapeData() {
        ShapeData shape = new ShapeData();
        shape.latitude = this.getLat();
        shape.longitude = this.getLon();
        shape.seq = this.getSequence();
        shape.distTraveled = this.getDistTraveled();
        return shape;
    }

    public String getKey() {
        return key;
    }

    public int getSequence() {
        return rt.getSequence();
    }

    public Double getDistTraveled() {
        if (rt.isDistTraveledSet()) {
            return rt.getDistTraveled();
        }
        return null;
    }

    public double getLat() {
        return rt.getLat();
    }

    public double getLon() {
        return rt.getLon();
    }
}
