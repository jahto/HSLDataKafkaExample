/*
 * Copyright 2015-2018 the original author or authors.
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
package com.github.jahto.utils.FSTSerializers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

/**
 *
 * @author Jouni Ahto
 */
public class SerializerImplementations {

    public static Object deserializeLocalTime(FSTObjectInput in) throws IOException {
        byte h = in.readByte();
        boolean hasMinutes = (h & 0b10000000) == 0;
        boolean hasSeconds = (h & 0b01000000) == 0;
        boolean hasNanos = (h & 0b00100000) == 0;
        h &= 0b00011111;
        byte m = 0;
        byte s = 0;
        int n = 0;
        if (hasMinutes) {
            m = in.readByte();
        }
        if (hasSeconds) {
            s = in.readByte();
        }
        if (hasNanos) {
            n = in.readInt();
        }
        Object res = LocalTime.of(h, m, s, n);
        return res;
    }

    public static void serializeLocalTime(Object toWrite, FSTObjectOutput out) throws IOException {
        LocalTime ld = (LocalTime) toWrite;
        boolean hasMinutes = ld.getMinute() != 0;
        boolean hasSeconds = ld.getSecond() != 0;
        boolean hasNanos = ld.getNano() != 0;
        byte hour = (byte) ld.getHour();
        if (!hasMinutes) {
            hour |= 0b10000000;
        }
        if (!hasSeconds) {
            hour |= 0b01000000;
        }
        if (!hasNanos) {
            hour |= 0b00100000;
        }
        out.writeByte(hour);
        if (hasMinutes) {
            out.writeByte(ld.getMinute());
        }
        if (hasSeconds) {
            out.writeByte(ld.getSecond());
        }
        if (hasNanos) {
            out.writeInt(ld.getNano());
        }
    }

    public static Object deserializeLocalDate(FSTObjectInput in) throws IOException {
        int year = in.readInt();
        byte month = in.readByte();
        byte day = in.readByte();
        Object res = LocalDate.of(year, month, day);
        return res;
    }

    public static void serializeLocalDate(Object toWrite, FSTObjectOutput out) throws IOException {
        LocalDate ld = (LocalDate) toWrite;
        out.writeInt(ld.getYear());
        out.writeByte(ld.getMonthValue());
        out.writeByte(ld.getDayOfMonth());
    }

    public static void serializeInstant(Object toWrite, FSTObjectOutput out) throws IOException {
        Instant inst = (Instant) toWrite;
        long s = inst.getEpochSecond();
        int n = inst.getNano();
        if (s < 0) {
            s = ~s;
            s |= 0x80000000_00000000L;
        }
        if (n != 0) {
            s |= 0x40000000_00000000L;
        }
        out.writeLong(s);
        if (n != 0) {
            out.writeInt(inst.getNano());
        }
    }

    public static Object deserializeInstant(FSTObjectInput in) throws IOException {
        long seconds = in.readLong();
        boolean hasNanos = (seconds & 0x40000000_00000000L) != 0;
        boolean isNegative = (seconds & 0x80000000_00000000L) != 0;
        seconds &= 0x0FFFFFFF_FFFFFFFFL;
        if (isNegative) {
            seconds = ~seconds;
        }
        int nanos = 0;
        if (hasNanos) {
            nanos = in.readInt();
        }
        Object res = Instant.ofEpochSecond(seconds, nanos);
        return res;
    }

    public static Object deserializeZonedDateTime(FSTObjectInput in) throws IOException {
        LocalDate date = (LocalDate) deserializeLocalDate(in);
        LocalTime time = (LocalTime) deserializeLocalTime(in);
        ZoneId zone = (ZoneId) deserializeZoneId(in);
        Object res = ZonedDateTime.of(date, time, zone);
        return res;
    }

    public static void serializeZonedDateTime(Object toWrite, FSTObjectOutput out) throws IOException {
        ZonedDateTime zdt = (ZonedDateTime) toWrite;
        serializeLocalDate(zdt.toLocalDate(), out);
        serializeLocalTime(zdt.toLocalTime(), out);
        serializeZoneId(zdt.getZone(), out);
    }

    public static Object deserializeLocalDateTime(FSTObjectInput in) throws IOException {
        LocalDate date = (LocalDate) deserializeLocalDate(in);
        LocalTime time = (LocalTime) deserializeLocalTime(in);
        Object res = LocalDateTime.of(date, time);
        return res;
    }

    public static void serializeLocalDateTime(Object toWrite, FSTObjectOutput out) throws IOException {
        LocalDateTime zdt = (LocalDateTime) toWrite;
        serializeLocalDate(zdt.toLocalDate(), out);
        serializeLocalTime(zdt.toLocalTime(), out);
    }

    public static Object deserializeOffsetDateTime(FSTObjectInput in) throws IOException {
        LocalDate date = (LocalDate) deserializeLocalDate(in);
        LocalTime time = (LocalTime) deserializeLocalTime(in);
        ZoneOffset off = (ZoneOffset) deserializeZoneOffset(in);
        Object res = OffsetDateTime.of(date, time, off);
        return res;
    }

    public static void serializeOffsetDateTime(Object toWrite, FSTObjectOutput out) throws IOException {
        OffsetDateTime zdt = (OffsetDateTime) toWrite;
        serializeLocalDate(zdt.toLocalDate(), out);
        serializeLocalTime(zdt.toLocalTime(), out);
        serializeZoneOffset(zdt.getOffset(), out);
    }

    public static Object deserializePeriod(FSTObjectInput in) throws IOException {
        int y = in.readInt();
        int m = in.readInt();
        int d = in.readInt();
        Period res = Period.of(y, m, d);
        return res;
    }

    public static void serializePeriod(Object toWrite, FSTObjectOutput out) throws IOException {
        Period p = (Period) toWrite;
        out.writeInt(p.getYears());
        out.writeInt(p.getMonths());
        out.writeInt(p.getDays());
    }

    public static Object deserializeDuration(FSTObjectInput in) throws IOException {
        long s = in.readLong();
        int n  = in.readInt();
        Duration res = Duration.ofSeconds(s, n);
        return res;
    }

    public static void serializeDuration(Object toWrite, FSTObjectOutput out) throws IOException {
        Duration d = (Duration) toWrite;
        out.writeLong(d.getSeconds());
        out.writeInt(d.getNano());
    }

    public static Object deserializeYear(FSTObjectInput in) throws IOException {
        int y = in.readInt();
        Year res = Year.of(y);
        return res;
    }

    public static void serializeYear(Object toWrite, FSTObjectOutput out) throws IOException {
        Year y = (Year) toWrite;
        out.writeInt(y.getValue());
    }

    public static Object deserializeYearMonth(FSTObjectInput in) throws IOException {
        int y = in.readInt();
        byte m = in.readByte();
        YearMonth res = YearMonth.of(y, m);
        return res;
    }

    public static void serializeYearMonth(Object toWrite, FSTObjectOutput out) throws IOException {
        YearMonth y = (YearMonth) toWrite;
        out.writeInt(y.getYear());
        out.writeByte(y.getMonthValue());
    }

    public static Object deserializeMonthDay(FSTObjectInput in) throws IOException {
        byte m = in.readByte();
        byte d = in.readByte();
        MonthDay res = MonthDay.of(m, d);
        return res;
    }

    public static void serializeMonthDay(Object toWrite, FSTObjectOutput out) throws IOException {
        MonthDay md = (MonthDay) toWrite;
        out.writeByte(md.getMonthValue());
        out.writeByte(md.getDayOfMonth());
    }

    public static Object deserializeZoneId(FSTObjectInput in) throws IOException {
        String id = in.readStringUTF();
        Object res = ZoneId.of(id);
        return res;
    }

    public static void serializeZoneId(Object toWrite, FSTObjectOutput out) throws IOException {
        ZoneId id = (ZoneId) toWrite;
        out.writeStringUTF(id.getId());
    }

    public static Object deserializeZoneOffset(FSTObjectInput in) throws IOException {
        int s = in.readByte();
        // Idea and code taken from JDK sources
        s = (s == 127 ? readThreeByteInt(in) : s * 900);
        Object res = ZoneOffset.ofTotalSeconds(s);
        return res;
    }

    public static void serializeZoneOffset(Object toWrite, FSTObjectOutput out) throws IOException {
        ZoneOffset id = (ZoneOffset) toWrite;
        int s = id.getTotalSeconds();
        // Idea and code taken from JDK sources
        s = s % 900 == 0 ? s / 900 : 127; // compress to -72 to +72
        out.writeByte(s);
        if (s == 127) {
            writeThreeByteInt(s, out);
        }
    }

    public static void writeThreeByteInt(int val, FSTObjectOutput out) throws IOException {
        // Check that the value will fit in 23 bits, one bit is needed for the sign.
        if (val > 8388607) {
            // Add throw.
        }
        if (val < -8388608) {
            // Add throw.
        }
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(val);
        byte[] arr = bb.array();
        out.writeByte(arr[1]);
        out.writeByte(arr[2]);
        out.writeByte(arr[3]);
    }

    public static int readThreeByteInt(FSTObjectInput in) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(4);
        byte one = in.readByte();
        byte two = in.readByte();
        byte three = in.readByte();
        // Check if one is negative first.
        if (one < 0) {
            bb.put(0, (byte) -1);
        }
        else {
            bb.put(0, (byte) 0);
        }
        bb.put(1, one);
        bb.put(2, two);
        bb.put(3, three);
        int res = bb.getInt();
        return res;
    }
}
