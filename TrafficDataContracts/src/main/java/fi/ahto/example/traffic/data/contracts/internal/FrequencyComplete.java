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
package fi.ahto.example.traffic.data.contracts.internal;

import fi.ahto.example.traffic.data.contracts.utils.GTFSLocalTimeConverter;
import javax.persistence.Convert;
import org.onebusaway.gtfs.model.Frequency;

/**
 *
 * @author Jouni Ahto
 */
public class FrequencyComplete {
    public FrequencyComplete() {}

    public FrequencyComplete(String prefix, Frequency fr) {
        this.startTime = GTFSLocalTime.ofSecondOfDay(fr.getStartTime());
        this.endTime = GTFSLocalTime.ofSecondOfDay(fr.getEndTime());
        this.headwaySecs = (short) fr.getHeadwaySecs();
        this.exactTimes = (short) fr.getExactTimes();
    }
    
    public GTFSLocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(GTFSLocalTime startTime) {
        this.startTime = startTime;
    }

    public GTFSLocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(GTFSLocalTime endTime) {
        this.endTime = endTime;
    }

    public short getHeadwaySecs() {
        return headwaySecs;
    }

    public void setHeadwaySecs(short headwaySecs) {
        this.headwaySecs = headwaySecs;
    }

    public short getExactTimes() {
        return exactTimes;
    }

    public void setExactTimes(short exactTimes) {
        this.exactTimes = exactTimes;
    }

    // private String tripId;
    @Convert(converter = GTFSLocalTimeConverter.class)
    private GTFSLocalTime startTime;
    @Convert(converter = GTFSLocalTimeConverter.class)
    private GTFSLocalTime endTime;
    private short headwaySecs;
    private short exactTimes; // TODO: Check if this actually boolean.
}
