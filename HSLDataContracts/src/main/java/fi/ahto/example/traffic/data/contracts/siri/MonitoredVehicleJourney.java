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
package fi.ahto.example.traffic.data.contracts.siri;

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
import java.time.Duration;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MonitoredVehicleJourney {

    public Double getBearing() {
        return bearing;
    }

    public void setBearing(Double bearing) {
        this.bearing = bearing;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public String getJourneyPatternRef() {
        return journeyPatternRef;
    }

    public void setJourneyPatternRef(String journeyPatternRef) {
        this.journeyPatternRef = journeyPatternRef;
    }

    public String getOriginShortName() {
        return originShortName;
    }

    public void setOriginShortName(String originShortName) {
        this.originShortName = originShortName;
    }

    public String getDestinationShortName() {
        return destinationShortName;
    }

    public void setDestinationShortName(String destinationShortName) {
        this.destinationShortName = destinationShortName;
    }

    public String getOriginAimedDepartureTime() {
        return originAimedDepartureTime;
    }

    public void setOriginAimedDepartureTime(String originAimedDepartureTime) {
        this.originAimedDepartureTime = originAimedDepartureTime;
    }

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
    @JsonProperty("Bearing")
    private Double bearing;
    @JsonProperty("Speed")
    private Double speed;
    @JsonProperty("JourneyPatternRef")
    private String journeyPatternRef;
    @JsonProperty("OriginShortName")
    private String originShortName;
    @JsonProperty("DestinationShortName")
    private String destinationShortName;
    @JsonProperty("OriginAimedDepartureTime")
    private String originAimedDepartureTime;
    
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
        // At least HSL uses plain seconds.
        try {
            delaySeconds = Integer.parseInt(delay);
            return;
        }
        catch (NumberFormatException e) {
            isValid = false;
        }
        // At least TKL uses "-P0Y0M0DT0H0M6.000S\" -format.
        // "^([+-]?P)\\d+Y\\d+M(\\d+DT\\d+H\\d+M\\d+\\.\\d+)S$"
        String exp = "^([+-]?P)\\d+Y\\d+M(\\d+DT\\d+H\\d+M\\d+\\.\\d+S)$";
        
        if (delay.matches(exp)) {
            String res = delay.replaceAll(exp, "$1$2");
            Duration dur = Duration.parse(res);
            delaySeconds = (int) dur.getSeconds();
            isValid = true;
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
}
