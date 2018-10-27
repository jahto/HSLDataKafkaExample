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
package fi.ahto.example.traffic.data.contracts.database.sql;

import fi.ahto.example.traffic.data.contracts.utils.Helpers;
import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.*;
import lombok.Data;
import org.onebusaway.gtfs.model.ServiceCalendar;

/**
 *
 * @author Jouni Ahto
 */
@Data
@Entity
public class DBCalendar implements Serializable {

    private static final long serialVersionUID = -4669208302827540551L;

    @Id
    @Column(name = "service_num")
    private Long serviceNum;

    @Column(name = "service_id")
    private String serviceId;
    @Column(name = "valid_from")
    private LocalDate validFrom;
    @Column(name = "valid_until")
    private LocalDate validUntil;
    @Column(name = "monday")
    private boolean monday;
    @Column(name = "tuesday")
    private boolean tuesday;
    @Column(name = "wednesday")
    private boolean wednesday;
    @Column(name = "thursday")
    private boolean thursday;
    @Column(name = "friday")
    private boolean friday;
    @Column(name = "saturday")
    private boolean saturday;
    @Column(name = "sunday")
    private boolean sunday;

    protected DBCalendar() {
    }

    public DBCalendar(String prefix, ServiceCalendar src) {
        this.serviceId = prefix + src.getServiceId().getId();
        this.validFrom = Helpers.from(src.getStartDate());
        this.validUntil = Helpers.from(src.getEndDate());
        this.monday = src.getMonday() == 1;
        this.tuesday = src.getTuesday() == 1;
        this.wednesday = src.getWednesday() == 1;
        this.thursday = src.getThursday() == 1;
        this.friday = src.getFriday() == 1;
        this.saturday = src.getSaturday() == 1;
        this.sunday = src.getSunday() == 1;
    }
    /*
    //@Column(name = "service_id")
    public String getServiceId() {
        return serviceId;
    }
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    //@Column(name = "valid_from")
    public LocalDate getValidFrom() {
        return validFrom;
    }
    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    //@Column(name = "valid_until")
    public LocalDate getValidUntil() {
        return validUntil;
    }
    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    //@Column(name = "monday")
    public boolean isMonday() {
        return monday;
    }
    public void setMonday(boolean monday) {
        this.monday = monday;
    }
    
    //@Column(name = "tuesday")
    public boolean isTuesday() {
        return tuesday;
    }
    public void setTuesday(boolean tuesday) {
        this.tuesday = tuesday;
    }
    
    //@Column(name = "wednesday")
    public boolean isWednesday() {
        return wednesday;
    }
    public void setWednesday(boolean wednesday) {
        this.wednesday = wednesday;
    }
    
    //@Column(name = "thursday")
    public boolean isThursday() {
        return thursday;
    }
    public void setThursday(boolean thursday) {
        this.thursday = thursday;
    }
    
    //@Column(name = "friday")
    public boolean isFriday() {
        return friday;
    }
    public void setFriday(boolean friday) {
        this.friday = friday;
    }
    
    //@Column(name = "saturday")
    public boolean isSaturday() {
        return saturday;
    }
    public void setSaturday(boolean saturday) {
        this.saturday = saturday;
    }
    
    //@Column(name = "sunday")
    public boolean isSunday() {
        return sunday;
    }
    public void setSunday(boolean sunday) {
        this.sunday = sunday;
    }
    */
}
