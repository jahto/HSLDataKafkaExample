/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.example.traffic.data.contracts.internal;

import java.util.TreeSet;

/**
 *
 * @author jah
 */
public class StopDataSet extends TreeSet<StopData> {
    
    public StopDataSet() {
        super((StopData o1, StopData o2) -> Integer.compare(o1.seq, o2.seq));
    }
    
}
