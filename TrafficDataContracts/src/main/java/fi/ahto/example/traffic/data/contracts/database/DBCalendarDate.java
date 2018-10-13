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
import javax.persistence.*;
import org.onebusaway.gtfs.model.ServiceCalendarDate;

/**
 *
 * @author Jouni Ahto
 */
@Entity
public class DBCalendarDate implements Serializable {
    
    private static final long serialVersionUID = 6416239279270803561L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long generatedId;
    
    private String serviceId;
    private LocalDate exceptionDate;
    private short exceptionType;
    
    protected DBCalendarDate() {}
    
    public DBCalendarDate(String prefix, ServiceCalendarDate src) {
        this.serviceId = prefix + src.getServiceId();
        this.exceptionDate = Helpers.from(src.getDate());
        this.exceptionType = (short) src.getExceptionType();
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getGeneratedId() {
        return generatedId;
    }

    public void setGeneratedId(Long generatedId) {
        this.generatedId = generatedId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public LocalDate getExceptionDate() {
        return exceptionDate;
    }

    public void setExceptionDate(LocalDate exceptionDate) {
        this.exceptionDate = exceptionDate;
    }

    public short getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(short exceptionType) {
        this.exceptionType = exceptionType;
    }
    
}
