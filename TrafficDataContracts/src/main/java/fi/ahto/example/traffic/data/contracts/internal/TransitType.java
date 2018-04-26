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
package fi.ahto.example.traffic.data.contracts.internal;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jouni Ahto
 */
public enum TransitType {
    UNKNOWN(-1),
    TRAM(0),
    METRO(1),
    TRAIN(2),
    BUS(3),
    FERRY(4),
    // The next ones do not match with each other in GTFS and Siri enum VehicleMode
    // First ones from GTFS
    CABLECAR(5),
    AERIALLIFT(6),
    FUNICULAR(7),
    // And the rest from Siri
    COACH(8),
    UNDERGROUND(9),
    AIR(10)
    ;
    
    private final int value;
    private static final Map<Integer, TransitType> map = new HashMap<>();
    
    private TransitType(int val) {
        value = val;
    }
    
    static {
        for (TransitType type : TransitType.values()) {
            map.put(type.value, type);
        }
    }
    
    public TransitType from(int val) {
        return map.getOrDefault(val, UNKNOWN);
    }
}
