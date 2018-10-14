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

import fi.ahto.example.traffic.data.contracts.database.DBFrequency;
import fi.ahto.example.traffic.data.contracts.database.base.DBFrequencyBase;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Jouni Ahto
 */
@Entity
@Table(name = "frequencies")
public class DBFrecuencySQLImpl extends DBFrequencyBase implements DBFrequency, Serializable {
    
    private static final long serialVersionUID = -2593403070543829944L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long generated_id;

    @Override
    public String getGeneratedId() {
        if (generated_id == null) return null;
        return generated_id.toString();
    }

    @Override
    public void setGeneratedId(String generatedId) {
        this.generated_id = Long.parseLong(generatedId);
    }
}
