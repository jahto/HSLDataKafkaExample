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
public class TripStopSetComparator implements Comparator<TripStop>, Serializable {

    private static final long serialVersionUID = 8611269010100525058L;

    @Override
    public int compare(TripStop o1, TripStop o2) {
        return Integer.compare(o1.seq, o2.seq);
    }
}
