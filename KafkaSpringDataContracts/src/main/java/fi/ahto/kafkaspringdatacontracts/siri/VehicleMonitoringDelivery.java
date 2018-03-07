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
package fi.ahto.kafkaspringdatacontracts.siri;

/**
 *
 * @author Jouni Ahto
 */
import java.util.HashMap;
import java.util.List;
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
    "version",
    "ResponseTimestamp",
    "Status",
    "VehicleActivity"
})
public class VehicleMonitoringDelivery {

    @JsonProperty("version")
    private String version;
    @JsonProperty("ResponseTimestamp")
    private Instant responseTimestamp;
    @JsonProperty("Status")
    private Boolean status;
    @JsonProperty("VehicleActivity")
    private List<VehicleActivity> vehicleActivity = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("ResponseTimestamp")
    public Instant getResponseTimestamp() {
        return responseTimestamp;
    }

    @JsonProperty("ResponseTimestamp")
    public void setResponseTimestamp(Instant responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    @JsonProperty("Status")
    public Boolean getStatus() {
        return status;
    }

    @JsonProperty("Status")
    public void setStatus(Boolean status) {
        this.status = status;
    }

    @JsonProperty("VehicleActivity")
    public List<VehicleActivity> getVehicleActivity() {
        return vehicleActivity;
    }

    @JsonProperty("VehicleActivity")
    public void setVehicleActivity(List<VehicleActivity> vehicleActivity) {
        this.vehicleActivity = vehicleActivity;
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
        return new ToStringBuilder(this).append("version", version).append("responseTimestamp", responseTimestamp).append("status", status).append("vehicleActivity", vehicleActivity).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(status).append(additionalProperties).append(vehicleActivity).append(responseTimestamp).append(version).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof VehicleMonitoringDelivery) == false) {
            return false;
        }
        VehicleMonitoringDelivery rhs = ((VehicleMonitoringDelivery) other);
        return new EqualsBuilder().append(status, rhs.status).append(additionalProperties, rhs.additionalProperties).append(vehicleActivity, rhs.vehicleActivity).append(responseTimestamp, rhs.responseTimestamp).append(version, rhs.version).isEquals();
    }

}
