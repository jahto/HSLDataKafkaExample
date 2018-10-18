/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.example.traffic.data.contracts.database;

import fi.ahto.example.traffic.data.contracts.database.utils.GTFSLocalTimeConverter;
import fi.ahto.example.traffic.data.contracts.internal.GTFSLocalTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Convert;

/**
 *
 * @author jah
 */
public class DBService {

    @Column(name = "service_id")
    private String serviceId;
    @Column(name = "valid_from")
    private LocalDate validFrom;
    @Column(name = "valid_until")
    private LocalDate validUntil;
    @Column(name = "monday")
    private boolean monday;
    @Column(name = "tuesday")
    private boolean tuesday;
    @Column(name = "wednesday")
    private boolean wednesday;
    @Column(name = "thursday")
    private boolean thursday;
    @Column(name = "friday")
    private boolean friday;
    @Column(name = "saturday")
    private boolean saturday;
    @Column(name = "sunday")
    private boolean sunday;

    private final List<LocalDate> in_use = new ArrayList<>();
    private final List<LocalDate> not_in_use = new ArrayList<>();
    private final List<Trip> trips = new ArrayList<>();

    @Column(name = "route_id")
    private String routeId;

    public DBService() {
    }

    class Trip {

        // @Column(name = "route_id")
        // private String routeId;
        // @Column(name = "service_id")
        // private String serviceId;
        @Column(name = "trip_id")
        private String tripId;
        @Column(name = "headsign")
        private String headsign;
        @Column(name = "direction")
        private short direction;
        @Column(name = "shape_id")
        private String shapeId;
        @Column(name = "block_id")
        private String blockId;
        @Column(name = "wheelchair_accessible")
        private short wheelchairAccessible;
        @Column(name = "bikes_allowed")
        private short bikesAllowed;

        private final List<StopTime> stop_times = new ArrayList<>();
        private final List<Frequency> frequencies = new ArrayList<>();

        class Frequency {

            // @Column(name = "trip_id")
            // private String tripId;
            @Column(name = "start_time")
            @Convert(converter = GTFSLocalTimeConverter.class)
            private GTFSLocalTime startTime;
            @Column(name = "end_time")
            @Convert(converter = GTFSLocalTimeConverter.class)
            private GTFSLocalTime endTime;
            @Column(name = "headway_secs")
            private short headwaySecs;
            @Column(name = "exact_times")
            private short exactTimes; // TODO: Check if this actually boolean.
        }

        class StopTime {

            @Column(name = "stop_sequence")
            private int stopSequence;
            @Column(name = "stop_id")
            private String stopId;
            @Column(name = "headsign")
            private String headsign;
            @Column(name = "arrival")
            @Convert(converter = GTFSLocalTimeConverter.class)
            private GTFSLocalTime arrival;
            @Column(name = "departure")
            @Convert(converter = GTFSLocalTimeConverter.class)
            private GTFSLocalTime departure;
            @Column(name = "pickup_type")
            private short pickupType;
            @Column(name = "dropoff_type")
            private short dropoffType;
            @Column(name = "timepoint")
            private short timepoint;
            @Column(name = "dist_traveled")
            private float distTraveled;
        }
    }
}
