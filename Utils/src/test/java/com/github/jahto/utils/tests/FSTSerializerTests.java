/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jahto.utils.tests;

import com.github.jahto.utils.CommonFSTConfiguration;
import com.github.jahto.utils.FSTSerde;
import com.github.jahto.utils.FSTSerializers.*;
/*
import com.github.jahto.utils.FSTSerializers.FSTInstantSerializer;
import com.github.jahto.utils.FSTSerializers.FSTLocalDateSerializer;
import com.github.jahto.utils.FSTSerializers.FSTLocalTimeSerializer;
import com.github.jahto.utils.FSTSerializers.FSTLocalTimeWithoutNanosSerializer;
*/
import fi.ahto.example.traffic.data.contracts.internal.ServiceData;
import fi.ahto.example.traffic.data.contracts.internal.ServiceList;
import fi.ahto.example.traffic.data.contracts.internal.TripStop;
import fi.ahto.example.traffic.data.contracts.internal.TripStopSet;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
/**
 *
 * @author Jouni Ahto
 */
public class FSTSerializerTests {
    @Test
    public void test_ThreeByteInt() throws IOException {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        for (int i = -8388608; i < 8388608; i++){
            FSTObjectOutput out = conf.getObjectOutput();
            SerializerImplementations.writeThreeByteInt(i, out);
            byte[] b = out.getBuffer();
            FSTObjectInput in = conf.getObjectInput(b);
            int j = SerializerImplementations.readThreeByteInt(in);
            assertThat(j, is(i));
        }
    }
    
    @Test
    public void test_LocalDate() {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.registerSerializer(LocalTime.class, new FSTLocalTimeSerializer(), false);
        FSTSerde<LocalDate> serde = new FSTSerde<>(LocalDate.class, conf);
        byte[] ser = serde.serializer().serialize("foo", LocalDate.MAX);
        LocalDate max = serde.deserializer().deserialize("foobar", ser);
        assertThat(max, is(LocalDate.MAX));
        ser = serde.serializer().serialize("foo", LocalDate.MIN);
        LocalDate min = serde.deserializer().deserialize("foobar", ser);
        assertThat(min, is(LocalDate.MIN));
    }
    
    @Test
    public void test_LocalTime() {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.registerSerializer(LocalTime.class, new FSTLocalTimeSerializer(), false);
        FSTSerde<LocalTime> serde = new FSTSerde<>(LocalTime.class, conf);
        byte[] ser = serde.serializer().serialize("foo", LocalTime.MAX);
        LocalTime max = serde.deserializer().deserialize("foobar", ser);
        assertThat(max, is(LocalTime.MAX));
        ser = serde.serializer().serialize("foo", LocalTime.MIN);
        LocalTime min = serde.deserializer().deserialize("foobar", ser);
        assertThat(min, is(LocalTime.MIN));
    }
    
    @Test
    public void test_LocalTimeWithoutNanos() {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.registerSerializer(LocalTime.class, new FSTLocalTimeSerializer(), false);
        FSTSerde<LocalTime> serde = new FSTSerde<>(LocalTime.class, conf);
        byte[] ser = serde.serializer().serialize("foo", LocalTime.MAX);
        LocalTime max = serde.deserializer().deserialize("foobar", ser);
        assertThat(max.toSecondOfDay(), is(LocalTime.MAX.toSecondOfDay()));
        ser = serde.serializer().serialize("foo", LocalTime.MIN);
        LocalTime min = serde.deserializer().deserialize("foobar", ser);
        assertThat(min, is(LocalTime.MIN));
    }
    
    @Test
    public void test_Instant() {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.registerSerializer(Instant.class, new FSTInstantSerializer(), false);
        FSTSerde<Instant> serde = new FSTSerde<>(Instant.class, conf);
        byte[] ser = serde.serializer().serialize("foo", Instant.MAX);
        Instant max = serde.deserializer().deserialize("foobar", ser);
        assertThat(max, is(Instant.MAX));
        ser = serde.serializer().serialize("foo", Instant.MIN);
        Instant min = serde.deserializer().deserialize("foobar", ser);
        assertThat(min, is(Instant.MIN));
    }
    
    @Test
    public void test_ZoneOffset() {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.registerSerializer(ZoneOffset.class, new FSTZoneOffsetSerializer(), false);
        FSTSerde<ZoneOffset> serde = new FSTSerde<>(ZoneOffset.class, conf);
        byte[] ser = serde.serializer().serialize("foo", ZoneOffset.MAX);
        ZoneOffset max = serde.deserializer().deserialize("foobar", ser);
        assertThat(max, is(ZoneOffset.MAX));
        ser = serde.serializer().serialize("foo", ZoneOffset.MIN);
        ZoneOffset min = serde.deserializer().deserialize("foobar", ser);
        assertThat(min, is(ZoneOffset.MIN));
    }
    
    //@Test
    public void test_TreeSerde() {
        TripStopSet set = new TripStopSet();
        set.block = "A";
        set.direction = "B";
        set.route = "C";
        set.service = "D";
        TripStop stop1 = new TripStop();
        stop1.stopid = "STOP1";
        stop1.seq = 1;
        stop1.arrivalTime = LocalTime.of(15, 10).toSecondOfDay();
        TripStop stop2 = new TripStop();
        stop2.stopid = "STOP2";
        stop2.seq = 2;
        stop2.arrivalTime = LocalTime.of(15, 15).toSecondOfDay();
        set.add(stop1);
        set.add(stop2);
        
        FSTConfiguration conf = CommonFSTConfiguration.getCommonFSTConfiguration();
        FSTSerde<TripStopSet> serde = new FSTSerde<>(TripStopSet.class, conf);
        byte[] ser = serde.serializer().serialize("foo", set);
        TripStopSet cmp = serde.deserializer().deserialize("foobar", ser);
        int i = 0;
    }
    //@Test
    public void test_ServiceList() {
        FSTConfiguration conf = CommonFSTConfiguration.getCommonFSTConfiguration();
        ServiceList list = new ServiceList();
        ServiceData d1 = new ServiceData();
        d1.blockIds.add("foo");
        d1.routeIds.add("foo");
        d1.notinuse.add(LocalDate.MIN);
        list.add(d1);
        FSTSerde<ServiceList> serde = new FSTSerde<>(ServiceList.class, conf);
        byte[] ser = serde.serializer().serialize("foo", list);
        ServiceList cmp = serde.deserializer().deserialize("foobar", ser);
        int i = 0;
    }
}
