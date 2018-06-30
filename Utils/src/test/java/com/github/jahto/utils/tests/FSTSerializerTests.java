/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jahto.utils.tests;

import com.github.jahto.utils.FSTSerde;
import com.github.jahto.utils.FSTSerializers.FSTInstantSerializer;
import com.github.jahto.utils.FSTSerializers.FSTLocalDateSerializer;
import com.github.jahto.utils.FSTSerializers.FSTLocalTimeSerializer;
import fi.ahto.example.traffic.data.contracts.internal.TripStop;
import fi.ahto.example.traffic.data.contracts.internal.TripStopSet;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;

/**
 *
 * @author jah
 */
public class FSTSerializerTests {
    @Test
    public void test_String() {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        
        FSTSerde<String> serde = new FSTSerde<>(String.class, conf);
        byte[] ser = serde.serializer().serialize("foo", "Mitä vittua?");
        String str = serde.deserializer().deserialize("foo", ser);

        int i = 0;
    }

    @Test
    public void test_Instant() {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        FSTSerde<Instant> serde = new FSTSerde<>(Instant.class, conf);
        byte[] ser = serde.serializer().serialize("foo", Instant.MAX);
        Instant inst = serde.deserializer().deserialize("foobar", ser);
        int i = 0;
    }
    
    @Test
    public void test_TreeMap() {
        TripStopSet set = new TripStopSet();
        TripStop stop1 = new TripStop();
        stop1.stopid = "STOP1";
        stop1.seq = 1;
        stop1.arrivalTime = LocalTime.of(15, 10);
        TripStop stop2 = new TripStop();
        stop2.stopid = "STOP2";
        stop2.seq = 2;
        stop2.arrivalTime = LocalTime.of(15, 15);
        set.add(stop1);
        set.add(stop2);
        
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.registerSerializer(LocalTime.class, new FSTLocalTimeSerializer(), false);
        conf.registerSerializer(LocalDate.class, new FSTLocalDateSerializer(), false);
        conf.registerSerializer(Instant.class, new FSTInstantSerializer(), false);
        conf.setShareReferences(false);
        FSTSerde<TripStopSet> serde = new FSTSerde<>(TripStopSet.class, conf);
        byte[] ser = serde.serializer().serialize("foo", set);
        TripStopSet cmp = serde.deserializer().deserialize("foobar", ser);
        int i = 0;
    }
}
