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

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.Objects;

/**
 *
 * @author Jouni Ahto
 */

public class TripStop implements Serializable {
    private static final long serialVersionUID = -3422464128608239456L;
    
    @JsonProperty("StopId")
    public String stopid;
    @JsonProperty("Sequence")
    public int seq;
    @JsonProperty("ArrivalTime")
    public GTFSLocalTime arrivalTime;

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.stopid);
        hash = 37 * hash + this.seq;
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
        final TripStop other = (TripStop) obj;
        if (this.seq != other.seq) {
            return false;
        }
        if (!Objects.equals(this.stopid, other.stopid)) {
            return false;
        }
        return true;
    }

}
