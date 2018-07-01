/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jahto.utils.FSTSerializers;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

/**
 *
 * @author jah
 */
public class SerializerImplementations {

    public static Object deserializeLocalTime(FSTObjectInput in) throws IOException {
        long nsecs = in.readLong();
        Object res = LocalTime.ofNanoOfDay(nsecs);
        return res;
    }

    public static void serializeLocalTime(Object toWrite, FSTObjectOutput out) throws IOException {
        LocalTime ld = (LocalTime) toWrite;
        out.writeLong(ld.toNanoOfDay());
    }

    public static Object deserializeLocalDate(FSTObjectInput in) throws IOException {
        short year = in.readShort();
        short month = in.readShort();
        short day = in.readShort();
        Object res = LocalDate.of(year, month, day);
        return res;
    }

    public static void serializeLocalDate(Object toWrite, FSTObjectOutput out) throws IOException {
        LocalDate ld = (LocalDate) toWrite;
        out.writeShort(ld.getYear());
        out.writeShort(ld.getMonthValue());
        out.writeShort(ld.getDayOfMonth());
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

    public static Object deserializeZoneId(FSTObjectInput in) throws IOException {
        String id = in.readStringUTF();
        Object res = ZoneId.of(id);
        //        in.registerObject(res,streamPosition,serializationInfo, referencee); can skip as alwaysCopy is true
        return res;
    }

    public static void serializeZoneId(Object toWrite, FSTObjectOutput out) throws IOException {
        ZoneId id = (ZoneId) toWrite;
        out.writeStringUTF(id.getId());
    }

    public static Object deserializeZonedDateTime(FSTObjectInput in) throws IOException {
        LocalDate date = (LocalDate) deserializeLocalDate(in);
        LocalTime time = (LocalTime) deserializeLocalTime(in);
        ZoneId zone = (ZoneId) deserializeZoneId(in);
        Object res = ZonedDateTime.of(date, time, zone);
        //        in.registerObject(res,streamPosition,serializationInfo, referencee); can skip as alwaysCopy is true
        return res;
    }

    public static void serializeZonedDateTime(Object toWrite, FSTObjectOutput out) throws IOException {
        ZonedDateTime zdt = (ZonedDateTime) toWrite;
        serializeLocalDate(zdt.toLocalDate(), out);
        serializeLocalTime(zdt.toLocalTime(), out);
        serializeZoneId(zdt.getZone(), out);
    }
}
