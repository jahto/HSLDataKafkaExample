/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jahto.utils.tests;

import com.github.jahto.utils.FSTSerde;
import com.github.jahto.utils.FSTSerializers.java.time.FSTDurationSerializer;
import com.github.jahto.utils.FSTSerializers.java.time.FSTInstantSerializer;
import com.github.jahto.utils.FSTSerializers.java.time.FSTLocalTimeSerializer;
import com.github.jahto.utils.FSTSerializers.java.time.FSTPeriodSerializer;
import com.github.jahto.utils.FSTSerializers.java.time.FSTYearMonthSerializer;
import com.github.jahto.utils.FSTSerializers.java.time.FSTZonedDateTimeSerializer;
import com.github.jahto.utils.FSTSerializers.java.time.SerializerImplementations;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
/**
 *
 * @author Jouni Ahto
 */
public class FSTSerializerTests {
    @Test
    public void test_ThreeByteInt() throws IOException {
        final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        for (int i = -8388608; i < 8388608; i++){
            final FSTObjectOutput out = conf.getObjectOutput();
            SerializerImplementations.writeThreeByteInt(i, out);
            final byte[] b = out.getBuffer();
            final FSTObjectInput in = conf.getObjectInput(b);
            final int j = SerializerImplementations.readThreeByteInt(in);
            assertThat(j, is(i));
        }
    }
    
    @Test
    public void test_ThreeByteIntAndCompareSpeed() throws IOException {
        final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        // Seems we need warming runs for both cases before the measurements
        for (int i = -8388608; i < 8388608; i++){
            // We only want to test ints that actually need three bytes.
            // In any other case, like the length not known before,
            // just use out.writeInt().
            if (i > Short.MIN_VALUE && i < Short.MAX_VALUE) {
                i = Short.MAX_VALUE;
                continue;
            }
            final FSTObjectOutput out = conf.getObjectOutput();
            out.writeInt(i);
            final byte[] b = out.getBuffer();
            final FSTObjectInput in = conf.getObjectInput(b);
            final int j = in.readInt();
        }
        for (int i = -8388608; i < 8388608; i++){
            if (i > Short.MIN_VALUE && i < Short.MAX_VALUE) {
                i = Short.MAX_VALUE;
                continue;
            }
            final FSTObjectOutput out = conf.getObjectOutput();
            SerializerImplementations.writeThreeByteInt(i, out);
            final byte[] b = out.getBuffer();
            final FSTObjectInput in = conf.getObjectInput(b);
            final int j = SerializerImplementations.readThreeByteInt(in);
        }

        Instant start = Instant.now();
        for (int i = -8388608; i < 8388608; i++){
            if (i > Short.MIN_VALUE && i < Short.MAX_VALUE) {
                i = Short.MAX_VALUE;
                continue;
            }
            final FSTObjectOutput out = conf.getObjectOutput();
            SerializerImplementations.writeThreeByteInt(i, out);
            final byte[] b = out.getBuffer();
            final FSTObjectInput in = conf.getObjectInput(b);
            final int j = SerializerImplementations.readThreeByteInt(in);
        }
        Instant end = Instant.now();
        System.out.println(Duration.between(start, end));
        start = Instant.now();
        for (int i = -8388608; i < 8388608; i++){
            if (i > Short.MIN_VALUE && i < Short.MAX_VALUE) {
                i = Short.MAX_VALUE;
                continue;
            }
            final FSTObjectOutput out = conf.getObjectOutput();
            out.writeInt(i);
            final byte[] b = out.getBuffer();
            final FSTObjectInput in = conf.getObjectInput(b);
            final int j = in.readInt();
        }
        end = Instant.now();
        System.out.println(Duration.between(start, end));
        // Result: so near each other it's impossible to tell which one
        // wins at any test, could be either one... So any enhancement
        // that writeThreeByteInt comes with, it's guaranteed to only
        // write three bytes so there'll be some small space savings.
        // The same should apply to all the other exact-byte versions
        // of short-int-long serializers.
    }
    
    @Test
    public void test_Short() throws IOException {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        for (int i = -32768; i < 32768; i++){
            FSTObjectOutput out = conf.getObjectOutput();
            SerializerImplementations.writeShort(i, out);
            byte[] b = out.getBuffer();
            FSTObjectInput in = conf.getObjectInput(b);
            int j = SerializerImplementations.readShort(in);
            assertThat(j, is(i));
        }
    }
    
    @Test
    public void test_Int() throws IOException {
        FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        for (long i = -2147483648; i < 2147483648L; i += 1000){
            FSTObjectOutput out = conf.getObjectOutput();
            SerializerImplementations.writeInt((int) i, out);
            byte[] b = out.getBuffer();
            FSTObjectInput in = conf.getObjectInput(b);
            int j = SerializerImplementations.readInt(in);
            assertThat(j, is((int) i));
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

        Duration four = Duration.ofSeconds(-2147483647);
        ser = serde.serializer().serialize("", four);
        res = serde.deserializer().deserialize("", ser);
        assertThat(res, is(four));

        Duration three = Duration.ofSeconds(-8388607);
        ser = serde.serializer().serialize("", three);
        res = serde.deserializer().deserialize("", ser);
        assertThat(res, is(three));

        Duration two = Duration.ofSeconds(-32767);
        ser = serde.serializer().serialize("", two);
        res = serde.deserializer().deserialize("", ser);
        assertThat(res, is(two));

        Duration one = Duration.ofSeconds(-127);
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
