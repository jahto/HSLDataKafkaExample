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
@javax.persistence.Table(name = "calendar_dates")
@org.springframework.data.relational.core.mapping.Table(value = "calendar_dates")
public class DBCalendarDate implements Serializable {
    
    private static final long serialVersionUID = 6416239279270803561L;

    @javax.persistence.Id
    @org.springframework.data.annotation.Id
    @GeneratedValue
    @javax.persistence.Column(name = "calendar_date_num")
    @org.springframework.data.relational.core.mapping.Column(value = "calendar_date_num")
    private Long calendarDateNum;

    // @javax.persistence.Id
    // @org.springframework.data.annotation.Id
    @javax.persistence.Column(name = "service_num")
    @org.springframework.data.relational.core.mapping.Column(value = "service_num")
    private Long serviceId;

    // @javax.persistence.Id
    // @org.springframework.data.annotation.Id
    @javax.persistence.Column(name = "exception_date")
    @org.springframework.data.relational.core.mapping.Column(value = "exception_date")
    private LocalDate exceptionDate;
    @javax.persistence.Column(name = "exception_type")
    @org.springframework.data.relational.core.mapping.Column(value = "exception_type")
    private short exceptionType;
    
    public DBCalendarDate() {}
}
