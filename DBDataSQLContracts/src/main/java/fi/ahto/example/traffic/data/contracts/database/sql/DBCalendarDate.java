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
import org.onebusaway.gtfs.model.ServiceCalendarDate;

/**
 *
 * @author Jouni Ahto
 */
@Data
@Entity
public class DBCalendarDate implements Serializable {
    
    private static final long serialVersionUID = 6416239279270803561L;

    @Id
    @Column(name = "calendar_date_num")
    private Long calendarDateNum;

    @Column(name = "service_id")
    private String serviceId;
    @Column(name = "exception_date")
    private LocalDate exceptionDate;
    @Column(name = "exception_type")
    private short exceptionType;
    
    protected DBCalendarDate() {}
    
    public DBCalendarDate(String prefix, ServiceCalendarDate src) {
        this.serviceId = prefix + src.getServiceId().getId();
        this.exceptionDate = Helpers.from(src.getDate());
        this.exceptionType = (short) src.getExceptionType();
    }
    /*
    //@Column(name = "service_id")
    public String getServiceId() {
        return serviceId;
    }
    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    //@Column(name = "exception_date")
    public LocalDate getExceptionDate() {
        return exceptionDate;
    }
    public void setExceptionDate(LocalDate exceptionDate) {
        this.exceptionDate = exceptionDate;
    }

    //@Column(name = "exception_type")
    public short getExceptionType() {
        return exceptionType;
    }
    public void setExceptionType(short exceptionType) {
        this.exceptionType = exceptionType;
    }
    */
}
