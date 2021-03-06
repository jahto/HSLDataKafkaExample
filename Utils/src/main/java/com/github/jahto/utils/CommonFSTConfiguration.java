/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jahto.utils;

import com.github.jahto.utils.FSTSerializers.FSTGTFSLocalTimeSerializer;
import com.github.jahto.utils.FSTSerializers.java.time.FSTInstantSerializer;
import com.github.jahto.utils.FSTSerializers.java.time.FSTLocalDateSerializer;
import com.github.jahto.utils.FSTSerializers.java.time.FSTLocalTimeSerializer;
import com.github.jahto.utils.FSTSerializers.java.time.FSTZonedDateTimeSerializer;
import com.github.jahto.utils.FSTSerializers.TripStopSerializer;
import fi.ahto.example.traffic.data.contracts.internal.GTFSLocalTime;
import fi.ahto.example.traffic.data.contracts.internal.RouteData;
import fi.ahto.example.traffic.data.contracts.internal.StreamServiceData;
import fi.ahto.example.traffic.data.contracts.internal.ServiceList;
import fi.ahto.example.traffic.data.contracts.internal.StartTimesToTrips;
import fi.ahto.example.traffic.data.contracts.internal.RouteType;
import fi.ahto.example.traffic.data.contracts.internal.ShapeData;
import fi.ahto.example.traffic.data.contracts.internal.ShapeSet;
import fi.ahto.example.traffic.data.contracts.internal.StopData;
import fi.ahto.example.traffic.data.contracts.internal.TripStop;
import fi.ahto.example.traffic.data.contracts.internal.TripStopSet;
import fi.ahto.example.traffic.data.contracts.internal.TripStopSetComparator;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import fi.ahto.example.traffic.data.contracts.internal.VehicleDataList;
import fi.ahto.example.traffic.data.contracts.internal.VehicleHistoryRecord;
import fi.ahto.example.traffic.data.contracts.internal.VehicleHistorySet;
import fi.ahto.example.traffic.data.contracts.internal.VehicleHistorySetComparator;
import java.lang.invoke.SerializedLambda;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
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
        //conf.registerSerializer(TripStopSet.class, new FSTSetSerializer(), false);
        conf.registerClass(TripStopSet.class);
        conf.registerClass(TripStopSetComparator.class);
        conf.registerSerializer(TripStop.class, new TripStopSerializer(), false);
        conf.registerClass(TripStop.class);
        conf.registerClass(VehicleHistorySet.class);
        conf.registerClass(VehicleHistorySetComparator.class);
        conf.registerClass(VehicleActivity.class);
        conf.registerClass(ServiceList.class);
        conf.registerClass(StreamServiceData.class);
        conf.registerClass(StartTimesToTrips.class);
        conf.registerClass(VehicleDataList.class);
        conf.registerClass(RouteType.class);
        conf.registerClass(VehicleHistoryRecord.class);
        conf.registerClass(StopData.class);
        conf.registerClass(RouteData.class);
        // conf.registerClass(RouteStop.class);
        conf.registerClass(ShapeSet.class);
        conf.registerClass(ShapeData.class);
        // Not obvious, but was found during debugging
        conf.registerClass(SerializedLambda.class);
        
        // New, add to end.
        conf.registerSerializer(GTFSLocalTime.class, new FSTGTFSLocalTimeSerializer(), false);
        conf.registerClass(GTFSLocalTime.class);

        // We rarely have anything that could be shared, so don't bother even checking.
        // Shaves off a load of processing cycles.
        conf.setShareReferences(false);
        // Must test with both true and false. Speedier serialization comes with the price
        // of increased object size, and it seems the bottleneck is instead I/O when doing
        // lookups from globaltables, so false might win. Ok, true was better.
        conf.setPreferSpeed(true);
        return conf;
    }
}
