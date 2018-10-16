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
@Table(name = "calendar_dates",
        uniqueConstraints =  {
            @UniqueConstraint(name = "calendar_dates_service_id_exception_date_idx", columnNames = {"service_id", "exception_date"})
        },
        indexes = {
            @Index(columnList = ("exception_date"), name = "calendar_dates_exception_date_idx"),
            @Index(columnList = ("service_id"), name = "calendar_dates_service_id_idx")
        })

public interface DBCalendarDate extends Serializable {
    @Id
    @Column(name = "generated_id")
    String getGeneratedId();
    void setGeneratedId(String generatedId);

    @Column(name = "exception_date", nullable = false)
    LocalDate getExceptionDate();
    void setExceptionDate(LocalDate exceptionDate);

    @Column(name = "exception_type", nullable = false)
    short getExceptionType();
    void setExceptionType(short exceptionType);

    @Column(name = "service_id", nullable = false)
    String getServiceId();
    void setServiceId(String serviceId);
}
