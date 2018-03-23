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
import java.time.Instant;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "ValidUntilTime",
    "RecordedAtTime",
    "MonitoredVehicleJourney"
})
public class VehicleActivity {

    @JsonProperty("ValidUntilTime")
    private Instant validUntilTime;
    @JsonProperty("RecordedAtTime")
    private Instant recordedAtTime;
    @JsonProperty("MonitoredVehicleJourney")
    private MonitoredVehicleJourney monitoredVehicleJourney;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("ValidUntilTime")
    public Instant getValidUntilTime() {
        return validUntilTime;
    }

    @JsonProperty("ValidUntilTime")
    public void setValidUntilTime(Instant validUntilTime) {
        this.validUntilTime = validUntilTime;
    }

    @JsonProperty("RecordedAtTime")
    public Instant getRecordedAtTime() {
        return recordedAtTime;
    }

    @JsonProperty("RecordedAtTime")
    public void setRecordedAtTime(Instant recordedAtTime) {
        this.recordedAtTime = recordedAtTime;
    }

    @JsonProperty("MonitoredVehicleJourney")
    public MonitoredVehicleJourney getMonitoredVehicleJourney() {
        return monitoredVehicleJourney;
    }

    @JsonProperty("MonitoredVehicleJourney")
    public void setMonitoredVehicleJourney(MonitoredVehicleJourney monitoredVehicleJourney) {
        this.monitoredVehicleJourney = monitoredVehicleJourney;
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
        return new ToStringBuilder(this).append("validUntilTime", validUntilTime).append("recordedAtTime", recordedAtTime).append("monitoredVehicleJourney", monitoredVehicleJourney).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(monitoredVehicleJourney).append(additionalProperties).append(recordedAtTime).append(validUntilTime).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof VehicleActivity) == false) {
            return false;
        }
        VehicleActivity rhs = ((VehicleActivity) other);
        return new EqualsBuilder().append(monitoredVehicleJourney, rhs.monitoredVehicleJourney).append(additionalProperties, rhs.additionalProperties).append(recordedAtTime, rhs.recordedAtTime).append(validUntilTime, rhs.validUntilTime).isEquals();
    }

}
