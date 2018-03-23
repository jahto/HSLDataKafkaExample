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
package fi.ahto.example.hsl.data.contracts.siri;

/**
 *
 * @author Jouni Ahto
 */
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "LineRef",
    "DirectionRef",
    "FramedVehicleJourneyRef",
    "OperatorRef",
    "Monitored",
    "VehicleLocation",
    "Delay",
    "MonitoredCall",
    "VehicleRef"
})
public class MonitoredVehicleJourney {

    @JsonProperty("LineRef")
    private LineRef lineRef;
    @JsonProperty("DirectionRef")
    private DirectionRef directionRef;
    @JsonProperty("FramedVehicleJourneyRef")
    private FramedVehicleJourneyRef framedVehicleJourneyRef;
    @JsonProperty("OperatorRef")
    private OperatorRef operatorRef;
    @JsonProperty("Monitored")
    private Boolean monitored;
    @JsonProperty("VehicleLocation")
    private VehicleLocation vehicleLocation;
    @JsonProperty("Delay")
    private String delay;
    @JsonProperty("DelaySeconds")
    private Integer delaySeconds;
    @JsonProperty("MonitoredCall")
    private MonitoredCall monitoredCall;
    @JsonProperty("VehicleRef")
    private VehicleRef vehicleRef;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    @JsonIgnore
    private boolean isValid = true;

    @JsonProperty("LineRef")
    public LineRef getLineRef() {
        return lineRef;
    }

    @JsonProperty("LineRef")
    public void setLineRef(LineRef lineRef) {
        this.lineRef = lineRef;
    }

    @JsonProperty("DirectionRef")
    public DirectionRef getDirectionRef() {
        return directionRef;
    }

    @JsonProperty("DirectionRef")
    public void setDirectionRef(DirectionRef directionRef) {
        this.directionRef = directionRef;
    }

    @JsonProperty("FramedVehicleJourneyRef")
    public FramedVehicleJourneyRef getFramedVehicleJourneyRef() {
        return framedVehicleJourneyRef;
    }

    @JsonProperty("FramedVehicleJourneyRef")
    public void setFramedVehicleJourneyRef(FramedVehicleJourneyRef framedVehicleJourneyRef) {
        this.framedVehicleJourneyRef = framedVehicleJourneyRef;
    }

    @JsonProperty("OperatorRef")
    public OperatorRef getOperatorRef() {
        return operatorRef;
    }

    @JsonProperty("OperatorRef")
    public void setOperatorRef(OperatorRef operatorRef) {
        this.operatorRef = operatorRef;
    }

    @JsonProperty("Monitored")
    public Boolean getMonitored() {
        return monitored;
    }

    @JsonProperty("Monitored")
    public void setMonitored(Boolean monitored) {
        this.monitored = monitored;
    }

    @JsonProperty("VehicleLocation")
    public VehicleLocation getVehicleLocation() {
        return vehicleLocation;
    }

    @JsonProperty("VehicleLocation")
    public void setVehicleLocation(VehicleLocation vehicleLocation) {
        this.vehicleLocation = vehicleLocation;
    }

    @JsonProperty("Delay")
    public String getDelay() {
        return delay;
    }

    @JsonProperty("Delay")
    public void setDelay(String delay) {
        this.delay = delay;
        try {
            delaySeconds = Integer.parseInt(delay);
        }
        catch (NumberFormatException e) {
            isValid = false;
        }
    }

    @JsonProperty("DelaySeconds")
    public Integer getDelaySeconds() {
        return delaySeconds;
    }

    @JsonProperty("DelaySeconds")
    public void setDelaySeconds(Integer delay) {
        this.delaySeconds = delay;
    }

    @JsonProperty("MonitoredCall")
    public MonitoredCall getMonitoredCall() {
        return monitoredCall;
    }

    @JsonProperty("MonitoredCall")
    public void setMonitoredCall(MonitoredCall monitoredCall) {
        this.monitoredCall = monitoredCall;
    }

    @JsonProperty("VehicleRef")
    public VehicleRef getVehicleRef() {
        return vehicleRef;
    }

    @JsonProperty("VehicleRef")
    public void setVehicleRef(VehicleRef vehicleRef) {
        this.vehicleRef = vehicleRef;
    }

    public boolean IsValid() {
        return isValid;
    }
    
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("lineRef", lineRef).append("directionRef", directionRef).append("framedVehicleJourneyRef", framedVehicleJourneyRef).append("operatorRef", operatorRef).append("monitored", monitored).append("vehicleLocation", vehicleLocation).append("delay", delay).append("monitoredCall", monitoredCall).append("vehicleRef", vehicleRef).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(vehicleRef).append(operatorRef).append(framedVehicleJourneyRef).append(vehicleLocation).append(monitoredCall).append(additionalProperties).append(delay).append(monitored).append(directionRef).append(lineRef).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MonitoredVehicleJourney) == false) {
            return false;
        }
        MonitoredVehicleJourney rhs = ((MonitoredVehicleJourney) other);
        return new EqualsBuilder().append(vehicleRef, rhs.vehicleRef).append(operatorRef, rhs.operatorRef).append(framedVehicleJourneyRef, rhs.framedVehicleJourneyRef).append(vehicleLocation, rhs.vehicleLocation).append(monitoredCall, rhs.monitoredCall).append(additionalProperties, rhs.additionalProperties).append(delay, rhs.delay).append(monitored, rhs.monitored).append(directionRef, rhs.directionRef).append(lineRef, rhs.lineRef).isEquals();
    }

}
