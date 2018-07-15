/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jahto.utils;

import com.github.jahto.utils.FSTSerializers.FSTInstantSerializer;
import com.github.jahto.utils.FSTSerializers.FSTLocalDateSerializer;
import com.github.jahto.utils.FSTSerializers.FSTLocalTimeSerializer;
//import com.github.jahto.utils.FSTSerializers.FSTLocalTimeWithoutNanosSerializer;
import com.github.jahto.utils.FSTSerializers.FSTSetSerializer;
import com.github.jahto.utils.FSTSerializers.FSTZonedDateTimeSerializer;
//import com.github.jahto.utils.FSTSerializers.FSTZonedDateTimeWithoutNanosSerializer;
import com.github.jahto.utils.FSTSerializers.GenericObjectSerializer;
import com.github.jahto.utils.FSTSerializers.ServiceStopSerializer;
import com.github.jahto.utils.FSTSerializers.TripStopSerializer;
import fi.ahto.example.traffic.data.contracts.internal.ServiceData;
import fi.ahto.example.traffic.data.contracts.internal.ServiceList;
import fi.ahto.example.traffic.data.contracts.internal.ServiceStop;
import fi.ahto.example.traffic.data.contracts.internal.ServiceStopSet;
import fi.ahto.example.traffic.data.contracts.internal.ServiceTrips;
import fi.ahto.example.traffic.data.contracts.internal.TripStop;
import fi.ahto.example.traffic.data.contracts.internal.TripStopSet;
import fi.ahto.example.traffic.data.contracts.internal.TripStopSetComparator;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import fi.ahto.example.traffic.data.contracts.internal.VehicleDataList;
import fi.ahto.example.traffic.data.contracts.internal.VehicleHistorySet;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Set;
import org.nustaq.serialization.FSTConfiguration;

/**
 *
 * @author jah
 */
public class CommonFSTConfiguration {
    public static FSTConfiguration getCommonFSTConfiguration() {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.registerSerializer(LocalTime.class, new FSTLocalTimeSerializer(), false);
        conf.registerSerializer(LocalDate.class, new FSTLocalDateSerializer(), false);
        conf.registerSerializer(Instant.class, new FSTInstantSerializer(), false);
        conf.registerSerializer(ZonedDateTime.class, new FSTZonedDateTimeSerializer(), false);
        conf.registerClass(LocalTime.class);
        conf.registerClass(LocalDate.class);
        conf.registerClass(Instant.class);
        conf.registerClass(ZonedDateTime.class);
        //conf.registerSerializer(Set.class, new FSTSetSerializer(), false);
        //conf.registerSerializer(Set.class, new FSTSetSerializer(), false);
        //conf.registerSerializer(ServiceStopSet.class, new FSTSetSerializer(), false);
        conf.registerClass(ServiceStopSet.class);
        //conf.registerSerializer(ServiceStop.class, new ServiceStopSerializer(), false);
        conf.registerClass(ServiceStop.class);
        //conf.registerSerializer(TripStopSet.class, new FSTSetSerializer(), false);
        conf.registerClass(TripStopSet.class);
        //conf.registerClass(TripStopSetComparator.class);
        //conf.registerSerializer(TripStop.class, new TripStopSerializer(), false);
        conf.registerClass(TripStop.class);
        //conf.registerSerializer(VehicleHistorySet.class, new FSTSetSerializer(), false);
        conf.registerClass(VehicleHistorySet.class);
        // conf.registerSerializer(VehicleActivity.class, new VehicleActivitySerializer(), false);
        // conf.registerSerializer(VehicleActivity.class, new GenericObjectSerializer<VehicleActivity>(VehicleActivity.class), false);
        conf.registerClass(VehicleActivity.class);
        conf.registerClass(ServiceList.class);
        conf.registerClass(ServiceData.class);
        conf.registerClass(ServiceTrips.class);
        conf.registerClass(VehicleDataList.class);
        conf.setShareReferences(false);
        return conf;
    }
}
