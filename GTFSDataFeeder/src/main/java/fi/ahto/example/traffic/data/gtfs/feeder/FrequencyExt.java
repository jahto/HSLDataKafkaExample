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
package fi.ahto.example.traffic.data.gtfs.feeder;

import fi.ahto.example.traffic.data.contracts.internal.FrequencyData;
import fi.ahto.example.traffic.data.contracts.internal.GTFSLocalTime;
import org.onebusaway.gtfs.model.Frequency;

/**
 *
 * @author Jouni Ahto
 */
public class FrequencyExt {
    private final Frequency fr;

    public FrequencyExt(Frequency fr) {
        this.fr = fr;
    }
    
    public FrequencyData toFrequencyData() {
        FrequencyData fd = new FrequencyData();
        fd.setStartTime(this.getStartTime());
        fd.setEndTime(this.getEndTime());
        fd.setHeadwaySecs(this.getHeadwaySecs());
        fd.setExactTimes(this.getExactTimes());
        return fd;
    }
    /*
    public Integer getId() {
        return fr.getId();
    }

    public Trip getTrip() {
        return fr.getTrip();
    }
    */
    public GTFSLocalTime getStartTime() {
        return GTFSLocalTime.ofSecondOfDay(fr.getStartTime());
    }

    public GTFSLocalTime getEndTime() {
        return GTFSLocalTime.ofSecondOfDay(fr.getEndTime());
    }

    public short getHeadwaySecs() {
        return (short) fr.getHeadwaySecs();
    }

    public short getExactTimes() {
        return (short) fr.getExactTimes();
    }

    public int getLabelOnly() {
        return fr.getLabelOnly();
    }
}
