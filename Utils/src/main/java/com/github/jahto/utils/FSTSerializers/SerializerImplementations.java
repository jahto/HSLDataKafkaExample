/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jahto.utils.FSTSerializers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
        out.writeLong(inst.getEpochSecond());
        out.writeInt(inst.getNano());
    }

    public static Object deserializeInstant(FSTObjectInput in) throws IOException {
        long seconds = in.readLong();
        int nanos = in.readInt();
        Object res = Instant.ofEpochSecond(seconds, nanos);
        return res;
    }

    public static void serializeInstantWithoutNanos(Object toWrite, FSTObjectOutput out) throws IOException {
        Instant inst = (Instant) toWrite;
        out.writeLong(inst.getEpochSecond());
    }

    public static Object deserializeInstantWithoutNanos(FSTObjectInput in) throws IOException {
        long seconds = in.readLong();
        Object res = Instant.ofEpochSecond(seconds);
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
        return null;
    }

    public static void serializeOffsetDateTime(Object toWrite, FSTObjectOutput out) throws IOException {
    }

    public static Object deserializeOffsetDateTimeWithoutNanos(FSTObjectInput in) throws IOException {
        return null;
    }

    public static void serializeOffsetDateTimeWithoutNanos(Object toWrite, FSTObjectOutput out) throws IOException {
    }

    public static Object deserializePeriod(FSTObjectInput in) throws IOException {
        return null;
    }

    public static void serializePeriod(Object toWrite, FSTObjectOutput out) throws IOException {
    }

    public static Object deserializeDuration(FSTObjectInput in) throws IOException {
        return null;
    }

    public static void serializeDuration(Object toWrite, FSTObjectOutput out) throws IOException {
    }

    public static Object deserializeDurationWithoutNanos(FSTObjectInput in) throws IOException {
        return null;
    }

    public static void serializeDurationWithoutNanos(Object toWrite, FSTObjectOutput out) throws IOException {
    }

    public static Object deserializeYear(FSTObjectInput in) throws IOException {
        return null;
    }

    public static void serializeYear(Object toWrite, FSTObjectOutput out) throws IOException {
    }

    public static Object deserializeYearMonth(FSTObjectInput in) throws IOException {
        return null;
    }

    public static void serializeYearMonth(Object toWrite, FSTObjectOutput out) throws IOException {
    }

    public static Object deserializeMonthDay(FSTObjectInput in) throws IOException {
        return null;
    }

    public static void serializeMonthDay(Object toWrite, FSTObjectOutput out) throws IOException {
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
        //int s = readThreeByteInt(in);
        int s = in.readInt();
        Object res = ZoneOffset.ofTotalSeconds(s);
        return res;
    }

    public static void serializeZoneOffset(Object toWrite, FSTObjectOutput out) throws IOException {
        ZoneOffset id = (ZoneOffset) toWrite;
        int s = id.getTotalSeconds();
        Duration dur = Duration.ofSeconds(s);
        out.writeInt(s);
        //writeThreeByteInt(s, out);
    }

    // Must handle negative values before being usable... Work in progress
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
        if (arr[0] == -1) {
            // Negative number.
            int i = 0;
        }
        out.writeByte(arr[1]);
        out.writeByte(arr[2]);
        out.writeByte(arr[3]);
    }

    // Must handle negative values before being usable... Work in progress
    public static int readThreeByteInt(FSTObjectInput in) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(4);
        byte one = in.readByte();
        byte two = in.readByte();
        byte three = in.readByte();
        // Check the flag in one first.
        bb.put(0, (byte) 0);
        bb.put(1, one);
        bb.put(2, two);
        bb.put(3, three);
        int res = bb.getInt();
        return res;
    }
}
