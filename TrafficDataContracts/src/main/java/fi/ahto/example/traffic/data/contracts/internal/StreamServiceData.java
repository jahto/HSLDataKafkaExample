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

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jouni Ahto
 */

public class StreamServiceData implements Serializable {

    public void setValidFrom(LocalDate validfrom) {
        this.validFrom = validfrom;
    }

    public void setValidUntil(LocalDate validuntil) {
        this.validUntil = validuntil;
    }

    public byte getWeekdays() {
        return weekdays;
    }

    public void setWeekdays(byte weekdays) {
        this.weekdays = weekdays;
    }
    private static final long serialVersionUID = 3738645659204806594L;

    public String getServiceId() {
        return this.serviceId;
    }
    
    public void setServiceId(String id) {
        this.serviceId = id;
    }
    
    public LocalDate getValidFrom() {
        return this.validFrom;
    }
    
    public LocalDate getValidUntil() {
        return this.validUntil;
    }
    
    public List<LocalDate> getInUse() {
        return this.inuse;
    }
    
    public List<LocalDate> getNotInUse() {
        return this.notinuse;
    }

    public byte getExtra() {
        return extra;
    }

    public void setExtra(byte extra) {
        this.extra = extra;
    }

    @org.springframework.data.annotation.Id
    private String serviceId;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private final List<LocalDate> inuse = new ArrayList<>();
    private final List<LocalDate> notinuse = new ArrayList<>();
    private byte weekdays = 0;
    // Oh hell, we need an extra field for special flags...
    private byte extra = 0;
}
