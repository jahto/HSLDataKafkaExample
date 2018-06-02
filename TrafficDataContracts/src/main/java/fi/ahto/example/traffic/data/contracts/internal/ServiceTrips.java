/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.example.traffic.data.contracts.internal;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jah
 */
public class ServiceTrips {
    @JsonProperty("TimesForward")
    public Map<LocalTime, String> timesforward = new HashMap<>();
    @JsonProperty("TimesBackward")
    public Map<LocalTime, String> timesbackward = new HashMap<>();
}
