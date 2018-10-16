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
package fi.ahto.example.traffic.data.contracts.database;

import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 *
 * @author Jouni Ahto
 */
@Entity
@Table(name = "calendars",
        uniqueConstraints =  {
            @UniqueConstraint(name = "calendars_service_id_valid_from_idx", columnNames = {"service_id", "valid_from"})
        },
        indexes = {
            @Index(columnList = ("service_id"), name = "calendars_service_id_idx")
        })
public interface DBCalendar extends Serializable {
    @Id
    @Column(name = "generated_id")
    String getGeneratedId();
    void setGeneratedId(String generatedId);

    @Column(name = "service_id", nullable = false)
    String getServiceId();
    void setServiceId(String serviceId);

    @Column(name = "valid_from", nullable = false)
    LocalDate getValidFrom();
    void setValidFrom(LocalDate validFrom);

    @Column(name = "valid_until", nullable = false)
    LocalDate getValidUntil();
    void setValidUntil(LocalDate validUntil);

    @Column(name = "monday", nullable = false)
    boolean isMonday();
    void setMonday(boolean monday);

    @Column(name = "tuesday", nullable = false)
    boolean isTuesday();
    void setTuesday(boolean tuesday);

    @Column(name = "wednesday", nullable = false)
    boolean isWednesday();
    void setWednesday(boolean wednesday);

    @Column(name = "thursday", nullable = false)
    boolean isThursday();
    void setThursday(boolean thursday);

    @Column(name = "friday", nullable = false)
    boolean isFriday();
    void setFriday(boolean friday);

    @Column(name = "saturday", nullable = false)
    boolean isSaturday();
    void setSaturday(boolean saturday);

    @Column(name = "sunday", nullable = false)
    boolean isSunday();
    void setSunday(boolean sunday);
}
