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

import fi.ahto.example.traffic.data.contracts.internal.ServiceData;
import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.*;
import lombok.Data;

/**
 *
 * @author Jouni Ahto
 */
@Data
@Entity
@javax.persistence.Table(name = "calendars")
@org.springframework.data.relational.core.mapping.Table(value = "calendars")
public class DBCalendar implements Serializable {

    private static final long serialVersionUID = -4669208302827540551L;

    @javax.persistence.Id
    @org.springframework.data.annotation.Id
    @GeneratedValue
    @javax.persistence.Column(name = "service_num")
    @org.springframework.data.relational.core.mapping.Column(value = "service_num")
    private Long serviceNum;

    @javax.persistence.Column(name = "service_id")
    @org.springframework.data.relational.core.mapping.Column(value = "service_id")
    private String serviceId;
    @javax.persistence.Column(name = "valid_from")
    @org.springframework.data.relational.core.mapping.Column(value = "valid_from")
    private LocalDate validFrom;
    @javax.persistence.Column(name = "valid_until")
    @org.springframework.data.relational.core.mapping.Column(value = "valid_until")
    private LocalDate validUntil;
    @javax.persistence.Column(name = "monday")
    @org.springframework.data.relational.core.mapping.Column(value = "monday")
    private boolean monday;
    @javax.persistence.Column(name = "tuesday")
    @org.springframework.data.relational.core.mapping.Column(value = "tuesday")
    private boolean tuesday;
    @javax.persistence.Column(name = "wednesday")
    @org.springframework.data.relational.core.mapping.Column(value = "wednesday")
    private boolean wednesday;
    @javax.persistence.Column(name = "thursday")
    @org.springframework.data.relational.core.mapping.Column(value = "thursday")
    private boolean thursday;
    @javax.persistence.Column(name = "friday")
    @org.springframework.data.relational.core.mapping.Column(value = "friday")
    private boolean friday;
    @javax.persistence.Column(name = "saturday")
    @org.springframework.data.relational.core.mapping.Column(value = "saturday")
    private boolean saturday;
    @javax.persistence.Column(name = "sunday")
    @org.springframework.data.relational.core.mapping.Column(value = "sunday")
    private boolean sunday;
    /*
    @javax.persistence.Column(name = "weekdays")
    @org.springframework.data.relational.core.mapping.Column(value = "weekdays")
    private byte weekdays;
    */
    protected DBCalendar() {
    }

    public DBCalendar(ServiceData src) {
        this.serviceId = src.getServiceId();
        this.validFrom = src.getValidFrom();
        this.validUntil = src.getValidUntil();
        this.monday = src.isMonday();
        this.tuesday = src.isTuesday();
        this.wednesday = src.isWednesday();
        this.thursday = src.isThursday();
        this.friday = src.isFriday();
        this.saturday = src.isSaturday();
        this.sunday = src.isSunday();
        // this.weekdays = src.weekdays;
    }
}
