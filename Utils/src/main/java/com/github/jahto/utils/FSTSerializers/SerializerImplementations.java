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

    public static Object deSerializeInstant(FSTObjectInput in) throws IOException {
        long seconds = in.readLong();
        int nanos = in.readInt();
        Object res = Instant.ofEpochSecond(seconds, nanos);
        return res;
    }
    
}
