/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.example.traffic.data.contracts.database;

import java.time.LocalDate;
import org.onebusaway.gtfs.model.calendar.ServiceDate;

/**
 *
 * @author jah
 */
public class Helpers {
    public static LocalDate from(ServiceDate src) {
        return LocalDate.of(src.getYear(), src.getMonth(), src.getDay());
    }
}
