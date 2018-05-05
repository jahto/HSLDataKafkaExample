/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.example.traffic.data.contracts.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jah
 */
public class ServiceData {
    public String serviceId;
    public String routeId;
    public LocalDate validfrom;
    public LocalDate validuntil;
    public List<LocalDate> notinuse = new ArrayList<>();
    public byte weekdays = 0;
    @JsonProperty("StopsForward")
    public ServiceStopSet stopsforward = new ServiceStopSet();
    @JsonProperty("StopsBackward")
    public ServiceStopSet stopsbackward  = new ServiceStopSet();
    @JsonProperty("ShapesForward")
    public String shapesforward;
    @JsonProperty("ShapesBackward")
    public String shapesbackward;
    
    @JsonProperty("TimesForward")
    public Map<LocalTime, String> timesforward = new HashMap<>();
    @JsonProperty("TimesBackward")
    public Map<LocalTime, String> timesbackward = new HashMap<>();
}
