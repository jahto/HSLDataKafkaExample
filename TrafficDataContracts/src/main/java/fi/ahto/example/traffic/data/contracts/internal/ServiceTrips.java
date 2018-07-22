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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jouni Ahto
 */
// Only for internal use, so we can safely use the most efficient form.
// @JsonFormat(shape=JsonFormat.Shape.ARRAY)
// @JsonPropertyOrder({"TimesForward", "TimesBackward"})
public class ServiceTrips implements Serializable, Partitionable {
    private static final long serialVersionUID = -7159227698102377063L;
    public String route;
    /*
    @JsonProperty("TimesForward")
    public Map<LocalTime, String> timesforward = new HashMap<>();
    @JsonProperty("TimesBackward")
    public Map<LocalTime, String> timesbackward = new HashMap<>();
    */
    //@JsonProperty("TimesForward")
    //public Map<Integer, String> timesforward = new HashMap<>();
    //@JsonProperty("TimesBackward")
    //public Map<Integer, String> timesbackward = new HashMap<>();
    @JsonProperty("StartTimes")
    public Map<Integer, String> starttimes = new HashMap<>();

    @Override
    public byte[] getKeyBytes() {
        if (route == null) {
            return null;
        }
        return route.getBytes();
    }
}
