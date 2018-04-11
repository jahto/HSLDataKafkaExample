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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "DataFrameRef",
    "DatedVehicleJourneyRef"
})
public class FramedVehicleJourneyRef {

    @JsonProperty("DataFrameRef")
    private DataFrameRef dataFrameRef;
    @JsonProperty("DatedVehicleJourneyRef")
    private String datedVehicleJourneyRef;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("DataFrameRef")
    public DataFrameRef getDataFrameRef() {
        return dataFrameRef;
    }

    @JsonProperty("DataFrameRef")
    public void setDataFrameRef(DataFrameRef dataFrameRef) {
        this.dataFrameRef = dataFrameRef;
    }

    @JsonProperty("DatedVehicleJourneyRef")
    public String getDatedVehicleJourneyRef() {
        return datedVehicleJourneyRef;
    }

    @JsonProperty("DatedVehicleJourneyRef")
    public void setDatedVehicleJourneyRef(String datedVehicleJourneyRef) {
        this.datedVehicleJourneyRef = datedVehicleJourneyRef;
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
        return new ToStringBuilder(this).append("dataFrameRef", dataFrameRef).append("datedVehicleJourneyRef", datedVehicleJourneyRef).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(datedVehicleJourneyRef).append(additionalProperties).append(dataFrameRef).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FramedVehicleJourneyRef) == false) {
            return false;
        }
        FramedVehicleJourneyRef rhs = ((FramedVehicleJourneyRef) other);
        return new EqualsBuilder().append(datedVehicleJourneyRef, rhs.datedVehicleJourneyRef).append(additionalProperties, rhs.additionalProperties).append(dataFrameRef, rhs.dataFrameRef).isEquals();
    }

}
