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
import java.util.Objects;

/**
 *
 * @author Jouni Ahto
 */
// Only for internal use, so we can safely use the most efficient form.
// @JsonFormat(shape=JsonFormat.Shape.ARRAY)
// @JsonPropertyOrder({"StopId", "Sequence", "ArrivalTime", "Name>"})
public class ServiceStop implements Serializable {
    private static final long serialVersionUID = -4066234945648076125L;
    
    @JsonProperty("StopId")
    public String stopid;
    @JsonProperty("Sequence")
    public int seq;
    @JsonProperty("ArrivalTime")
    public LocalTime arrivalTime;
    @JsonProperty("Name")
    public String name;

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.stopid);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ServiceStop other = (ServiceStop) obj;
        if (!Objects.equals(this.stopid, other.stopid)) {
            return false;
        }
        return true;
    }
}
