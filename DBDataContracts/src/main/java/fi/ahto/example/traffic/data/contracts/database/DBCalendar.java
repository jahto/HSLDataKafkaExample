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
import javax.persistence.Table;

/**
 *
 * @author Jouni Ahto
 */
@Entity
@Table(name = "calendar")
public interface DBCalendar extends Serializable {
    @Id
    @Column(name = "generated_id")

    String getGeneratedId();

    void setGeneratedId(String generatedId);

    String getServiceId();

    LocalDate getValidFrom();

    LocalDate getValidUntil();

    boolean isFriday();

    boolean isMonday();

    boolean isSaturday();

    boolean isSunday();

    boolean isThursday();

    boolean isTuesday();

    boolean isWednesday();

    void setFriday(boolean friday);

    void setMonday(boolean monday);

    void setSaturday(boolean saturday);

    void setServiceId(String serviceId);

    void setSunday(boolean sunday);

    void setThursday(boolean thursday);

    void setTuesday(boolean tuesday);

    void setValidFrom(LocalDate validFrom);

    void setValidUntil(LocalDate validUntil);

    void setWednesday(boolean wednesday);
    
}
