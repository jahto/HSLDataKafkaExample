/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.example.traffic.data.contracts.internal;

import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * @author Jouni Ahto
 */
public class VehicleHistorySetComparator implements Comparator<VehicleHistoryRecord>, Serializable {

    private static final long serialVersionUID = 8611269010100525058L;

    @Override
    public int compare(VehicleHistoryRecord o1, VehicleHistoryRecord o2) {
        return o1.getRecordTime().compareTo(o2.getRecordTime());
    }
}
