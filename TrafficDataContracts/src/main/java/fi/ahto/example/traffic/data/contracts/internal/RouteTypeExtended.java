/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.ahto.example.traffic.data.contracts.internal;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jouni Ahto
 */
/*
 * See https://developers.google.com/transit/gtfs/reference/extended-route-types.
 */

public enum RouteTypeExtended implements Serializable {
    UNKNOWN(-1),
    RAILWAY_SERVICE(100), // YES
    HIGH_SPEED_RAIL_SERVICE(101), // YES
    LONG_DISTANCE_TRAINS(102), // YES
    INTER_REGIONAL_RAIL_SERVICE(103), // YES
    CAR_TRANSPORT_RAIL_SERVICE(104),
    SLEEPER_RAIL_SERVICE(105), // YES
    REGIONAL_RAIL_SERVICE(106), // YES
    TOURIST_RAILWAY_SERVICE(107), // YES
    RAIL_SHUTTLE_WITHIN_COMPLEX(108), // YES
    SUBURBAN_RAILWAY(109), // YES
    REPLACEMENT_RAIL_SERVICE(110),
    SPECIAL_RAIL_SERVICE(111),
    LORRY_TRANSPORT_RAIL_SERVICE(112),
    ALL_RAIL_SERVICES(113),
    CROSS_COUNTRY_RAIL_SERVICE(114),
    VEHICLE_TRANSPORT_RAIL_SERVICE(115),
    RACK_AND_PINION_RAILWAY(116),
    ADDITIONAL_RAIL_SERVICE(117),
    COACH_SERVICE(200), // YES
    INTERNATIONAL_COACH_SERVICE(201), // YES
    NATIONAL_COACH_SERVICE(202), // YES
    SHUTTLE_COACH_SERVICE(203),
    REGIONAL_COACH_SERVICE_(204), // YES
    SPECIAL_COACH_SERVICE(205),
    SIGHTSEEING_COACH_SERVICE(206),
    TOURIST_COACH_SERVICE(207),
    COMMUTER_COACH_SERVICE(208), // YES
    ALL_COACH_SERVICES(209),
    SUBURBAN_RAILWAY_SERVICE(300),
    URBAN_RAILWAY_SERVICE(400), // YES
    METRO_SERVICE_(401), // YES,
    UNDERGROUND_SERVICE_(402), // YES,
    URBAN_RAILWAY_SERVICE_(403),
    ALL_URBAN_RAILWAY_SERVICES(404),
    MONORAIL(405), // YES
    METRO_SERVICE(500),
    UNDERGROUND_SERVICE(600),
    BUS_SERVICE(700), // YES,
    REGIONAL_BUS_SERVICE(701), // YES,
    EXPRESS_BUS_SERVICE_(702), // YES
    STOPPING_BUS_SERVICE(703),
    LOCAL_BUS_SERVICE_(704), // YES
    NIGHT_BUS_SERVICE(705),
    POST_BUS_SERVICE(706),
    SPECIAL_NEEDS_BUS(707),
    MOBILITY_BUS_SERVICE(708),
    MOBILITY_BUS_FOR_REGISTERED_DISABLED(709),
    SIGHTSEEING_BUS(710),
    SHUTTLE_BUS(711),
    SCHOOL_BUS(712),
    SCHOOL_AND_PUBLIC_SERVICE_BUS(713),
    RAIL_REPLACEMENT_BUS_SERVICE(714),
    DEMAND_AND_RESPONSE_BUS_SERVICE(715),
    ALL_BUS_SERVICES(716),
    TROLLEYBUS_SERVICE(800), // YES
    TRAM_SERVICE(900), // YES
    CITY_TRAM_SERVICE(901),
    LOCAL_TRAM_SERVICE(902),
    REGIONAL_TRAM_SERVICE(903),
    SIGHTSEEING_TRAM_SERVICE(904),
    SHUTTLE_TRAM_SERVICE(905),
    ALL_TRAM_SERVICES(906),
    WATER_TRANSPORT_SERVICE(1000), // YES
    INTERNATIONAL_CAR_FERRY_SERVICE(1001),
    NATIONAL_CAR_FERRY_SERVICE(1002),
    REGIONAL_CAR_FERRY_SERVICE(1003),
    LOCAL_CAR_FERRY_SERVICE(1004),
    INTERNATIONAL_PASSENGER_FERRY_SERVICE(1005),
    NATIONAL_PASSENGER_FERRY_SERVICE(1006),
    REGIONAL_PASSENGER_FERRY_SERVICE(1007),
    LOCAL_PASSENGER_FERRY_SERVICE(1008),
    POST_BOAT_SERVICE(1009),
    TRAIN_FERRY_SERVICE(1010),
    ROAD_LINK_FERRY_SERVICE(1011),
    AIRPORT_LINK_FERRY_SERVICE(1012),
    CAR_HIGH_SPEED_FERRY_SERVICE(1013),
    PASSENGER_HIGH_SPEED_FERRY_SERVICE(1014),
    SIGHTSEEING_BOAT_SERVICE(1015),
    SCHOOL_BOAT(1016),
    CABLE_DRAWN_BOAT_SERVICE(1017),
    RIVER_BUS_SERVICE(1018),
    SCHEDULED_FERRY_SERVICE(1019),
    SHUTTLE_FERRY_SERVICE(1020),
    ALL_WATER_TRANSPORT_SERVICES(1021),
    AIR_SERVICE(1100),
    INTERNATIONAL_AIR_SERVICE(1101),
    DOMESTIC_AIR_SERVICE(1102),
    INTERCONTINENTAL_AIR_SERVICE(1103),
    DOMESTIC_SCHEDULED_AIR_SERVICE(1104),
    SHUTTLE_AIR_SERVICE(1105),
    INTERCONTINENTAL_CHARTER_AIR_SERVICE(1106),
    INTERNATIONAL_CHARTER_AIR_SERVICE(1107),
    ROUND_TRIP_CHARTER_AIR_SERVICE(1108),
    SIGHTSEEING_AIR_SERVICE(1109),
    HELICOPTER_AIR_SERVICE(1110),
    DOMESTIC_CHARTER_AIR_SERVICE(1111),
    SCHENGEN_AREA_AIR_SERVICE(1112),
    AIRSHIP_SERVICE(1113),
    ALL_AIR_SERVICES(1114),
    FERRY_SERVICE(1200),
    TELECABIN_SERVICE(1300), // YES
    TELECABIN_SERVICE_(1301),
    CABLE_CAR_SERVICE(1302),
    ELEVATOR_SERVICE(1303),
    CHAIR_LIFT_SERVICE(1304),
    DRAG_LIFT_SERVICE(1305),
    SMALL_TELECABIN_SERVICE(1306),
    ALL_TELECABIN_SERVICES(1307),
    FUNICULAR_SERVICE(1400), // YES
    FUNICULAR_SERVICE_(1401),
    ALL_FUNICULAR_SERVICE(1402),
    TAXI_SERVICE(1500),
    COMMUNAL_TAXI_SERVICE(1501), // YES
    WATER_TAXI_SERVICE(1502),
    RAIL_TAXI_SERVICE(1503),
    BIKE_TAXI_SERVICE(1504),
    LICENSED_TAXI_SERVICE(1505),
    PRIVATE_HIRE_SERVICE_VEHICLE(1506),
    ALL_TAXI_SERVICES(1507),
    SELF_DRIVE(1600),
    HIRE_CAR(1601),
    HIRE_VAN(1602),
    HIRE_MOTORBIKE(1603),
    HIRE_CYCLE(1604),
    MISCELLANEOUS_SERVICE(1700), // YES
    CABLE_CAR_(1701), // YES
    HORSE_DRAWN_CARRIAGE(1702)	// YES
    ;
    
    private static final long serialVersionUID = -2254522655930255127L;
    private final int value;
    private static final Map<Integer, RouteTypeExtended> map = new HashMap<>();

    private RouteTypeExtended(int val) {
        value = val;
    }
    
    static {
        for (RouteTypeExtended type : RouteTypeExtended.values()) {
            map.put(type.value, type);
        }
    }
    
    public static RouteTypeExtended from(int val) {
        switch (val) {
            case 0:
                return RouteTypeExtended.TRAM_SERVICE;
            case 1:
                return RouteTypeExtended.METRO_SERVICE;
            case 2:
                return RouteTypeExtended.RAILWAY_SERVICE;
            case 3:
                return RouteTypeExtended.BUS_SERVICE;
            case 4:
                return RouteTypeExtended.WATER_TRANSPORT_SERVICE; // Or FERRY_SERVICE?
            case 5:
                return RouteTypeExtended.TELECABIN_SERVICE;
            case 6:
                return RouteTypeExtended.TELECABIN_SERVICE; // Not exact match here between GTFS and GTFS-extended.
            case 7:
                return RouteTypeExtended.FUNICULAR_SERVICE;
            default:
                return map.getOrDefault(val, UNKNOWN);
        }
    }

    public static RouteTypeExtended mainCategory(RouteTypeExtended val) {
        int intval = val.value;
        intval = (intval / 100) * 100;
        if (intval == 800) {
            return RouteTypeExtended.ALL_BUS_SERVICES;
        } 
        return map.getOrDefault(intval, UNKNOWN);
    }
}
