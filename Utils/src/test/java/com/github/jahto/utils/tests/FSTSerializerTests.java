/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jahto.utils.tests;

import com.github.jahto.utils.FSTSerializers.java.time.SerializerImplementations;
import com.github.jahto.utils.FSTSerializers.java.time.FSTZoneOffsetSerializer;
import com.github.jahto.utils.FSTSerializers.java.time.FSTInstantSerializer;
import com.github.jahto.utils.FSTSerializers.java.time.FSTLocalTimeSerializer;
import com.github.jahto.utils.CommonFSTConfiguration;
import com.github.jahto.utils.FSTSerde;
import com.github.jahto.utils.FSTSerializers.*;
import com.github.jahto.utils.FSTSerializers.java.time.FSTDurationSerializer;
import com.github.jahto.utils.FSTSerializers.java.time.FSTPeriodSerializer;
import com.github.jahto.utils.FSTSerializers.java.time.FSTYearMonthSerializer;
import com.github.jahto.utils.FSTSerializers.java.time.FSTZoneIdSerializer;
import com.github.jahto.utils.FSTSerializers.java.time.FSTZonedDateTimeSerializer;
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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.TreeSet;
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
    public void test_YearMonth() {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.registerSerializer(YearMonth.class, new FSTYearMonthSerializer(), false);
        FSTSerde<YearMonth> serde = new FSTSerde<>(YearMonth.class, conf);
        YearMonth ym = YearMonth.of(Year.MAX_VALUE, 12);
        byte[] ser = serde.serializer().serialize("foo", ym);
        YearMonth max = serde.deserializer().deserialize("foobar", ser);
        ym = YearMonth.of(Year.MIN_VALUE, 1);
        ser = serde.serializer().serialize("foo", ym);
        YearMonth min = serde.deserializer().deserialize("foobar", ser);
        
        ym = YearMonth.of(2018, 7);
        ser = serde.serializer().serialize("foo", ym);
        YearMonth now = serde.deserializer().deserialize("foobar", ser);

        ym = YearMonth.of(-2018, 7);
        ser = serde.serializer().serialize("foo", ym);
        YearMonth past = serde.deserializer().deserialize("foobar", ser);

        int i = ym.getYear();
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
    public void test_ZonedDateTime() {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.registerSerializer(ZonedDateTime.class, new FSTZonedDateTimeSerializer(), false);
        FSTSerde<ZonedDateTime> serde = new FSTSerde<>(ZonedDateTime.class, conf);
        byte[] ser = serde.serializer().serialize("foo", ZonedDateTime.now());
        ZonedDateTime now = serde.deserializer().deserialize("foobar", ser);
        ser = serde.serializer().serialize("foo", ZonedDateTime.now().withZoneSameInstant(ZoneId.of("Africa/Abidjan")));
        ZonedDateTime now2 = serde.deserializer().deserialize("foobar", ser);
        int i = 0;
    }
    
    @Test
    public void test_Duration() {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.registerSerializer(Duration.class, new FSTDurationSerializer(), false);
        FSTSerde<Duration> serde = new FSTSerde<>(Duration.class, conf);
        byte[] ser;
        Duration res;
        
        Duration eight = Duration.ofSeconds(-46028797018963968L);
        ser = serde.serializer().serialize("", eight);
        res = serde.deserializer().deserialize("", ser);
        assertThat(res, is(eight));
        
        Duration seven = Duration.ofSeconds(-36028797018963968L);
        ser = serde.serializer().serialize("", seven);
        res = serde.deserializer().deserialize("", ser);
        assertThat(res, is(seven));

        Duration six = Duration.ofSeconds(-140737488355328L);
        ser = serde.serializer().serialize("", six);
        res = serde.deserializer().deserialize("", ser);
        assertThat(res, is(six));

        Duration five = Duration.ofSeconds(-549755813888L);
        ser = serde.serializer().serialize("", five);
        res = serde.deserializer().deserialize("", ser);
        assertThat(res, is(five));

        Duration four = Duration.ofSeconds(-2147483648);
        ser = serde.serializer().serialize("", four);
        res = serde.deserializer().deserialize("", ser);
        assertThat(res, is(four));

        Duration three = Duration.ofSeconds(-8388608);
        ser = serde.serializer().serialize("", three);
        res = serde.deserializer().deserialize("", ser);
        assertThat(res, is(three));

        Duration two = Duration.ofSeconds(-32768);
        ser = serde.serializer().serialize("", two);
        res = serde.deserializer().deserialize("", ser);
        assertThat(res, is(two));

        Duration one = Duration.ofSeconds(-128);
        ser = serde.serializer().serialize("", one);
        res = serde.deserializer().deserialize("", ser);
        assertThat(res, is(one));

    }
    
    @Test
    public void test_Period() {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.registerSerializer(Period.class, new FSTPeriodSerializer(), false);
        FSTSerde<Period> serde = new FSTSerde<>(Period.class, conf);
        
        byte[] ser;
        Period res;

        Period zero = Period.of(0, 0, 0);
        Period one = Period.of(0, 0, 1000);
        Period two = Period.of(0, 1000, 1);
        Period three = Period.of(1000, 0, 1);
        Period four = Period.of(1000, 1, 1);
        Period five = Period.of(0, 1000, 0);
        Period six = Period.of(1000, 1, 0);
        Period seven = Period.of(1000, 0, 0);
        
        ser = serde.serializer().serialize("", zero);
        res = serde.deserializer().deserialize("", ser);
        assertThat(res, is(zero));

        ser = serde.serializer().serialize("", one);
        res = serde.deserializer().deserialize("", ser);
        assertThat(res, is(one));

        ser = serde.serializer().serialize("", two);
        res = serde.deserializer().deserialize("", ser);
        assertThat(res, is(two));

        ser = serde.serializer().serialize("", three);
        res = serde.deserializer().deserialize("", ser);
        assertThat(res, is(three));

        ser = serde.serializer().serialize("", four);
        res = serde.deserializer().deserialize("", ser);
        assertThat(res, is(four));

        ser = serde.serializer().serialize("", five);
        res = serde.deserializer().deserialize("", ser);
        assertThat(res, is(five));

        ser = serde.serializer().serialize("", six);
        res = serde.deserializer().deserialize("", ser);
        assertThat(res, is(six));

        ser = serde.serializer().serialize("", seven);
        res = serde.deserializer().deserialize("", ser);
        assertThat(res, is(seven));
    }
}
