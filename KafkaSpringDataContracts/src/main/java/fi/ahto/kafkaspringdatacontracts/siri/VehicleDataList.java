/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.kafkaspringdatacontracts.siri;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 *
 * @author Jouni Ahto
 */
public class VehicleDataList {
    @JsonProperty("VehicleDataFlattened")
    private List<VehicleActivityFlattened> vehicleDataFlattened = null;
    
    @JsonProperty("VehicleDataFlattened")
    public List<VehicleActivityFlattened> getVehicleActivity() {
        return vehicleDataFlattened;
    }

    @JsonProperty("VehicleDataFlattened")
    public void setVehicleActivity(List<VehicleActivityFlattened> vehicleDataFlattened) {
        this.vehicleDataFlattened = vehicleDataFlattened;
    }

}
