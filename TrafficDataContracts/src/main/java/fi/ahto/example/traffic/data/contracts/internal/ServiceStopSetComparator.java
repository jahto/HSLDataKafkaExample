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
public class ServiceStopSetComparator implements Comparator<ServiceStop>, Serializable {
    private static final long serialVersionUID = -7398359359831634571L;

    @Override
    public int compare(ServiceStop o1, ServiceStop o2) {
        return Integer.compare(o1.seq, o2.seq);
    }
}
