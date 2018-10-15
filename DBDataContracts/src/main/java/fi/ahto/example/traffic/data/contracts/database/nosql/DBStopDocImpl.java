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
package fi.ahto.example.traffic.data.contracts.database.nosql;

import fi.ahto.example.traffic.data.contracts.database.DBStop;
import fi.ahto.example.traffic.data.contracts.database.base.DBStopBase;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 *
 * @author Jouni Ahto
 */
// @Entity
public class DBStopDocImpl extends DBStopBase implements DBStop, Serializable {
    
    private static final long serialVersionUID = -1803816257749624895L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String generatedId;

    public String getGeneratedId() {
        return generatedId;
    }

    public void setGeneratedId(String generatedId) {
        this.generatedId = generatedId;
    }
}
