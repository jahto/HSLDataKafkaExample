/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.example.traffic.data.gtfs.feeder;

import java.util.Objects;

/**
 *
 * @author jah
 */
public class TripKey {
    public String tripid;
    public String routeid;

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.tripid);
        hash = 83 * hash + Objects.hashCode(this.routeid);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TripKey other = (TripKey) obj;
        if (!Objects.equals(this.tripid, other.tripid)) {
            return false;
        }
        if (!Objects.equals(this.routeid, other.routeid)) {
            return false;
        }
        return true;
    }
    
    public TripKey(String trip, String route) {
        this.tripid = trip;
        this.routeid = route;
    }
}
