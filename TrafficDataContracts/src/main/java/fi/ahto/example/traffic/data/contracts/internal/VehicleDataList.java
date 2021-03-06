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

/**
 *
 * @author Jouni Ahto
 */
public class VehicleDataList implements Serializable {
    private static final long serialVersionUID = -5693004336997523400L;
    @JsonProperty("VehicleActivities")
    private List<VehicleActivity> vehicleActivities = new ArrayList<>();
    
    @JsonProperty("VehicleActivities")
    public List<VehicleActivity> getVehicleActivities() {
        return vehicleActivities;
    }
    
    @JsonProperty("VehicleActivities")
    public void setVehicleActivities(List<VehicleActivity> activities) {
        this.vehicleActivities = activities;
    }
}
