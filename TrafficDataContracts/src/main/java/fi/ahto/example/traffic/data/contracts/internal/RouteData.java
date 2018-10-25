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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Jouni Ahto
 */
public class RouteData implements Serializable {

    private static final long serialVersionUID = -1109210000681896888L;
    @JsonProperty("RouteId")
    public String routeid;
    @JsonProperty("ShortName")
    public String shortname;
    @JsonProperty("LongName")
    public String longname;
    @JsonProperty("TransitType")
    public RouteType type = RouteType.UNKNOWN;
    
    @JsonProperty("Stops")
    public List<RouteStop> stops = new ArrayList<>();
    
    public static class RouteStop implements Serializable {

        private static final long serialVersionUID = -3579965916298485253L;

        @JsonProperty("StopId")
        public String stopid;

        public RouteStop() {}
        
        public RouteStop(String id) {
            this.stopid = id;
        }
        
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 61 * hash + Objects.hashCode(this.stopid);
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
            final RouteStop other = (RouteStop) obj;
            if (!Objects.equals(this.stopid, other.stopid)) {
                return false;
            }
            return true;
        }
    }
}
