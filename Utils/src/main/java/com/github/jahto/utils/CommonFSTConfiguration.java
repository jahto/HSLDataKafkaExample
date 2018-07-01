/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jahto.utils;

import com.github.jahto.utils.FSTSerializers.FSTInstantSerializer;
import com.github.jahto.utils.FSTSerializers.FSTLocalDateSerializer;
import com.github.jahto.utils.FSTSerializers.FSTLocalTimeSerializer;
import com.github.jahto.utils.FSTSerializers.FSTSetSerializer;
import com.github.jahto.utils.FSTSerializers.FSTZonedDateTimeSerializer;
import com.github.jahto.utils.FSTSerializers.GenericObjectSerializer;
import com.github.jahto.utils.FSTSerializers.ServiceStopSerializer;
import fi.ahto.example.traffic.data.contracts.internal.ServiceStop;
import fi.ahto.example.traffic.data.contracts.internal.ServiceStopSet;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
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
        conf.registerSerializer(Set.class, new FSTSetSerializer(), false);
        conf.registerSerializer(ServiceStopSet.class, new FSTSetSerializer(), false);
        conf.registerSerializer(ServiceStop.class, new ServiceStopSerializer(), false);
        conf.registerSerializer(VehicleHistorySet.class, new FSTSetSerializer(), false);
        // conf.registerSerializer(VehicleActivity.class, new VehicleActivitySerializer(), false);
        // conf.registerSerializer(VehicleActivity.class, new GenericObjectSerializer<VehicleActivity>(VehicleActivity.class), false);
        conf.registerClass(VehicleActivity.class);
        conf.registerClass(ServiceStopSet.class);
        conf.registerClass(ServiceStop.class);
        conf.registerClass(VehicleHistorySet.class);
        conf.setShareReferences(false);
        return conf;
    }
}
