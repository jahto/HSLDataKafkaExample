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
    "ResponseTimestamp",
    "ProducerRef",
    "Status",
    "MoreData",
    "VehicleMonitoringDelivery"
})
public class ServiceDelivery {

    @JsonProperty("ResponseTimestamp")
    private Instant responseTimestamp;
    @JsonProperty("ProducerRef")
    private ProducerRef producerRef;
    @JsonProperty("Status")
    private Boolean status;
    @JsonProperty("MoreData")
    private Boolean moreData;
    @JsonProperty("VehicleMonitoringDelivery")
    private List<VehicleMonitoringDelivery> vehicleMonitoringDelivery = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("ResponseTimestamp")
    public Instant getResponseTimestamp() {
        return responseTimestamp;
    }

    @JsonProperty("ResponseTimestamp")
    public void setResponseTimestamp(Instant responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    @JsonProperty("ProducerRef")
    public ProducerRef getProducerRef() {
        return producerRef;
    }

    @JsonProperty("ProducerRef")
    public void setProducerRef(ProducerRef producerRef) {
        this.producerRef = producerRef;
    }

    @JsonProperty("Status")
    public Boolean getStatus() {
        return status;
    }

    @JsonProperty("Status")
    public void setStatus(Boolean status) {
        this.status = status;
    }

    @JsonProperty("MoreData")
    public Boolean getMoreData() {
        return moreData;
    }

    @JsonProperty("MoreData")
    public void setMoreData(Boolean moreData) {
        this.moreData = moreData;
    }

    @JsonProperty("VehicleMonitoringDelivery")
    public List<VehicleMonitoringDelivery> getVehicleMonitoringDelivery() {
        return vehicleMonitoringDelivery;
    }

    @JsonProperty("VehicleMonitoringDelivery")
    public void setVehicleMonitoringDelivery(List<VehicleMonitoringDelivery> vehicleMonitoringDelivery) {
        this.vehicleMonitoringDelivery = vehicleMonitoringDelivery;
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
        return new ToStringBuilder(this).append("responseTimestamp", responseTimestamp).append("producerRef", producerRef).append("status", status).append("moreData", moreData).append("vehicleMonitoringDelivery", vehicleMonitoringDelivery).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(moreData).append(status).append(additionalProperties).append(vehicleMonitoringDelivery).append(responseTimestamp).append(producerRef).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ServiceDelivery) == false) {
            return false;
        }
        ServiceDelivery rhs = ((ServiceDelivery) other);
        return new EqualsBuilder().append(moreData, rhs.moreData).append(status, rhs.status).append(additionalProperties, rhs.additionalProperties).append(vehicleMonitoringDelivery, rhs.vehicleMonitoringDelivery).append(responseTimestamp, rhs.responseTimestamp).append(producerRef, rhs.producerRef).isEquals();
    }

}
