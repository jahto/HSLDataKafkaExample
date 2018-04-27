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
import fi.ahto.example.traffic.data.contracts.internal.ShapeSet;
import java.util.HashMap;
import java.util.Map;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.StopTime;

/**
 *
 * @author Jouni Ahto
 */
public class ShapeCollector {
    public Map<String, ShapeSet> shapes = new HashMap();

    public void add(String prefix, ShapePoint sp) {
        String shape = prefix + sp.getShapeId().getId();
        
        ShapeSet set = shapes.get(shape);
        
        if (set == null) {
            set = new ShapeSet();
            shapes.put(shape, set);
        }
        
        ShapeData sd = new ShapeData();
        sd.latitude = sp.getLat();
        sd.longitude = sp.getLon();
        sd.seq = sp.getSequence();
        set.add(sd);
    }
}
