/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.example.traffic.data.contracts.internal;

import java.time.LocalTime;
import javax.persistence.Convert;
import org.onebusaway.gtfs.model.Frequency;

/**
 *
 * @author jah
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
