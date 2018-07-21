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
package com.github.jahto.utils.FSTSerializers.java.time;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

/**
 *
 * @author Jouni Ahto
 */
public class SerializerImplementations {

    private static final int YEAR_ADJUST = 2018;

    public static void serializeLocalTime(Object toWrite, FSTObjectOutput out) throws IOException {
        LocalTime ld = (LocalTime) toWrite;
        // Minutes guaranteed by Java specs to be in range 0...59, which fits in 5 bits,
        // and we can use there remaining 3 higher ones to indicate if the remaining fields
        // are 0, which we don't bother to write, or something else.
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

    public static void serializeInstant(Object toWrite, FSTObjectOutput out) throws IOException {
        Instant inst = (Instant) toWrite;
        // Guaranteed by Java specs to be in range -31557014167219200L...31556889864403199L,
        // so we have some extra bits to use for optimization, and can avoid writing nanos
        // totally if there aren't any.
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

    public static void serializeLocalDate(Object toWrite, FSTObjectOutput out) throws IOException {
        LocalDate ld = (LocalDate) toWrite;
        YearMonth ym = YearMonth.of(ld.getYear(), ld.getMonthValue());
        // See explanation in that method, there's a way to compress the value. 
        serializeYearMonth(ym, out);
        // Guaranteed by Java specs to fit in a byte
        out.writeByte(ld.getDayOfMonth());
    }

    public static Object deserializeLocalDate(FSTObjectInput in) throws IOException {
        YearMonth ym = (YearMonth) deserializeYearMonth(in);
        byte day = in.readByte();
        Object res = LocalDate.of(ym.getYear(), ym.getMonthValue(), day);
        return res;
    }

    public static void serializeZonedDateTime(Object toWrite, FSTObjectOutput out) throws IOException {
        ZonedDateTime zdt = (ZonedDateTime) toWrite;
        serializeLocalDate(zdt.toLocalDate(), out);
        serializeLocalTime(zdt.toLocalTime(), out);
        serializeZoneId(zdt.getZone(), out);
    }

    public static Object deserializeZonedDateTime(FSTObjectInput in) throws IOException {
        LocalDate date = (LocalDate) deserializeLocalDate(in);
        LocalTime time = (LocalTime) deserializeLocalTime(in);
        ZoneId zone = (ZoneId) deserializeZoneId(in);
        Object res = ZonedDateTime.of(date, time, zone);
        return res;
    }

    public static void serializeLocalDateTime(Object toWrite, FSTObjectOutput out) throws IOException {
        LocalDateTime zdt = (LocalDateTime) toWrite;
        serializeLocalDate(zdt.toLocalDate(), out);
        serializeLocalTime(zdt.toLocalTime(), out);
    }

    public static Object deserializeLocalDateTime(FSTObjectInput in) throws IOException {
        LocalDate date = (LocalDate) deserializeLocalDate(in);
        LocalTime time = (LocalTime) deserializeLocalTime(in);
        Object res = LocalDateTime.of(date, time);
        return res;
    }

    public static void serializeOffsetDateTime(Object toWrite, FSTObjectOutput out) throws IOException {
        OffsetDateTime zdt = (OffsetDateTime) toWrite;
        serializeLocalDate(zdt.toLocalDate(), out);
        serializeLocalTime(zdt.toLocalTime(), out);
        serializeZoneOffset(zdt.getOffset(), out);
    }

    public static Object deserializeOffsetDateTime(FSTObjectInput in) throws IOException {
        LocalDate date = (LocalDate) deserializeLocalDate(in);
        LocalTime time = (LocalTime) deserializeLocalTime(in);
        ZoneOffset off = (ZoneOffset) deserializeZoneOffset(in);
        Object res = OffsetDateTime.of(date, time, off);
        return res;
    }

    public static void serializePeriod(Object toWrite, FSTObjectOutput out) throws IOException {
        Period p = (Period) toWrite;
        // No limits in Java specs, so no chance to optimize
        out.writeInt(p.getYears());
        out.writeInt(p.getMonths());
        out.writeInt(p.getDays());
    }

    // Not implemented yet...
    public static void serializePeriodAlt(Object toWrite, FSTObjectOutput out) throws IOException {
        Period p = (Period) toWrite;
        /*
        Theory of Periods, and the most likely outcome is 40 bits, not 42...
        Assume that in most cases, years, months and days are in somewhat reasonable
        range, ie. not 50000000 years, -4000000 months, 75000000 days. Probably in most
        cases the values could be encoded in just one byte. So we'll add one extra byte
        containing that information.
        Bits 0-1, bytes needed for the day.
        Bits 2-3, bytes needed for the month.
        Bits 4-5, bytes needed for the year.
        Which leaves bits 6-7 for another use. Like encoding the information whether
        days, months and years even exist or are zero instead. Possible alternatives:
        noDays (00)
        noMonths (01)
        noYears (10)
        yearsNeitherMonthsOrDays (11)
        The last one was chosen somewhat arbitrarily, assuming that people needing
        something like years being 5 million don't really need the exact month and date.
        Sorry for the people who use Periods for predicting that the end of world will
        happen after 50000000 years from now, on ThirteenMillionthMonths day 7123456.
        They'll get 13 bytes written instead of the minimum 12, but I'd guess they
        have a time nearing infinity to wait anyway, and everyone else benefits.
        Besides, using Period was the wrong choice in any case.
        */
    }

    public static Object deserializePeriod(FSTObjectInput in) throws IOException {
        int y = in.readInt();
        int m = in.readInt();
        int d = in.readInt();
        Period res = Period.of(y, m, d);
        return res;
    }

    public static void serializeDuration(Object toWrite, FSTObjectOutput out) throws IOException {
        Duration d = (Duration) toWrite;
        // No limits in Java specs, so no chance to optimize
        out.writeLong(d.getSeconds());
        out.writeInt(d.getNano());
    }

    // Not implemented yet...
    public static void serializeDurationAlt(Object toWrite, FSTObjectOutput out) throws IOException {
        Duration d = (Duration) toWrite;
        /*
        Theory of durations. People needing seconds to not be in
        the range -2147483648...2147483647, ie. covering a little bit more
        than -68...67 years, do not probably really need the nanoseconds part.
        So just write an extra byte and encode that and how many bytes are
        actually needed for each part, or do the nanos even exist. Probably
        I don't bother to write read/write sevenByteLong, sixByteLong and
        fiveByteLong variations, althoug threeByteInt versions could be
        copy-pasted and modified. Writing that extra byte that need only one
        bit to encode if there are nanos or not leaves exactly 7 bits... 
        */
    }

    public static Object deserializeDuration(FSTObjectInput in) throws IOException {
        long s = in.readLong();
        int n = in.readInt();
        Duration res = Duration.ofSeconds(s, n);
        return res;
    }

    public static void serializeYear(Object toWrite, FSTObjectOutput out) throws IOException {
        Year y = (Year) toWrite;
        out.writeInt(y.getValue());
    }

    public static Object deserializeYear(FSTObjectInput in) throws IOException {
        int y = in.readInt();
        Year res = Year.of(y);
        return res;
    }

    public static void serializeYearMonth(Object toWrite, FSTObjectOutput out) throws IOException {
        YearMonth ym = (YearMonth) toWrite;
        // Month guaranteed by Java specs to in range 1...12. Which leaves
        // the upper bits free to use for decoding to express if the year needs
        // 1, 2, 3 or 4 bytes. My hunch is that the year is in most common use
        // cases somewhere near present time (2018 when this was written) and
        // can then be compressed to one byte.
        byte m = (byte) ym.getMonthValue();
        int y = ym.getYear() - YEAR_ADJUST;
        boolean oneByteYear = false;
        boolean twoByteYear = false;
        boolean threeByteYear = false;
        boolean negativeYear = y < 0;
        if (negativeYear) {
            m |= 0b00010000;
            y = ~y;
        }
        if (y < 128) {
            oneByteYear = true;
            m |= 0b00100000;
        } else if (y < 32768) {
            twoByteYear = true;
            m |= 0b01000000;
        } else if (y < 8388608) {
            threeByteYear = true;
            m |= 0b10000000;
        }
        out.writeByte(m);
        if (oneByteYear) {
            out.writeByte(y);
        } else if (twoByteYear) {
            out.writeShort(y);
        } else if (threeByteYear) {
            writeThreeByteInt(y, out);
        } else {
            out.writeInt(y);
        }
    }

    public static Object deserializeYearMonth(FSTObjectInput in) throws IOException {
        int y;
        byte m = in.readByte();
        boolean negativeYear = (m & 0b00010000) != 0;
        boolean oneByteYear = (m & 0b00100000) != 0;
        boolean twoByteYear = (m & 0b01000000) != 0;
        boolean threeByteYear = (m & 0b10000000) != 0;
        m &= 0b00001111;
        if (oneByteYear) {
            y = in.readByte();
        } else if (twoByteYear) {
            y = in.readShort();
        } else if (threeByteYear) {
            y = readThreeByteInt(in);
        } else {
            y = in.readInt();
        }
        if (negativeYear) {
            y -= YEAR_ADJUST;
            y = ~y;
        } else {
            y += YEAR_ADJUST;
        }
        YearMonth res = YearMonth.of(y, m);
        return res;
    }

    public static void serializeMonthDay(Object toWrite, FSTObjectOutput out) throws IOException {
        MonthDay md = (MonthDay) toWrite;
        // Both guaranteed by Java specs to fit in a byte
        out.writeByte(md.getMonthValue());
        out.writeByte(md.getDayOfMonth());
    }

    public static Object deserializeMonthDay(FSTObjectInput in) throws IOException {
        byte m = in.readByte();
        byte d = in.readByte();
        MonthDay res = MonthDay.of(m, d);
        return res;
    }

    public static void serializeZoneId(Object toWrite, FSTObjectOutput out) throws IOException {
        // Serialization works even with some future zoneids that aren't in the maps.
        // Like Catalonia finally going independent and a new zone Europe/Barcelona
        // being created. At least the prefix Europe/ will be compressed. Another thing
        // is what happens if the deserializing side hasn't been updated and doesn't
        // know anything about the new zone... This implementation will throw an exception,
        // Java deserialization can handle even that case.
        ZoneId zoneid = (ZoneId) toWrite;
        String id = zoneid.getId();
        // Implementation 1.
        /*
        if (ZONE_TO_BYTE.containsKey(id)) {
            byte b = ZONE_TO_BYTE.get(id);
            out.writeByte(b);
        } else {
            out.writeByte(0);
            int i = id.lastIndexOf('/');
            // There's at least some kind of prefix
            if (i > 0) {
                String prefix = id.substring(0, i);
                String sid = id.substring(i + 1);
                if (PREFIX_TO_BYTE.containsKey(prefix)) {
                    byte b = PREFIX_TO_BYTE.get(prefix);
                    out.writeByte(b);
                    out.writeUTF(sid);
                } else {
                    out.writeByte(0);
                    out.writeUTF(id);
                }
            } else {
                out.writeByte(0);
                out.writeUTF(id);
            }
        }
        */
        // Implementation 2.
        if (ZONE_TO_SHORT.containsKey(id)) {
            short s = ZONE_TO_SHORT.get(id);
            out.writeShort(s);
        } else {
            int i = id.lastIndexOf('/');
            // There's at least some kind of prefix
            if (i > 0) {
                String prefix = id.substring(0, i);
                String sid = id.substring(i + 1);
                if (PREFIX_TO_BYTE.containsKey(prefix)) {
                    byte b = PREFIX_TO_BYTE.get(prefix);
                    out.writeShort(b);
                    out.writeUTF(sid);
                } else {
                    out.writeShort(0);
                    out.writeUTF(id);
                }
            } else {
                out.writeShort(0);
                out.writeUTF(id);
            }
        }
    }

    // Should do something to possible exceptions if the id is not valid...
    public static Object deserializeZoneId(FSTObjectInput in) throws IOException {
        // Impelementation 1. 
        /*
        byte b = in.readByte();
        if (b != 0) {
            String s = BYTE_TO_ZONE.get(b);
            ZoneId zone = ZoneId.of(s);
            return zone;
        }
        b = in.readByte();
        if (b != 0) {
            String prefix = BYTE_TO_PREFIX.get(b);
            String s = in.readUTF();
            String zid = prefix + s;
            ZoneId zone = ZoneId.of(zid);
            return zone;
        }
        String id = in.readUTF();
        ZoneId zone = ZoneId.of(id);
        */
        // Implementation 2. 
        short s = in.readShort();
        if (s > 255) {
            String zid = SHORT_TO_ZONE.get(s);
            ZoneId zone = ZoneId.of(zid);
            return zone;
        }
        if (s > 0) {
            String prefix = BYTE_TO_PREFIX.get((byte) s);
            String id = in.readUTF();
            String zid = prefix + id;
            ZoneId zone = ZoneId.of(zid);
            return zone;
        }
        String id = in.readUTF();
        ZoneId zone = ZoneId.of(id);
        return zone;
    }

    public static void serializeZoneOffset(Object toWrite, FSTObjectOutput out) throws IOException {
        ZoneOffset id = (ZoneOffset) toWrite;
        int s = id.getTotalSeconds();
        // Idea and code taken from JDK sources
        s = s % 900 == 0 ? s / 900 : 127; // compress to -72 to +72
        out.writeByte(s);
        if (s == 127) {
            // Offset guaranteed to be in range -64800...64800, so will fit in three bytes.
            writeThreeByteInt(s, out);
        }
    }

    public static Object deserializeZoneOffset(FSTObjectInput in) throws IOException {
        int s = in.readByte();
        // Idea and code taken from JDK sources
        s = (s == 127 ? readThreeByteInt(in) : s * 900);
        Object res = ZoneOffset.ofTotalSeconds(s);
        return res;
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
        } else {
            bb.put(0, (byte) 0);
        }
        bb.put(1, one);
        bb.put(2, two);
        bb.put(3, three);
        int res = bb.getInt();
        return res;
    }

    private static final Map<Byte, String> BYTE_TO_ZONE = new HashMap<>();
    private static final Map<String, Byte> ZONE_TO_BYTE = new HashMap<>();
    private static final Map<Byte, String> BYTE_TO_PREFIX = new HashMap<>();
    private static final Map<String, Byte> PREFIX_TO_BYTE = new HashMap<>();
    private static final Map<Short, String> SHORT_TO_ZONE = new HashMap<>();
    private static final Map<String, Short> ZONE_TO_SHORT = new HashMap<>();

    static {
        PREFIX_TO_BYTE.put("Africa", (byte) 1);
        PREFIX_TO_BYTE.put("America", (byte) 2);
        PREFIX_TO_BYTE.put("America/Argentina", (byte) 3);
        PREFIX_TO_BYTE.put("America/Indiana", (byte) 4);
        PREFIX_TO_BYTE.put("America/Kentucky", (byte) 5);
        PREFIX_TO_BYTE.put("America/North_Dakota", (byte) 6);
        PREFIX_TO_BYTE.put("Antarctica", (byte) 7);
        PREFIX_TO_BYTE.put("Asia", (byte) 8);
        PREFIX_TO_BYTE.put("Atlantic", (byte) 9);
        PREFIX_TO_BYTE.put("Australia", (byte) 10);
        PREFIX_TO_BYTE.put("Europe", (byte) 11);
        PREFIX_TO_BYTE.put("Indian", (byte) 12);
        PREFIX_TO_BYTE.put("Pacific", (byte) 13);
        PREFIX_TO_BYTE.put("Etc", (byte) 14);
        PREFIX_TO_BYTE.put("Arctic", (byte) 15);
        PREFIX_TO_BYTE.put("Brazil", (byte) 16);
        PREFIX_TO_BYTE.put("Canada", (byte) 17);
        PREFIX_TO_BYTE.put("Mexico", (byte) 18);
        PREFIX_TO_BYTE.put("SystemV", (byte) 19);
        PREFIX_TO_BYTE.put("US", (byte) 20);
        // Preparing for the near future...
        PREFIX_TO_BYTE.put("Moon", (byte) 254);
        PREFIX_TO_BYTE.put("Mars", (byte) 255);

        BYTE_TO_PREFIX.put((byte) 1, "Africa/");
        BYTE_TO_PREFIX.put((byte) 2, "America/");
        BYTE_TO_PREFIX.put((byte) 3, "America/Argentina/");
        BYTE_TO_PREFIX.put((byte) 4, "America/Indiana/");
        BYTE_TO_PREFIX.put((byte) 5, "America/Kentucky/");
        BYTE_TO_PREFIX.put((byte) 6, "America/North_Dakota/");
        BYTE_TO_PREFIX.put((byte) 7, "Antarctica/");
        BYTE_TO_PREFIX.put((byte) 8, "Asia/");
        BYTE_TO_PREFIX.put((byte) 9, "Atlantic/");
        BYTE_TO_PREFIX.put((byte) 10, "Australia/");
        BYTE_TO_PREFIX.put((byte) 11, "Europe/");
        BYTE_TO_PREFIX.put((byte) 12, "Indian/");
        BYTE_TO_PREFIX.put((byte) 13, "Pacific/");
        BYTE_TO_PREFIX.put((byte) 14, "Etc/");
        BYTE_TO_PREFIX.put((byte) 15, "Arctic/");
        BYTE_TO_PREFIX.put((byte) 16, "Brazil/");
        BYTE_TO_PREFIX.put((byte) 17, "Canada/");
        BYTE_TO_PREFIX.put((byte) 18, "Mexico/");
        BYTE_TO_PREFIX.put((byte) 19, "SystemV/");
        BYTE_TO_PREFIX.put((byte) 20, "US/");
        // Preparing for the near future...
        BYTE_TO_PREFIX.put((byte) 254, "Moon/");
        BYTE_TO_PREFIX.put((byte) 255, "Mars/");

        // Contents of these maps not yet decided, but Etc/UTC
        // definitely belongs here and is good enough for testing.
        ZONE_TO_BYTE.put("Etc/UTC", (byte) 1);
        BYTE_TO_ZONE.put((byte) 1, "Etc/UTC");

        // These maps contain all the current timezones available in Java
        ZONE_TO_SHORT.put("Africa/Abidjan", (short) 256);
        ZONE_TO_SHORT.put("Africa/Accra", (short) 257);
        ZONE_TO_SHORT.put("Africa/Addis_Ababa", (short) 258);
        ZONE_TO_SHORT.put("Africa/Algiers", (short) 259);
        ZONE_TO_SHORT.put("Africa/Asmara", (short) 260);
        ZONE_TO_SHORT.put("Africa/Asmera", (short) 261);
        ZONE_TO_SHORT.put("Africa/Bamako", (short) 262);
        ZONE_TO_SHORT.put("Africa/Bangui", (short) 263);
        ZONE_TO_SHORT.put("Africa/Banjul", (short) 264);
        ZONE_TO_SHORT.put("Africa/Bissau", (short) 265);
        ZONE_TO_SHORT.put("Africa/Blantyre", (short) 266);
        ZONE_TO_SHORT.put("Africa/Brazzaville", (short) 267);
        ZONE_TO_SHORT.put("Africa/Bujumbura", (short) 268);
        ZONE_TO_SHORT.put("Africa/Cairo", (short) 269);
        ZONE_TO_SHORT.put("Africa/Casablanca", (short) 270);
        ZONE_TO_SHORT.put("Africa/Ceuta", (short) 271);
        ZONE_TO_SHORT.put("Africa/Conakry", (short) 272);
        ZONE_TO_SHORT.put("Africa/Dakar", (short) 273);
        ZONE_TO_SHORT.put("Africa/Dar_es_Salaam", (short) 274);
        ZONE_TO_SHORT.put("Africa/Djibouti", (short) 275);
        ZONE_TO_SHORT.put("Africa/Douala", (short) 276);
        ZONE_TO_SHORT.put("Africa/El_Aaiun", (short) 277);
        ZONE_TO_SHORT.put("Africa/Freetown", (short) 278);
        ZONE_TO_SHORT.put("Africa/Gaborone", (short) 279);
        ZONE_TO_SHORT.put("Africa/Harare", (short) 280);
        ZONE_TO_SHORT.put("Africa/Johannesburg", (short) 281);
        ZONE_TO_SHORT.put("Africa/Juba", (short) 282);
        ZONE_TO_SHORT.put("Africa/Kampala", (short) 283);
        ZONE_TO_SHORT.put("Africa/Khartoum", (short) 284);
        ZONE_TO_SHORT.put("Africa/Kigali", (short) 285);
        ZONE_TO_SHORT.put("Africa/Kinshasa", (short) 286);
        ZONE_TO_SHORT.put("Africa/Lagos", (short) 287);
        ZONE_TO_SHORT.put("Africa/Libreville", (short) 288);
        ZONE_TO_SHORT.put("Africa/Lome", (short) 289);
        ZONE_TO_SHORT.put("Africa/Luanda", (short) 290);
        ZONE_TO_SHORT.put("Africa/Lubumbashi", (short) 291);
        ZONE_TO_SHORT.put("Africa/Lusaka", (short) 292);
        ZONE_TO_SHORT.put("Africa/Malabo", (short) 293);
        ZONE_TO_SHORT.put("Africa/Maputo", (short) 294);
        ZONE_TO_SHORT.put("Africa/Maseru", (short) 295);
        ZONE_TO_SHORT.put("Africa/Mbabane", (short) 296);
        ZONE_TO_SHORT.put("Africa/Mogadishu", (short) 297);
        ZONE_TO_SHORT.put("Africa/Monrovia", (short) 298);
        ZONE_TO_SHORT.put("Africa/Nairobi", (short) 299);
        ZONE_TO_SHORT.put("Africa/Ndjamena", (short) 300);
        ZONE_TO_SHORT.put("Africa/Niamey", (short) 301);
        ZONE_TO_SHORT.put("Africa/Nouakchott", (short) 302);
        ZONE_TO_SHORT.put("Africa/Ouagadougou", (short) 303);
        ZONE_TO_SHORT.put("Africa/Porto-Novo", (short) 304);
        ZONE_TO_SHORT.put("Africa/Sao_Tome", (short) 305);
        ZONE_TO_SHORT.put("Africa/Timbuktu", (short) 306);
        ZONE_TO_SHORT.put("Africa/Tripoli", (short) 307);
        ZONE_TO_SHORT.put("Africa/Tunis", (short) 308);
        ZONE_TO_SHORT.put("Africa/Windhoek", (short) 309);
        ZONE_TO_SHORT.put("America/Adak", (short) 310);
        ZONE_TO_SHORT.put("America/Anchorage", (short) 311);
        ZONE_TO_SHORT.put("America/Anguilla", (short) 312);
        ZONE_TO_SHORT.put("America/Antigua", (short) 313);
        ZONE_TO_SHORT.put("America/Araguaina", (short) 314);
        ZONE_TO_SHORT.put("America/Argentina/Buenos_Aires", (short) 315);
        ZONE_TO_SHORT.put("America/Argentina/Catamarca", (short) 316);
        ZONE_TO_SHORT.put("America/Argentina/ComodRivadavia", (short) 317);
        ZONE_TO_SHORT.put("America/Argentina/Cordoba", (short) 318);
        ZONE_TO_SHORT.put("America/Argentina/Jujuy", (short) 319);
        ZONE_TO_SHORT.put("America/Argentina/La_Rioja", (short) 320);
        ZONE_TO_SHORT.put("America/Argentina/Mendoza", (short) 321);
        ZONE_TO_SHORT.put("America/Argentina/Rio_Gallegos", (short) 322);
        ZONE_TO_SHORT.put("America/Argentina/Salta", (short) 323);
        ZONE_TO_SHORT.put("America/Argentina/San_Juan", (short) 324);
        ZONE_TO_SHORT.put("America/Argentina/San_Luis", (short) 325);
        ZONE_TO_SHORT.put("America/Argentina/Tucuman", (short) 326);
        ZONE_TO_SHORT.put("America/Argentina/Ushuaia", (short) 327);
        ZONE_TO_SHORT.put("America/Aruba", (short) 328);
        ZONE_TO_SHORT.put("America/Asuncion", (short) 329);
        ZONE_TO_SHORT.put("America/Atikokan", (short) 330);
        ZONE_TO_SHORT.put("America/Atka", (short) 331);
        ZONE_TO_SHORT.put("America/Bahia", (short) 332);
        ZONE_TO_SHORT.put("America/Bahia_Banderas", (short) 333);
        ZONE_TO_SHORT.put("America/Barbados", (short) 334);
        ZONE_TO_SHORT.put("America/Belem", (short) 335);
        ZONE_TO_SHORT.put("America/Belize", (short) 336);
        ZONE_TO_SHORT.put("America/Blanc-Sablon", (short) 337);
        ZONE_TO_SHORT.put("America/Boa_Vista", (short) 338);
        ZONE_TO_SHORT.put("America/Bogota", (short) 339);
        ZONE_TO_SHORT.put("America/Boise", (short) 340);
        ZONE_TO_SHORT.put("America/Buenos_Aires", (short) 341);
        ZONE_TO_SHORT.put("America/Cambridge_Bay", (short) 342);
        ZONE_TO_SHORT.put("America/Campo_Grande", (short) 343);
        ZONE_TO_SHORT.put("America/Cancun", (short) 344);
        ZONE_TO_SHORT.put("America/Caracas", (short) 345);
        ZONE_TO_SHORT.put("America/Catamarca", (short) 346);
        ZONE_TO_SHORT.put("America/Cayenne", (short) 347);
        ZONE_TO_SHORT.put("America/Cayman", (short) 348);
        ZONE_TO_SHORT.put("America/Chicago", (short) 349);
        ZONE_TO_SHORT.put("America/Chihuahua", (short) 350);
        ZONE_TO_SHORT.put("America/Coral_Harbour", (short) 351);
        ZONE_TO_SHORT.put("America/Cordoba", (short) 352);
        ZONE_TO_SHORT.put("America/Costa_Rica", (short) 353);
        ZONE_TO_SHORT.put("America/Creston", (short) 354);
        ZONE_TO_SHORT.put("America/Cuiaba", (short) 355);
        ZONE_TO_SHORT.put("America/Curacao", (short) 356);
        ZONE_TO_SHORT.put("America/Danmarkshavn", (short) 357);
        ZONE_TO_SHORT.put("America/Dawson", (short) 358);
        ZONE_TO_SHORT.put("America/Dawson_Creek", (short) 359);
        ZONE_TO_SHORT.put("America/Denver", (short) 360);
        ZONE_TO_SHORT.put("America/Detroit", (short) 361);
        ZONE_TO_SHORT.put("America/Dominica", (short) 362);
        ZONE_TO_SHORT.put("America/Edmonton", (short) 363);
        ZONE_TO_SHORT.put("America/Eirunepe", (short) 364);
        ZONE_TO_SHORT.put("America/El_Salvador", (short) 365);
        ZONE_TO_SHORT.put("America/Ensenada", (short) 366);
        ZONE_TO_SHORT.put("America/Fort_Nelson", (short) 367);
        ZONE_TO_SHORT.put("America/Fort_Wayne", (short) 368);
        ZONE_TO_SHORT.put("America/Fortaleza", (short) 369);
        ZONE_TO_SHORT.put("America/Glace_Bay", (short) 370);
        ZONE_TO_SHORT.put("America/Godthab", (short) 371);
        ZONE_TO_SHORT.put("America/Goose_Bay", (short) 372);
        ZONE_TO_SHORT.put("America/Grand_Turk", (short) 373);
        ZONE_TO_SHORT.put("America/Grenada", (short) 374);
        ZONE_TO_SHORT.put("America/Guadeloupe", (short) 375);
        ZONE_TO_SHORT.put("America/Guatemala", (short) 376);
        ZONE_TO_SHORT.put("America/Guayaquil", (short) 377);
        ZONE_TO_SHORT.put("America/Guyana", (short) 378);
        ZONE_TO_SHORT.put("America/Halifax", (short) 379);
        ZONE_TO_SHORT.put("America/Havana", (short) 380);
        ZONE_TO_SHORT.put("America/Hermosillo", (short) 381);
        ZONE_TO_SHORT.put("America/Indiana/Indianapolis", (short) 382);
        ZONE_TO_SHORT.put("America/Indiana/Knox", (short) 383);
        ZONE_TO_SHORT.put("America/Indiana/Marengo", (short) 384);
        ZONE_TO_SHORT.put("America/Indiana/Petersburg", (short) 385);
        ZONE_TO_SHORT.put("America/Indiana/Tell_City", (short) 386);
        ZONE_TO_SHORT.put("America/Indiana/Vevay", (short) 387);
        ZONE_TO_SHORT.put("America/Indiana/Vincennes", (short) 388);
        ZONE_TO_SHORT.put("America/Indiana/Winamac", (short) 389);
        ZONE_TO_SHORT.put("America/Indianapolis", (short) 390);
        ZONE_TO_SHORT.put("America/Inuvik", (short) 391);
        ZONE_TO_SHORT.put("America/Iqaluit", (short) 392);
        ZONE_TO_SHORT.put("America/Jamaica", (short) 393);
        ZONE_TO_SHORT.put("America/Jujuy", (short) 394);
        ZONE_TO_SHORT.put("America/Juneau", (short) 395);
        ZONE_TO_SHORT.put("America/Kentucky/Louisville", (short) 396);
        ZONE_TO_SHORT.put("America/Kentucky/Monticello", (short) 397);
        ZONE_TO_SHORT.put("America/Knox_IN", (short) 398);
        ZONE_TO_SHORT.put("America/Kralendijk", (short) 399);
        ZONE_TO_SHORT.put("America/La_Paz", (short) 400);
        ZONE_TO_SHORT.put("America/Lima", (short) 401);
        ZONE_TO_SHORT.put("America/Los_Angeles", (short) 402);
        ZONE_TO_SHORT.put("America/Louisville", (short) 403);
        ZONE_TO_SHORT.put("America/Lower_Princes", (short) 404);
        ZONE_TO_SHORT.put("America/Maceio", (short) 405);
        ZONE_TO_SHORT.put("America/Managua", (short) 406);
        ZONE_TO_SHORT.put("America/Manaus", (short) 407);
        ZONE_TO_SHORT.put("America/Marigot", (short) 408);
        ZONE_TO_SHORT.put("America/Martinique", (short) 409);
        ZONE_TO_SHORT.put("America/Matamoros", (short) 410);
        ZONE_TO_SHORT.put("America/Mazatlan", (short) 411);
        ZONE_TO_SHORT.put("America/Mendoza", (short) 412);
        ZONE_TO_SHORT.put("America/Menominee", (short) 413);
        ZONE_TO_SHORT.put("America/Merida", (short) 414);
        ZONE_TO_SHORT.put("America/Metlakatla", (short) 415);
        ZONE_TO_SHORT.put("America/Mexico_City", (short) 416);
        ZONE_TO_SHORT.put("America/Miquelon", (short) 417);
        ZONE_TO_SHORT.put("America/Moncton", (short) 418);
        ZONE_TO_SHORT.put("America/Monterrey", (short) 419);
        ZONE_TO_SHORT.put("America/Montevideo", (short) 420);
        ZONE_TO_SHORT.put("America/Montreal", (short) 421);
        ZONE_TO_SHORT.put("America/Montserrat", (short) 422);
        ZONE_TO_SHORT.put("America/Nassau", (short) 423);
        ZONE_TO_SHORT.put("America/New_York", (short) 424);
        ZONE_TO_SHORT.put("America/Nipigon", (short) 425);
        ZONE_TO_SHORT.put("America/Nome", (short) 426);
        ZONE_TO_SHORT.put("America/Noronha", (short) 427);
        ZONE_TO_SHORT.put("America/North_Dakota/Beulah", (short) 428);
        ZONE_TO_SHORT.put("America/North_Dakota/Center", (short) 429);
        ZONE_TO_SHORT.put("America/North_Dakota/New_Salem", (short) 430);
        ZONE_TO_SHORT.put("America/Ojinaga", (short) 431);
        ZONE_TO_SHORT.put("America/Panama", (short) 432);
        ZONE_TO_SHORT.put("America/Pangnirtung", (short) 433);
        ZONE_TO_SHORT.put("America/Paramaribo", (short) 434);
        ZONE_TO_SHORT.put("America/Phoenix", (short) 435);
        ZONE_TO_SHORT.put("America/Port-au-Prince", (short) 436);
        ZONE_TO_SHORT.put("America/Port_of_Spain", (short) 437);
        ZONE_TO_SHORT.put("America/Porto_Acre", (short) 438);
        ZONE_TO_SHORT.put("America/Porto_Velho", (short) 439);
        ZONE_TO_SHORT.put("America/Puerto_Rico", (short) 440);
        ZONE_TO_SHORT.put("America/Punta_Arenas", (short) 441);
        ZONE_TO_SHORT.put("America/Rainy_River", (short) 442);
        ZONE_TO_SHORT.put("America/Rankin_Inlet", (short) 443);
        ZONE_TO_SHORT.put("America/Recife", (short) 444);
        ZONE_TO_SHORT.put("America/Regina", (short) 445);
        ZONE_TO_SHORT.put("America/Resolute", (short) 446);
        ZONE_TO_SHORT.put("America/Rio_Branco", (short) 447);
        ZONE_TO_SHORT.put("America/Rosario", (short) 448);
        ZONE_TO_SHORT.put("America/Santa_Isabel", (short) 449);
        ZONE_TO_SHORT.put("America/Santarem", (short) 450);
        ZONE_TO_SHORT.put("America/Santiago", (short) 451);
        ZONE_TO_SHORT.put("America/Santo_Domingo", (short) 452);
        ZONE_TO_SHORT.put("America/Sao_Paulo", (short) 453);
        ZONE_TO_SHORT.put("America/Scoresbysund", (short) 454);
        ZONE_TO_SHORT.put("America/Shiprock", (short) 455);
        ZONE_TO_SHORT.put("America/Sitka", (short) 456);
        ZONE_TO_SHORT.put("America/St_Barthelemy", (short) 457);
        ZONE_TO_SHORT.put("America/St_Johns", (short) 458);
        ZONE_TO_SHORT.put("America/St_Kitts", (short) 459);
        ZONE_TO_SHORT.put("America/St_Lucia", (short) 460);
        ZONE_TO_SHORT.put("America/St_Thomas", (short) 461);
        ZONE_TO_SHORT.put("America/St_Vincent", (short) 462);
        ZONE_TO_SHORT.put("America/Swift_Current", (short) 463);
        ZONE_TO_SHORT.put("America/Tegucigalpa", (short) 464);
        ZONE_TO_SHORT.put("America/Thule", (short) 465);
        ZONE_TO_SHORT.put("America/Thunder_Bay", (short) 466);
        ZONE_TO_SHORT.put("America/Tijuana", (short) 467);
        ZONE_TO_SHORT.put("America/Toronto", (short) 468);
        ZONE_TO_SHORT.put("America/Tortola", (short) 469);
        ZONE_TO_SHORT.put("America/Vancouver", (short) 470);
        ZONE_TO_SHORT.put("America/Virgin", (short) 471);
        ZONE_TO_SHORT.put("America/Whitehorse", (short) 472);
        ZONE_TO_SHORT.put("America/Winnipeg", (short) 473);
        ZONE_TO_SHORT.put("America/Yakutat", (short) 474);
        ZONE_TO_SHORT.put("America/Yellowknife", (short) 475);
        ZONE_TO_SHORT.put("Antarctica/Casey", (short) 476);
        ZONE_TO_SHORT.put("Antarctica/Davis", (short) 477);
        ZONE_TO_SHORT.put("Antarctica/DumontDUrville", (short) 478);
        ZONE_TO_SHORT.put("Antarctica/Macquarie", (short) 479);
        ZONE_TO_SHORT.put("Antarctica/Mawson", (short) 480);
        ZONE_TO_SHORT.put("Antarctica/McMurdo", (short) 481);
        ZONE_TO_SHORT.put("Antarctica/Palmer", (short) 482);
        ZONE_TO_SHORT.put("Antarctica/Rothera", (short) 483);
        ZONE_TO_SHORT.put("Antarctica/South_Pole", (short) 484);
        ZONE_TO_SHORT.put("Antarctica/Syowa", (short) 485);
        ZONE_TO_SHORT.put("Antarctica/Troll", (short) 486);
        ZONE_TO_SHORT.put("Antarctica/Vostok", (short) 487);
        ZONE_TO_SHORT.put("Arctic/Longyearbyen", (short) 488);
        ZONE_TO_SHORT.put("Asia/Aden", (short) 489);
        ZONE_TO_SHORT.put("Asia/Almaty", (short) 490);
        ZONE_TO_SHORT.put("Asia/Amman", (short) 491);
        ZONE_TO_SHORT.put("Asia/Anadyr", (short) 492);
        ZONE_TO_SHORT.put("Asia/Aqtau", (short) 493);
        ZONE_TO_SHORT.put("Asia/Aqtobe", (short) 494);
        ZONE_TO_SHORT.put("Asia/Ashgabat", (short) 495);
        ZONE_TO_SHORT.put("Asia/Ashkhabad", (short) 496);
        ZONE_TO_SHORT.put("Asia/Atyrau", (short) 497);
        ZONE_TO_SHORT.put("Asia/Baghdad", (short) 498);
        ZONE_TO_SHORT.put("Asia/Bahrain", (short) 499);
        ZONE_TO_SHORT.put("Asia/Baku", (short) 500);
        ZONE_TO_SHORT.put("Asia/Bangkok", (short) 501);
        ZONE_TO_SHORT.put("Asia/Barnaul", (short) 502);
        ZONE_TO_SHORT.put("Asia/Beirut", (short) 503);
        ZONE_TO_SHORT.put("Asia/Bishkek", (short) 504);
        ZONE_TO_SHORT.put("Asia/Brunei", (short) 505);
        ZONE_TO_SHORT.put("Asia/Calcutta", (short) 506);
        ZONE_TO_SHORT.put("Asia/Chita", (short) 507);
        ZONE_TO_SHORT.put("Asia/Choibalsan", (short) 508);
        ZONE_TO_SHORT.put("Asia/Chongqing", (short) 509);
        ZONE_TO_SHORT.put("Asia/Chungking", (short) 510);
        ZONE_TO_SHORT.put("Asia/Colombo", (short) 511);
        ZONE_TO_SHORT.put("Asia/Dacca", (short) 512);
        ZONE_TO_SHORT.put("Asia/Damascus", (short) 513);
        ZONE_TO_SHORT.put("Asia/Dhaka", (short) 514);
        ZONE_TO_SHORT.put("Asia/Dili", (short) 515);
        ZONE_TO_SHORT.put("Asia/Dubai", (short) 516);
        ZONE_TO_SHORT.put("Asia/Dushanbe", (short) 517);
        ZONE_TO_SHORT.put("Asia/Famagusta", (short) 518);
        ZONE_TO_SHORT.put("Asia/Gaza", (short) 519);
        ZONE_TO_SHORT.put("Asia/Harbin", (short) 520);
        ZONE_TO_SHORT.put("Asia/Hebron", (short) 521);
        ZONE_TO_SHORT.put("Asia/Ho_Chi_Minh", (short) 522);
        ZONE_TO_SHORT.put("Asia/Hong_Kong", (short) 523);
        ZONE_TO_SHORT.put("Asia/Hovd", (short) 524);
        ZONE_TO_SHORT.put("Asia/Irkutsk", (short) 525);
        ZONE_TO_SHORT.put("Asia/Istanbul", (short) 526);
        ZONE_TO_SHORT.put("Asia/Jakarta", (short) 527);
        ZONE_TO_SHORT.put("Asia/Jayapura", (short) 528);
        ZONE_TO_SHORT.put("Asia/Jerusalem", (short) 529);
        ZONE_TO_SHORT.put("Asia/Kabul", (short) 530);
        ZONE_TO_SHORT.put("Asia/Kamchatka", (short) 531);
        ZONE_TO_SHORT.put("Asia/Karachi", (short) 532);
        ZONE_TO_SHORT.put("Asia/Kashgar", (short) 533);
        ZONE_TO_SHORT.put("Asia/Kathmandu", (short) 534);
        ZONE_TO_SHORT.put("Asia/Katmandu", (short) 535);
        ZONE_TO_SHORT.put("Asia/Khandyga", (short) 536);
        ZONE_TO_SHORT.put("Asia/Kolkata", (short) 537);
        ZONE_TO_SHORT.put("Asia/Krasnoyarsk", (short) 538);
        ZONE_TO_SHORT.put("Asia/Kuala_Lumpur", (short) 539);
        ZONE_TO_SHORT.put("Asia/Kuching", (short) 540);
        ZONE_TO_SHORT.put("Asia/Kuwait", (short) 541);
        ZONE_TO_SHORT.put("Asia/Macao", (short) 542);
        ZONE_TO_SHORT.put("Asia/Macau", (short) 543);
        ZONE_TO_SHORT.put("Asia/Magadan", (short) 544);
        ZONE_TO_SHORT.put("Asia/Makassar", (short) 545);
        ZONE_TO_SHORT.put("Asia/Manila", (short) 546);
        ZONE_TO_SHORT.put("Asia/Muscat", (short) 547);
        ZONE_TO_SHORT.put("Asia/Nicosia", (short) 548);
        ZONE_TO_SHORT.put("Asia/Novokuznetsk", (short) 549);
        ZONE_TO_SHORT.put("Asia/Novosibirsk", (short) 550);
        ZONE_TO_SHORT.put("Asia/Omsk", (short) 551);
        ZONE_TO_SHORT.put("Asia/Oral", (short) 552);
        ZONE_TO_SHORT.put("Asia/Phnom_Penh", (short) 553);
        ZONE_TO_SHORT.put("Asia/Pontianak", (short) 554);
        ZONE_TO_SHORT.put("Asia/Pyongyang", (short) 555);
        ZONE_TO_SHORT.put("Asia/Qatar", (short) 556);
        ZONE_TO_SHORT.put("Asia/Qyzylorda", (short) 557);
        ZONE_TO_SHORT.put("Asia/Rangoon", (short) 558);
        ZONE_TO_SHORT.put("Asia/Riyadh", (short) 559);
        ZONE_TO_SHORT.put("Asia/Saigon", (short) 560);
        ZONE_TO_SHORT.put("Asia/Sakhalin", (short) 561);
        ZONE_TO_SHORT.put("Asia/Samarkand", (short) 562);
        ZONE_TO_SHORT.put("Asia/Seoul", (short) 563);
        ZONE_TO_SHORT.put("Asia/Shanghai", (short) 564);
        ZONE_TO_SHORT.put("Asia/Singapore", (short) 565);
        ZONE_TO_SHORT.put("Asia/Srednekolymsk", (short) 566);
        ZONE_TO_SHORT.put("Asia/Taipei", (short) 567);
        ZONE_TO_SHORT.put("Asia/Tashkent", (short) 568);
        ZONE_TO_SHORT.put("Asia/Tbilisi", (short) 569);
        ZONE_TO_SHORT.put("Asia/Tehran", (short) 570);
        ZONE_TO_SHORT.put("Asia/Tel_Aviv", (short) 571);
        ZONE_TO_SHORT.put("Asia/Thimbu", (short) 572);
        ZONE_TO_SHORT.put("Asia/Thimphu", (short) 573);
        ZONE_TO_SHORT.put("Asia/Tokyo", (short) 574);
        ZONE_TO_SHORT.put("Asia/Tomsk", (short) 575);
        ZONE_TO_SHORT.put("Asia/Ujung_Pandang", (short) 576);
        ZONE_TO_SHORT.put("Asia/Ulaanbaatar", (short) 577);
        ZONE_TO_SHORT.put("Asia/Ulan_Bator", (short) 578);
        ZONE_TO_SHORT.put("Asia/Urumqi", (short) 579);
        ZONE_TO_SHORT.put("Asia/Ust-Nera", (short) 580);
        ZONE_TO_SHORT.put("Asia/Vientiane", (short) 581);
        ZONE_TO_SHORT.put("Asia/Vladivostok", (short) 582);
        ZONE_TO_SHORT.put("Asia/Yakutsk", (short) 583);
        ZONE_TO_SHORT.put("Asia/Yangon", (short) 584);
        ZONE_TO_SHORT.put("Asia/Yekaterinburg", (short) 585);
        ZONE_TO_SHORT.put("Asia/Yerevan", (short) 586);
        ZONE_TO_SHORT.put("Atlantic/Azores", (short) 587);
        ZONE_TO_SHORT.put("Atlantic/Bermuda", (short) 588);
        ZONE_TO_SHORT.put("Atlantic/Canary", (short) 589);
        ZONE_TO_SHORT.put("Atlantic/Cape_Verde", (short) 590);
        ZONE_TO_SHORT.put("Atlantic/Faeroe", (short) 591);
        ZONE_TO_SHORT.put("Atlantic/Faroe", (short) 592);
        ZONE_TO_SHORT.put("Atlantic/Jan_Mayen", (short) 593);
        ZONE_TO_SHORT.put("Atlantic/Madeira", (short) 594);
        ZONE_TO_SHORT.put("Atlantic/Reykjavik", (short) 595);
        ZONE_TO_SHORT.put("Atlantic/South_Georgia", (short) 596);
        ZONE_TO_SHORT.put("Atlantic/St_Helena", (short) 597);
        ZONE_TO_SHORT.put("Atlantic/Stanley", (short) 598);
        ZONE_TO_SHORT.put("Australia/ACT", (short) 599);
        ZONE_TO_SHORT.put("Australia/Adelaide", (short) 600);
        ZONE_TO_SHORT.put("Australia/Brisbane", (short) 601);
        ZONE_TO_SHORT.put("Australia/Broken_Hill", (short) 602);
        ZONE_TO_SHORT.put("Australia/Canberra", (short) 603);
        ZONE_TO_SHORT.put("Australia/Currie", (short) 604);
        ZONE_TO_SHORT.put("Australia/Darwin", (short) 605);
        ZONE_TO_SHORT.put("Australia/Eucla", (short) 606);
        ZONE_TO_SHORT.put("Australia/Hobart", (short) 607);
        ZONE_TO_SHORT.put("Australia/LHI", (short) 608);
        ZONE_TO_SHORT.put("Australia/Lindeman", (short) 609);
        ZONE_TO_SHORT.put("Australia/Lord_Howe", (short) 610);
        ZONE_TO_SHORT.put("Australia/Melbourne", (short) 611);
        ZONE_TO_SHORT.put("Australia/NSW", (short) 612);
        ZONE_TO_SHORT.put("Australia/North", (short) 613);
        ZONE_TO_SHORT.put("Australia/Perth", (short) 614);
        ZONE_TO_SHORT.put("Australia/Queensland", (short) 615);
        ZONE_TO_SHORT.put("Australia/South", (short) 616);
        ZONE_TO_SHORT.put("Australia/Sydney", (short) 617);
        ZONE_TO_SHORT.put("Australia/Tasmania", (short) 618);
        ZONE_TO_SHORT.put("Australia/Victoria", (short) 619);
        ZONE_TO_SHORT.put("Australia/West", (short) 620);
        ZONE_TO_SHORT.put("Australia/Yancowinna", (short) 621);
        ZONE_TO_SHORT.put("Brazil/Acre", (short) 622);
        ZONE_TO_SHORT.put("Brazil/DeNoronha", (short) 623);
        ZONE_TO_SHORT.put("Brazil/East", (short) 624);
        ZONE_TO_SHORT.put("Brazil/West", (short) 625);
        ZONE_TO_SHORT.put("CET", (short) 626);
        ZONE_TO_SHORT.put("CST6CDT", (short) 627);
        ZONE_TO_SHORT.put("Canada/Atlantic", (short) 628);
        ZONE_TO_SHORT.put("Canada/Central", (short) 629);
        ZONE_TO_SHORT.put("Canada/Eastern", (short) 630);
        ZONE_TO_SHORT.put("Canada/Mountain", (short) 631);
        ZONE_TO_SHORT.put("Canada/Newfoundland", (short) 632);
        ZONE_TO_SHORT.put("Canada/Pacific", (short) 633);
        ZONE_TO_SHORT.put("Canada/Saskatchewan", (short) 634);
        ZONE_TO_SHORT.put("Canada/Yukon", (short) 635);
        ZONE_TO_SHORT.put("Chile/Continental", (short) 636);
        ZONE_TO_SHORT.put("Chile/EasterIsland", (short) 637);
        ZONE_TO_SHORT.put("Cuba", (short) 638);
        ZONE_TO_SHORT.put("EET", (short) 639);
        ZONE_TO_SHORT.put("EST5EDT", (short) 640);
        ZONE_TO_SHORT.put("Egypt", (short) 641);
        ZONE_TO_SHORT.put("Eire", (short) 642);
        ZONE_TO_SHORT.put("Etc/GMT", (short) 643);
        ZONE_TO_SHORT.put("Etc/GMT+0", (short) 644);
        ZONE_TO_SHORT.put("Etc/GMT+1", (short) 645);
        ZONE_TO_SHORT.put("Etc/GMT+10", (short) 646);
        ZONE_TO_SHORT.put("Etc/GMT+11", (short) 647);
        ZONE_TO_SHORT.put("Etc/GMT+12", (short) 648);
        ZONE_TO_SHORT.put("Etc/GMT+2", (short) 649);
        ZONE_TO_SHORT.put("Etc/GMT+3", (short) 650);
        ZONE_TO_SHORT.put("Etc/GMT+4", (short) 651);
        ZONE_TO_SHORT.put("Etc/GMT+5", (short) 652);
        ZONE_TO_SHORT.put("Etc/GMT+6", (short) 653);
        ZONE_TO_SHORT.put("Etc/GMT+7", (short) 654);
        ZONE_TO_SHORT.put("Etc/GMT+8", (short) 655);
        ZONE_TO_SHORT.put("Etc/GMT+9", (short) 656);
        ZONE_TO_SHORT.put("Etc/GMT-0", (short) 657);
        ZONE_TO_SHORT.put("Etc/GMT-1", (short) 658);
        ZONE_TO_SHORT.put("Etc/GMT-10", (short) 659);
        ZONE_TO_SHORT.put("Etc/GMT-11", (short) 660);
        ZONE_TO_SHORT.put("Etc/GMT-12", (short) 661);
        ZONE_TO_SHORT.put("Etc/GMT-13", (short) 662);
        ZONE_TO_SHORT.put("Etc/GMT-14", (short) 663);
        ZONE_TO_SHORT.put("Etc/GMT-2", (short) 664);
        ZONE_TO_SHORT.put("Etc/GMT-3", (short) 665);
        ZONE_TO_SHORT.put("Etc/GMT-4", (short) 666);
        ZONE_TO_SHORT.put("Etc/GMT-5", (short) 667);
        ZONE_TO_SHORT.put("Etc/GMT-6", (short) 668);
        ZONE_TO_SHORT.put("Etc/GMT-7", (short) 669);
        ZONE_TO_SHORT.put("Etc/GMT-8", (short) 670);
        ZONE_TO_SHORT.put("Etc/GMT-9", (short) 671);
        ZONE_TO_SHORT.put("Etc/GMT0", (short) 672);
        ZONE_TO_SHORT.put("Etc/Greenwich", (short) 673);
        ZONE_TO_SHORT.put("Etc/UCT", (short) 674);
        ZONE_TO_SHORT.put("Etc/UTC", (short) 675);
        ZONE_TO_SHORT.put("Etc/Universal", (short) 676);
        ZONE_TO_SHORT.put("Etc/Zulu", (short) 677);
        ZONE_TO_SHORT.put("Europe/Amsterdam", (short) 678);
        ZONE_TO_SHORT.put("Europe/Andorra", (short) 679);
        ZONE_TO_SHORT.put("Europe/Astrakhan", (short) 680);
        ZONE_TO_SHORT.put("Europe/Athens", (short) 681);
        ZONE_TO_SHORT.put("Europe/Belfast", (short) 682);
        ZONE_TO_SHORT.put("Europe/Belgrade", (short) 683);
        ZONE_TO_SHORT.put("Europe/Berlin", (short) 684);
        ZONE_TO_SHORT.put("Europe/Bratislava", (short) 685);
        ZONE_TO_SHORT.put("Europe/Brussels", (short) 686);
        ZONE_TO_SHORT.put("Europe/Bucharest", (short) 687);
        ZONE_TO_SHORT.put("Europe/Budapest", (short) 688);
        ZONE_TO_SHORT.put("Europe/Busingen", (short) 689);
        ZONE_TO_SHORT.put("Europe/Chisinau", (short) 690);
        ZONE_TO_SHORT.put("Europe/Copenhagen", (short) 691);
        ZONE_TO_SHORT.put("Europe/Dublin", (short) 692);
        ZONE_TO_SHORT.put("Europe/Gibraltar", (short) 693);
        ZONE_TO_SHORT.put("Europe/Guernsey", (short) 694);
        ZONE_TO_SHORT.put("Europe/Helsinki", (short) 695);
        ZONE_TO_SHORT.put("Europe/Isle_of_Man", (short) 696);
        ZONE_TO_SHORT.put("Europe/Istanbul", (short) 697);
        ZONE_TO_SHORT.put("Europe/Jersey", (short) 698);
        ZONE_TO_SHORT.put("Europe/Kaliningrad", (short) 699);
        ZONE_TO_SHORT.put("Europe/Kiev", (short) 700);
        ZONE_TO_SHORT.put("Europe/Kirov", (short) 701);
        ZONE_TO_SHORT.put("Europe/Lisbon", (short) 702);
        ZONE_TO_SHORT.put("Europe/Ljubljana", (short) 703);
        ZONE_TO_SHORT.put("Europe/London", (short) 704);
        ZONE_TO_SHORT.put("Europe/Luxembourg", (short) 705);
        ZONE_TO_SHORT.put("Europe/Madrid", (short) 706);
        ZONE_TO_SHORT.put("Europe/Malta", (short) 707);
        ZONE_TO_SHORT.put("Europe/Mariehamn", (short) 708);
        ZONE_TO_SHORT.put("Europe/Minsk", (short) 709);
        ZONE_TO_SHORT.put("Europe/Monaco", (short) 710);
        ZONE_TO_SHORT.put("Europe/Moscow", (short) 711);
        ZONE_TO_SHORT.put("Europe/Nicosia", (short) 712);
        ZONE_TO_SHORT.put("Europe/Oslo", (short) 713);
        ZONE_TO_SHORT.put("Europe/Paris", (short) 714);
        ZONE_TO_SHORT.put("Europe/Podgorica", (short) 715);
        ZONE_TO_SHORT.put("Europe/Prague", (short) 716);
        ZONE_TO_SHORT.put("Europe/Riga", (short) 717);
        ZONE_TO_SHORT.put("Europe/Rome", (short) 718);
        ZONE_TO_SHORT.put("Europe/Samara", (short) 719);
        ZONE_TO_SHORT.put("Europe/San_Marino", (short) 720);
        ZONE_TO_SHORT.put("Europe/Sarajevo", (short) 721);
        ZONE_TO_SHORT.put("Europe/Saratov", (short) 722);
        ZONE_TO_SHORT.put("Europe/Simferopol", (short) 723);
        ZONE_TO_SHORT.put("Europe/Skopje", (short) 724);
        ZONE_TO_SHORT.put("Europe/Sofia", (short) 725);
        ZONE_TO_SHORT.put("Europe/Stockholm", (short) 726);
        ZONE_TO_SHORT.put("Europe/Tallinn", (short) 727);
        ZONE_TO_SHORT.put("Europe/Tirane", (short) 728);
        ZONE_TO_SHORT.put("Europe/Tiraspol", (short) 729);
        ZONE_TO_SHORT.put("Europe/Ulyanovsk", (short) 730);
        ZONE_TO_SHORT.put("Europe/Uzhgorod", (short) 731);
        ZONE_TO_SHORT.put("Europe/Vaduz", (short) 732);
        ZONE_TO_SHORT.put("Europe/Vatican", (short) 733);
        ZONE_TO_SHORT.put("Europe/Vienna", (short) 734);
        ZONE_TO_SHORT.put("Europe/Vilnius", (short) 735);
        ZONE_TO_SHORT.put("Europe/Volgograd", (short) 736);
        ZONE_TO_SHORT.put("Europe/Warsaw", (short) 737);
        ZONE_TO_SHORT.put("Europe/Zagreb", (short) 738);
        ZONE_TO_SHORT.put("Europe/Zaporozhye", (short) 739);
        ZONE_TO_SHORT.put("Europe/Zurich", (short) 740);
        ZONE_TO_SHORT.put("GB", (short) 741);
        ZONE_TO_SHORT.put("GB-Eire", (short) 742);
        ZONE_TO_SHORT.put("GMT", (short) 743);
        ZONE_TO_SHORT.put("GMT0", (short) 744);
        ZONE_TO_SHORT.put("Greenwich", (short) 745);
        ZONE_TO_SHORT.put("Hongkong", (short) 746);
        ZONE_TO_SHORT.put("Iceland", (short) 747);
        ZONE_TO_SHORT.put("Indian/Antananarivo", (short) 748);
        ZONE_TO_SHORT.put("Indian/Chagos", (short) 749);
        ZONE_TO_SHORT.put("Indian/Christmas", (short) 750);
        ZONE_TO_SHORT.put("Indian/Cocos", (short) 751);
        ZONE_TO_SHORT.put("Indian/Comoro", (short) 752);
        ZONE_TO_SHORT.put("Indian/Kerguelen", (short) 753);
        ZONE_TO_SHORT.put("Indian/Mahe", (short) 754);
        ZONE_TO_SHORT.put("Indian/Maldives", (short) 755);
        ZONE_TO_SHORT.put("Indian/Mauritius", (short) 756);
        ZONE_TO_SHORT.put("Indian/Mayotte", (short) 757);
        ZONE_TO_SHORT.put("Indian/Reunion", (short) 758);
        ZONE_TO_SHORT.put("Iran", (short) 759);
        ZONE_TO_SHORT.put("Israel", (short) 760);
        ZONE_TO_SHORT.put("Jamaica", (short) 761);
        ZONE_TO_SHORT.put("Japan", (short) 762);
        ZONE_TO_SHORT.put("Kwajalein", (short) 763);
        ZONE_TO_SHORT.put("Libya", (short) 764);
        ZONE_TO_SHORT.put("MET", (short) 765);
        ZONE_TO_SHORT.put("MST7MDT", (short) 766);
        ZONE_TO_SHORT.put("Mexico/BajaNorte", (short) 767);
        ZONE_TO_SHORT.put("Mexico/BajaSur", (short) 768);
        ZONE_TO_SHORT.put("Mexico/General", (short) 769);
        ZONE_TO_SHORT.put("NZ", (short) 770);
        ZONE_TO_SHORT.put("NZ-CHAT", (short) 771);
        ZONE_TO_SHORT.put("Navajo", (short) 772);
        ZONE_TO_SHORT.put("PRC", (short) 773);
        ZONE_TO_SHORT.put("PST8PDT", (short) 774);
        ZONE_TO_SHORT.put("Pacific/Apia", (short) 775);
        ZONE_TO_SHORT.put("Pacific/Auckland", (short) 776);
        ZONE_TO_SHORT.put("Pacific/Bougainville", (short) 777);
        ZONE_TO_SHORT.put("Pacific/Chatham", (short) 778);
        ZONE_TO_SHORT.put("Pacific/Chuuk", (short) 779);
        ZONE_TO_SHORT.put("Pacific/Easter", (short) 780);
        ZONE_TO_SHORT.put("Pacific/Efate", (short) 781);
        ZONE_TO_SHORT.put("Pacific/Enderbury", (short) 782);
        ZONE_TO_SHORT.put("Pacific/Fakaofo", (short) 783);
        ZONE_TO_SHORT.put("Pacific/Fiji", (short) 784);
        ZONE_TO_SHORT.put("Pacific/Funafuti", (short) 785);
        ZONE_TO_SHORT.put("Pacific/Galapagos", (short) 786);
        ZONE_TO_SHORT.put("Pacific/Gambier", (short) 787);
        ZONE_TO_SHORT.put("Pacific/Guadalcanal", (short) 788);
        ZONE_TO_SHORT.put("Pacific/Guam", (short) 789);
        ZONE_TO_SHORT.put("Pacific/Honolulu", (short) 790);
        ZONE_TO_SHORT.put("Pacific/Johnston", (short) 791);
        ZONE_TO_SHORT.put("Pacific/Kiritimati", (short) 792);
        ZONE_TO_SHORT.put("Pacific/Kosrae", (short) 793);
        ZONE_TO_SHORT.put("Pacific/Kwajalein", (short) 794);
        ZONE_TO_SHORT.put("Pacific/Majuro", (short) 795);
        ZONE_TO_SHORT.put("Pacific/Marquesas", (short) 796);
        ZONE_TO_SHORT.put("Pacific/Midway", (short) 797);
        ZONE_TO_SHORT.put("Pacific/Nauru", (short) 798);
        ZONE_TO_SHORT.put("Pacific/Niue", (short) 799);
        ZONE_TO_SHORT.put("Pacific/Norfolk", (short) 800);
        ZONE_TO_SHORT.put("Pacific/Noumea", (short) 801);
        ZONE_TO_SHORT.put("Pacific/Pago_Pago", (short) 802);
        ZONE_TO_SHORT.put("Pacific/Palau", (short) 803);
        ZONE_TO_SHORT.put("Pacific/Pitcairn", (short) 804);
        ZONE_TO_SHORT.put("Pacific/Pohnpei", (short) 805);
        ZONE_TO_SHORT.put("Pacific/Ponape", (short) 806);
        ZONE_TO_SHORT.put("Pacific/Port_Moresby", (short) 807);
        ZONE_TO_SHORT.put("Pacific/Rarotonga", (short) 808);
        ZONE_TO_SHORT.put("Pacific/Saipan", (short) 809);
        ZONE_TO_SHORT.put("Pacific/Samoa", (short) 810);
        ZONE_TO_SHORT.put("Pacific/Tahiti", (short) 811);
        ZONE_TO_SHORT.put("Pacific/Tarawa", (short) 812);
        ZONE_TO_SHORT.put("Pacific/Tongatapu", (short) 813);
        ZONE_TO_SHORT.put("Pacific/Truk", (short) 814);
        ZONE_TO_SHORT.put("Pacific/Wake", (short) 815);
        ZONE_TO_SHORT.put("Pacific/Wallis", (short) 816);
        ZONE_TO_SHORT.put("Pacific/Yap", (short) 817);
        ZONE_TO_SHORT.put("Poland", (short) 818);
        ZONE_TO_SHORT.put("Portugal", (short) 819);
        ZONE_TO_SHORT.put("ROK", (short) 820);
        ZONE_TO_SHORT.put("Singapore", (short) 821);
        ZONE_TO_SHORT.put("SystemV/AST4", (short) 822);
        ZONE_TO_SHORT.put("SystemV/AST4ADT", (short) 823);
        ZONE_TO_SHORT.put("SystemV/CST6", (short) 824);
        ZONE_TO_SHORT.put("SystemV/CST6CDT", (short) 825);
        ZONE_TO_SHORT.put("SystemV/EST5", (short) 826);
        ZONE_TO_SHORT.put("SystemV/EST5EDT", (short) 827);
        ZONE_TO_SHORT.put("SystemV/HST10", (short) 828);
        ZONE_TO_SHORT.put("SystemV/MST7", (short) 829);
        ZONE_TO_SHORT.put("SystemV/MST7MDT", (short) 830);
        ZONE_TO_SHORT.put("SystemV/PST8", (short) 831);
        ZONE_TO_SHORT.put("SystemV/PST8PDT", (short) 832);
        ZONE_TO_SHORT.put("SystemV/YST9", (short) 833);
        ZONE_TO_SHORT.put("SystemV/YST9YDT", (short) 834);
        ZONE_TO_SHORT.put("Turkey", (short) 835);
        ZONE_TO_SHORT.put("UCT", (short) 836);
        ZONE_TO_SHORT.put("US/Alaska", (short) 837);
        ZONE_TO_SHORT.put("US/Aleutian", (short) 838);
        ZONE_TO_SHORT.put("US/Arizona", (short) 839);
        ZONE_TO_SHORT.put("US/Central", (short) 840);
        ZONE_TO_SHORT.put("US/East-Indiana", (short) 841);
        ZONE_TO_SHORT.put("US/Eastern", (short) 842);
        ZONE_TO_SHORT.put("US/Hawaii", (short) 843);
        ZONE_TO_SHORT.put("US/Indiana-Starke", (short) 844);
        ZONE_TO_SHORT.put("US/Michigan", (short) 845);
        ZONE_TO_SHORT.put("US/Mountain", (short) 846);
        ZONE_TO_SHORT.put("US/Pacific", (short) 847);
        ZONE_TO_SHORT.put("US/Pacific-New", (short) 848);
        ZONE_TO_SHORT.put("US/Samoa", (short) 849);
        ZONE_TO_SHORT.put("UTC", (short) 850);
        ZONE_TO_SHORT.put("Universal", (short) 851);
        ZONE_TO_SHORT.put("W-SU", (short) 852);
        ZONE_TO_SHORT.put("WET", (short) 853);
        ZONE_TO_SHORT.put("Zulu", (short) 854);

        SHORT_TO_ZONE.put((short) 256, "Africa/Abidjan");
        SHORT_TO_ZONE.put((short) 257, "Africa/Accra");
        SHORT_TO_ZONE.put((short) 258, "Africa/Addis_Ababa");
        SHORT_TO_ZONE.put((short) 259, "Africa/Algiers");
        SHORT_TO_ZONE.put((short) 260, "Africa/Asmara");
        SHORT_TO_ZONE.put((short) 261, "Africa/Asmera");
        SHORT_TO_ZONE.put((short) 262, "Africa/Bamako");
        SHORT_TO_ZONE.put((short) 263, "Africa/Bangui");
        SHORT_TO_ZONE.put((short) 264, "Africa/Banjul");
        SHORT_TO_ZONE.put((short) 265, "Africa/Bissau");
        SHORT_TO_ZONE.put((short) 266, "Africa/Blantyre");
        SHORT_TO_ZONE.put((short) 267, "Africa/Brazzaville");
        SHORT_TO_ZONE.put((short) 268, "Africa/Bujumbura");
        SHORT_TO_ZONE.put((short) 269, "Africa/Cairo");
        SHORT_TO_ZONE.put((short) 270, "Africa/Casablanca");
        SHORT_TO_ZONE.put((short) 271, "Africa/Ceuta");
        SHORT_TO_ZONE.put((short) 272, "Africa/Conakry");
        SHORT_TO_ZONE.put((short) 273, "Africa/Dakar");
        SHORT_TO_ZONE.put((short) 274, "Africa/Dar_es_Salaam");
        SHORT_TO_ZONE.put((short) 275, "Africa/Djibouti");
        SHORT_TO_ZONE.put((short) 276, "Africa/Douala");
        SHORT_TO_ZONE.put((short) 277, "Africa/El_Aaiun");
        SHORT_TO_ZONE.put((short) 278, "Africa/Freetown");
        SHORT_TO_ZONE.put((short) 279, "Africa/Gaborone");
        SHORT_TO_ZONE.put((short) 280, "Africa/Harare");
        SHORT_TO_ZONE.put((short) 281, "Africa/Johannesburg");
        SHORT_TO_ZONE.put((short) 282, "Africa/Juba");
        SHORT_TO_ZONE.put((short) 283, "Africa/Kampala");
        SHORT_TO_ZONE.put((short) 284, "Africa/Khartoum");
        SHORT_TO_ZONE.put((short) 285, "Africa/Kigali");
        SHORT_TO_ZONE.put((short) 286, "Africa/Kinshasa");
        SHORT_TO_ZONE.put((short) 287, "Africa/Lagos");
        SHORT_TO_ZONE.put((short) 288, "Africa/Libreville");
        SHORT_TO_ZONE.put((short) 289, "Africa/Lome");
        SHORT_TO_ZONE.put((short) 290, "Africa/Luanda");
        SHORT_TO_ZONE.put((short) 291, "Africa/Lubumbashi");
        SHORT_TO_ZONE.put((short) 292, "Africa/Lusaka");
        SHORT_TO_ZONE.put((short) 293, "Africa/Malabo");
        SHORT_TO_ZONE.put((short) 294, "Africa/Maputo");
        SHORT_TO_ZONE.put((short) 295, "Africa/Maseru");
        SHORT_TO_ZONE.put((short) 296, "Africa/Mbabane");
        SHORT_TO_ZONE.put((short) 297, "Africa/Mogadishu");
        SHORT_TO_ZONE.put((short) 298, "Africa/Monrovia");
        SHORT_TO_ZONE.put((short) 299, "Africa/Nairobi");
        SHORT_TO_ZONE.put((short) 300, "Africa/Ndjamena");
        SHORT_TO_ZONE.put((short) 301, "Africa/Niamey");
        SHORT_TO_ZONE.put((short) 302, "Africa/Nouakchott");
        SHORT_TO_ZONE.put((short) 303, "Africa/Ouagadougou");
        SHORT_TO_ZONE.put((short) 304, "Africa/Porto-Novo");
        SHORT_TO_ZONE.put((short) 305, "Africa/Sao_Tome");
        SHORT_TO_ZONE.put((short) 306, "Africa/Timbuktu");
        SHORT_TO_ZONE.put((short) 307, "Africa/Tripoli");
        SHORT_TO_ZONE.put((short) 308, "Africa/Tunis");
        SHORT_TO_ZONE.put((short) 309, "Africa/Windhoek");
        SHORT_TO_ZONE.put((short) 310, "America/Adak");
        SHORT_TO_ZONE.put((short) 311, "America/Anchorage");
        SHORT_TO_ZONE.put((short) 312, "America/Anguilla");
        SHORT_TO_ZONE.put((short) 313, "America/Antigua");
        SHORT_TO_ZONE.put((short) 314, "America/Araguaina");
        SHORT_TO_ZONE.put((short) 315, "America/Argentina/Buenos_Aires");
        SHORT_TO_ZONE.put((short) 316, "America/Argentina/Catamarca");
        SHORT_TO_ZONE.put((short) 317, "America/Argentina/ComodRivadavia");
        SHORT_TO_ZONE.put((short) 318, "America/Argentina/Cordoba");
        SHORT_TO_ZONE.put((short) 319, "America/Argentina/Jujuy");
        SHORT_TO_ZONE.put((short) 320, "America/Argentina/La_Rioja");
        SHORT_TO_ZONE.put((short) 321, "America/Argentina/Mendoza");
        SHORT_TO_ZONE.put((short) 322, "America/Argentina/Rio_Gallegos");
        SHORT_TO_ZONE.put((short) 323, "America/Argentina/Salta");
        SHORT_TO_ZONE.put((short) 324, "America/Argentina/San_Juan");
        SHORT_TO_ZONE.put((short) 325, "America/Argentina/San_Luis");
        SHORT_TO_ZONE.put((short) 326, "America/Argentina/Tucuman");
        SHORT_TO_ZONE.put((short) 327, "America/Argentina/Ushuaia");
        SHORT_TO_ZONE.put((short) 328, "America/Aruba");
        SHORT_TO_ZONE.put((short) 329, "America/Asuncion");
        SHORT_TO_ZONE.put((short) 330, "America/Atikokan");
        SHORT_TO_ZONE.put((short) 331, "America/Atka");
        SHORT_TO_ZONE.put((short) 332, "America/Bahia");
        SHORT_TO_ZONE.put((short) 333, "America/Bahia_Banderas");
        SHORT_TO_ZONE.put((short) 334, "America/Barbados");
        SHORT_TO_ZONE.put((short) 335, "America/Belem");
        SHORT_TO_ZONE.put((short) 336, "America/Belize");
        SHORT_TO_ZONE.put((short) 337, "America/Blanc-Sablon");
        SHORT_TO_ZONE.put((short) 338, "America/Boa_Vista");
        SHORT_TO_ZONE.put((short) 339, "America/Bogota");
        SHORT_TO_ZONE.put((short) 340, "America/Boise");
        SHORT_TO_ZONE.put((short) 341, "America/Buenos_Aires");
        SHORT_TO_ZONE.put((short) 342, "America/Cambridge_Bay");
        SHORT_TO_ZONE.put((short) 343, "America/Campo_Grande");
        SHORT_TO_ZONE.put((short) 344, "America/Cancun");
        SHORT_TO_ZONE.put((short) 345, "America/Caracas");
        SHORT_TO_ZONE.put((short) 346, "America/Catamarca");
        SHORT_TO_ZONE.put((short) 347, "America/Cayenne");
        SHORT_TO_ZONE.put((short) 348, "America/Cayman");
        SHORT_TO_ZONE.put((short) 349, "America/Chicago");
        SHORT_TO_ZONE.put((short) 350, "America/Chihuahua");
        SHORT_TO_ZONE.put((short) 351, "America/Coral_Harbour");
        SHORT_TO_ZONE.put((short) 352, "America/Cordoba");
        SHORT_TO_ZONE.put((short) 353, "America/Costa_Rica");
        SHORT_TO_ZONE.put((short) 354, "America/Creston");
        SHORT_TO_ZONE.put((short) 355, "America/Cuiaba");
        SHORT_TO_ZONE.put((short) 356, "America/Curacao");
        SHORT_TO_ZONE.put((short) 357, "America/Danmarkshavn");
        SHORT_TO_ZONE.put((short) 358, "America/Dawson");
        SHORT_TO_ZONE.put((short) 359, "America/Dawson_Creek");
        SHORT_TO_ZONE.put((short) 360, "America/Denver");
        SHORT_TO_ZONE.put((short) 361, "America/Detroit");
        SHORT_TO_ZONE.put((short) 362, "America/Dominica");
        SHORT_TO_ZONE.put((short) 363, "America/Edmonton");
        SHORT_TO_ZONE.put((short) 364, "America/Eirunepe");
        SHORT_TO_ZONE.put((short) 365, "America/El_Salvador");
        SHORT_TO_ZONE.put((short) 366, "America/Ensenada");
        SHORT_TO_ZONE.put((short) 367, "America/Fort_Nelson");
        SHORT_TO_ZONE.put((short) 368, "America/Fort_Wayne");
        SHORT_TO_ZONE.put((short) 369, "America/Fortaleza");
        SHORT_TO_ZONE.put((short) 370, "America/Glace_Bay");
        SHORT_TO_ZONE.put((short) 371, "America/Godthab");
        SHORT_TO_ZONE.put((short) 372, "America/Goose_Bay");
        SHORT_TO_ZONE.put((short) 373, "America/Grand_Turk");
        SHORT_TO_ZONE.put((short) 374, "America/Grenada");
        SHORT_TO_ZONE.put((short) 375, "America/Guadeloupe");
        SHORT_TO_ZONE.put((short) 376, "America/Guatemala");
        SHORT_TO_ZONE.put((short) 377, "America/Guayaquil");
        SHORT_TO_ZONE.put((short) 378, "America/Guyana");
        SHORT_TO_ZONE.put((short) 379, "America/Halifax");
        SHORT_TO_ZONE.put((short) 380, "America/Havana");
        SHORT_TO_ZONE.put((short) 381, "America/Hermosillo");
        SHORT_TO_ZONE.put((short) 382, "America/Indiana/Indianapolis");
        SHORT_TO_ZONE.put((short) 383, "America/Indiana/Knox");
        SHORT_TO_ZONE.put((short) 384, "America/Indiana/Marengo");
        SHORT_TO_ZONE.put((short) 385, "America/Indiana/Petersburg");
        SHORT_TO_ZONE.put((short) 386, "America/Indiana/Tell_City");
        SHORT_TO_ZONE.put((short) 387, "America/Indiana/Vevay");
        SHORT_TO_ZONE.put((short) 388, "America/Indiana/Vincennes");
        SHORT_TO_ZONE.put((short) 389, "America/Indiana/Winamac");
        SHORT_TO_ZONE.put((short) 390, "America/Indianapolis");
        SHORT_TO_ZONE.put((short) 391, "America/Inuvik");
        SHORT_TO_ZONE.put((short) 392, "America/Iqaluit");
        SHORT_TO_ZONE.put((short) 393, "America/Jamaica");
        SHORT_TO_ZONE.put((short) 394, "America/Jujuy");
        SHORT_TO_ZONE.put((short) 395, "America/Juneau");
        SHORT_TO_ZONE.put((short) 396, "America/Kentucky/Louisville");
        SHORT_TO_ZONE.put((short) 397, "America/Kentucky/Monticello");
        SHORT_TO_ZONE.put((short) 398, "America/Knox_IN");
        SHORT_TO_ZONE.put((short) 399, "America/Kralendijk");
        SHORT_TO_ZONE.put((short) 400, "America/La_Paz");
        SHORT_TO_ZONE.put((short) 401, "America/Lima");
        SHORT_TO_ZONE.put((short) 402, "America/Los_Angeles");
        SHORT_TO_ZONE.put((short) 403, "America/Louisville");
        SHORT_TO_ZONE.put((short) 404, "America/Lower_Princes");
        SHORT_TO_ZONE.put((short) 405, "America/Maceio");
        SHORT_TO_ZONE.put((short) 406, "America/Managua");
        SHORT_TO_ZONE.put((short) 407, "America/Manaus");
        SHORT_TO_ZONE.put((short) 408, "America/Marigot");
        SHORT_TO_ZONE.put((short) 409, "America/Martinique");
        SHORT_TO_ZONE.put((short) 410, "America/Matamoros");
        SHORT_TO_ZONE.put((short) 411, "America/Mazatlan");
        SHORT_TO_ZONE.put((short) 412, "America/Mendoza");
        SHORT_TO_ZONE.put((short) 413, "America/Menominee");
        SHORT_TO_ZONE.put((short) 414, "America/Merida");
        SHORT_TO_ZONE.put((short) 415, "America/Metlakatla");
        SHORT_TO_ZONE.put((short) 416, "America/Mexico_City");
        SHORT_TO_ZONE.put((short) 417, "America/Miquelon");
        SHORT_TO_ZONE.put((short) 418, "America/Moncton");
        SHORT_TO_ZONE.put((short) 419, "America/Monterrey");
        SHORT_TO_ZONE.put((short) 420, "America/Montevideo");
        SHORT_TO_ZONE.put((short) 421, "America/Montreal");
        SHORT_TO_ZONE.put((short) 422, "America/Montserrat");
        SHORT_TO_ZONE.put((short) 423, "America/Nassau");
        SHORT_TO_ZONE.put((short) 424, "America/New_York");
        SHORT_TO_ZONE.put((short) 425, "America/Nipigon");
        SHORT_TO_ZONE.put((short) 426, "America/Nome");
        SHORT_TO_ZONE.put((short) 427, "America/Noronha");
        SHORT_TO_ZONE.put((short) 428, "America/North_Dakota/Beulah");
        SHORT_TO_ZONE.put((short) 429, "America/North_Dakota/Center");
        SHORT_TO_ZONE.put((short) 430, "America/North_Dakota/New_Salem");
        SHORT_TO_ZONE.put((short) 431, "America/Ojinaga");
        SHORT_TO_ZONE.put((short) 432, "America/Panama");
        SHORT_TO_ZONE.put((short) 433, "America/Pangnirtung");
        SHORT_TO_ZONE.put((short) 434, "America/Paramaribo");
        SHORT_TO_ZONE.put((short) 435, "America/Phoenix");
        SHORT_TO_ZONE.put((short) 436, "America/Port-au-Prince");
        SHORT_TO_ZONE.put((short) 437, "America/Port_of_Spain");
        SHORT_TO_ZONE.put((short) 438, "America/Porto_Acre");
        SHORT_TO_ZONE.put((short) 439, "America/Porto_Velho");
        SHORT_TO_ZONE.put((short) 440, "America/Puerto_Rico");
        SHORT_TO_ZONE.put((short) 441, "America/Punta_Arenas");
        SHORT_TO_ZONE.put((short) 442, "America/Rainy_River");
        SHORT_TO_ZONE.put((short) 443, "America/Rankin_Inlet");
        SHORT_TO_ZONE.put((short) 444, "America/Recife");
        SHORT_TO_ZONE.put((short) 445, "America/Regina");
        SHORT_TO_ZONE.put((short) 446, "America/Resolute");
        SHORT_TO_ZONE.put((short) 447, "America/Rio_Branco");
        SHORT_TO_ZONE.put((short) 448, "America/Rosario");
        SHORT_TO_ZONE.put((short) 449, "America/Santa_Isabel");
        SHORT_TO_ZONE.put((short) 450, "America/Santarem");
        SHORT_TO_ZONE.put((short) 451, "America/Santiago");
        SHORT_TO_ZONE.put((short) 452, "America/Santo_Domingo");
        SHORT_TO_ZONE.put((short) 453, "America/Sao_Paulo");
        SHORT_TO_ZONE.put((short) 454, "America/Scoresbysund");
        SHORT_TO_ZONE.put((short) 455, "America/Shiprock");
        SHORT_TO_ZONE.put((short) 456, "America/Sitka");
        SHORT_TO_ZONE.put((short) 457, "America/St_Barthelemy");
        SHORT_TO_ZONE.put((short) 458, "America/St_Johns");
        SHORT_TO_ZONE.put((short) 459, "America/St_Kitts");
        SHORT_TO_ZONE.put((short) 460, "America/St_Lucia");
        SHORT_TO_ZONE.put((short) 461, "America/St_Thomas");
        SHORT_TO_ZONE.put((short) 462, "America/St_Vincent");
        SHORT_TO_ZONE.put((short) 463, "America/Swift_Current");
        SHORT_TO_ZONE.put((short) 464, "America/Tegucigalpa");
        SHORT_TO_ZONE.put((short) 465, "America/Thule");
        SHORT_TO_ZONE.put((short) 466, "America/Thunder_Bay");
        SHORT_TO_ZONE.put((short) 467, "America/Tijuana");
        SHORT_TO_ZONE.put((short) 468, "America/Toronto");
        SHORT_TO_ZONE.put((short) 469, "America/Tortola");
        SHORT_TO_ZONE.put((short) 470, "America/Vancouver");
        SHORT_TO_ZONE.put((short) 471, "America/Virgin");
        SHORT_TO_ZONE.put((short) 472, "America/Whitehorse");
        SHORT_TO_ZONE.put((short) 473, "America/Winnipeg");
        SHORT_TO_ZONE.put((short) 474, "America/Yakutat");
        SHORT_TO_ZONE.put((short) 475, "America/Yellowknife");
        SHORT_TO_ZONE.put((short) 476, "Antarctica/Casey");
        SHORT_TO_ZONE.put((short) 477, "Antarctica/Davis");
        SHORT_TO_ZONE.put((short) 478, "Antarctica/DumontDUrville");
        SHORT_TO_ZONE.put((short) 479, "Antarctica/Macquarie");
        SHORT_TO_ZONE.put((short) 480, "Antarctica/Mawson");
        SHORT_TO_ZONE.put((short) 481, "Antarctica/McMurdo");
        SHORT_TO_ZONE.put((short) 482, "Antarctica/Palmer");
        SHORT_TO_ZONE.put((short) 483, "Antarctica/Rothera");
        SHORT_TO_ZONE.put((short) 484, "Antarctica/South_Pole");
        SHORT_TO_ZONE.put((short) 485, "Antarctica/Syowa");
        SHORT_TO_ZONE.put((short) 486, "Antarctica/Troll");
        SHORT_TO_ZONE.put((short) 487, "Antarctica/Vostok");
        SHORT_TO_ZONE.put((short) 488, "Arctic/Longyearbyen");
        SHORT_TO_ZONE.put((short) 489, "Asia/Aden");
        SHORT_TO_ZONE.put((short) 490, "Asia/Almaty");
        SHORT_TO_ZONE.put((short) 491, "Asia/Amman");
        SHORT_TO_ZONE.put((short) 492, "Asia/Anadyr");
        SHORT_TO_ZONE.put((short) 493, "Asia/Aqtau");
        SHORT_TO_ZONE.put((short) 494, "Asia/Aqtobe");
        SHORT_TO_ZONE.put((short) 495, "Asia/Ashgabat");
        SHORT_TO_ZONE.put((short) 496, "Asia/Ashkhabad");
        SHORT_TO_ZONE.put((short) 497, "Asia/Atyrau");
        SHORT_TO_ZONE.put((short) 498, "Asia/Baghdad");
        SHORT_TO_ZONE.put((short) 499, "Asia/Bahrain");
        SHORT_TO_ZONE.put((short) 500, "Asia/Baku");
        SHORT_TO_ZONE.put((short) 501, "Asia/Bangkok");
        SHORT_TO_ZONE.put((short) 502, "Asia/Barnaul");
        SHORT_TO_ZONE.put((short) 503, "Asia/Beirut");
        SHORT_TO_ZONE.put((short) 504, "Asia/Bishkek");
        SHORT_TO_ZONE.put((short) 505, "Asia/Brunei");
        SHORT_TO_ZONE.put((short) 506, "Asia/Calcutta");
        SHORT_TO_ZONE.put((short) 507, "Asia/Chita");
        SHORT_TO_ZONE.put((short) 508, "Asia/Choibalsan");
        SHORT_TO_ZONE.put((short) 509, "Asia/Chongqing");
        SHORT_TO_ZONE.put((short) 510, "Asia/Chungking");
        SHORT_TO_ZONE.put((short) 511, "Asia/Colombo");
        SHORT_TO_ZONE.put((short) 512, "Asia/Dacca");
        SHORT_TO_ZONE.put((short) 513, "Asia/Damascus");
        SHORT_TO_ZONE.put((short) 514, "Asia/Dhaka");
        SHORT_TO_ZONE.put((short) 515, "Asia/Dili");
        SHORT_TO_ZONE.put((short) 516, "Asia/Dubai");
        SHORT_TO_ZONE.put((short) 517, "Asia/Dushanbe");
        SHORT_TO_ZONE.put((short) 518, "Asia/Famagusta");
        SHORT_TO_ZONE.put((short) 519, "Asia/Gaza");
        SHORT_TO_ZONE.put((short) 520, "Asia/Harbin");
        SHORT_TO_ZONE.put((short) 521, "Asia/Hebron");
        SHORT_TO_ZONE.put((short) 522, "Asia/Ho_Chi_Minh");
        SHORT_TO_ZONE.put((short) 523, "Asia/Hong_Kong");
        SHORT_TO_ZONE.put((short) 524, "Asia/Hovd");
        SHORT_TO_ZONE.put((short) 525, "Asia/Irkutsk");
        SHORT_TO_ZONE.put((short) 526, "Asia/Istanbul");
        SHORT_TO_ZONE.put((short) 527, "Asia/Jakarta");
        SHORT_TO_ZONE.put((short) 528, "Asia/Jayapura");
        SHORT_TO_ZONE.put((short) 529, "Asia/Jerusalem");
        SHORT_TO_ZONE.put((short) 530, "Asia/Kabul");
        SHORT_TO_ZONE.put((short) 531, "Asia/Kamchatka");
        SHORT_TO_ZONE.put((short) 532, "Asia/Karachi");
        SHORT_TO_ZONE.put((short) 533, "Asia/Kashgar");
        SHORT_TO_ZONE.put((short) 534, "Asia/Kathmandu");
        SHORT_TO_ZONE.put((short) 535, "Asia/Katmandu");
        SHORT_TO_ZONE.put((short) 536, "Asia/Khandyga");
        SHORT_TO_ZONE.put((short) 537, "Asia/Kolkata");
        SHORT_TO_ZONE.put((short) 538, "Asia/Krasnoyarsk");
        SHORT_TO_ZONE.put((short) 539, "Asia/Kuala_Lumpur");
        SHORT_TO_ZONE.put((short) 540, "Asia/Kuching");
        SHORT_TO_ZONE.put((short) 541, "Asia/Kuwait");
        SHORT_TO_ZONE.put((short) 542, "Asia/Macao");
        SHORT_TO_ZONE.put((short) 543, "Asia/Macau");
        SHORT_TO_ZONE.put((short) 544, "Asia/Magadan");
        SHORT_TO_ZONE.put((short) 545, "Asia/Makassar");
        SHORT_TO_ZONE.put((short) 546, "Asia/Manila");
        SHORT_TO_ZONE.put((short) 547, "Asia/Muscat");
        SHORT_TO_ZONE.put((short) 548, "Asia/Nicosia");
        SHORT_TO_ZONE.put((short) 549, "Asia/Novokuznetsk");
        SHORT_TO_ZONE.put((short) 550, "Asia/Novosibirsk");
        SHORT_TO_ZONE.put((short) 551, "Asia/Omsk");
        SHORT_TO_ZONE.put((short) 552, "Asia/Oral");
        SHORT_TO_ZONE.put((short) 553, "Asia/Phnom_Penh");
        SHORT_TO_ZONE.put((short) 554, "Asia/Pontianak");
        SHORT_TO_ZONE.put((short) 555, "Asia/Pyongyang");
        SHORT_TO_ZONE.put((short) 556, "Asia/Qatar");
        SHORT_TO_ZONE.put((short) 557, "Asia/Qyzylorda");
        SHORT_TO_ZONE.put((short) 558, "Asia/Rangoon");
        SHORT_TO_ZONE.put((short) 559, "Asia/Riyadh");
        SHORT_TO_ZONE.put((short) 560, "Asia/Saigon");
        SHORT_TO_ZONE.put((short) 561, "Asia/Sakhalin");
        SHORT_TO_ZONE.put((short) 562, "Asia/Samarkand");
        SHORT_TO_ZONE.put((short) 563, "Asia/Seoul");
        SHORT_TO_ZONE.put((short) 564, "Asia/Shanghai");
        SHORT_TO_ZONE.put((short) 565, "Asia/Singapore");
        SHORT_TO_ZONE.put((short) 566, "Asia/Srednekolymsk");
        SHORT_TO_ZONE.put((short) 567, "Asia/Taipei");
        SHORT_TO_ZONE.put((short) 568, "Asia/Tashkent");
        SHORT_TO_ZONE.put((short) 569, "Asia/Tbilisi");
        SHORT_TO_ZONE.put((short) 570, "Asia/Tehran");
        SHORT_TO_ZONE.put((short) 571, "Asia/Tel_Aviv");
        SHORT_TO_ZONE.put((short) 572, "Asia/Thimbu");
        SHORT_TO_ZONE.put((short) 573, "Asia/Thimphu");
        SHORT_TO_ZONE.put((short) 574, "Asia/Tokyo");
        SHORT_TO_ZONE.put((short) 575, "Asia/Tomsk");
        SHORT_TO_ZONE.put((short) 576, "Asia/Ujung_Pandang");
        SHORT_TO_ZONE.put((short) 577, "Asia/Ulaanbaatar");
        SHORT_TO_ZONE.put((short) 578, "Asia/Ulan_Bator");
        SHORT_TO_ZONE.put((short) 579, "Asia/Urumqi");
        SHORT_TO_ZONE.put((short) 580, "Asia/Ust-Nera");
        SHORT_TO_ZONE.put((short) 581, "Asia/Vientiane");
        SHORT_TO_ZONE.put((short) 582, "Asia/Vladivostok");
        SHORT_TO_ZONE.put((short) 583, "Asia/Yakutsk");
        SHORT_TO_ZONE.put((short) 584, "Asia/Yangon");
        SHORT_TO_ZONE.put((short) 585, "Asia/Yekaterinburg");
        SHORT_TO_ZONE.put((short) 586, "Asia/Yerevan");
        SHORT_TO_ZONE.put((short) 587, "Atlantic/Azores");
        SHORT_TO_ZONE.put((short) 588, "Atlantic/Bermuda");
        SHORT_TO_ZONE.put((short) 589, "Atlantic/Canary");
        SHORT_TO_ZONE.put((short) 590, "Atlantic/Cape_Verde");
        SHORT_TO_ZONE.put((short) 591, "Atlantic/Faeroe");
        SHORT_TO_ZONE.put((short) 592, "Atlantic/Faroe");
        SHORT_TO_ZONE.put((short) 593, "Atlantic/Jan_Mayen");
        SHORT_TO_ZONE.put((short) 594, "Atlantic/Madeira");
        SHORT_TO_ZONE.put((short) 595, "Atlantic/Reykjavik");
        SHORT_TO_ZONE.put((short) 596, "Atlantic/South_Georgia");
        SHORT_TO_ZONE.put((short) 597, "Atlantic/St_Helena");
        SHORT_TO_ZONE.put((short) 598, "Atlantic/Stanley");
        SHORT_TO_ZONE.put((short) 599, "Australia/ACT");
        SHORT_TO_ZONE.put((short) 600, "Australia/Adelaide");
        SHORT_TO_ZONE.put((short) 601, "Australia/Brisbane");
        SHORT_TO_ZONE.put((short) 602, "Australia/Broken_Hill");
        SHORT_TO_ZONE.put((short) 603, "Australia/Canberra");
        SHORT_TO_ZONE.put((short) 604, "Australia/Currie");
        SHORT_TO_ZONE.put((short) 605, "Australia/Darwin");
        SHORT_TO_ZONE.put((short) 606, "Australia/Eucla");
        SHORT_TO_ZONE.put((short) 607, "Australia/Hobart");
        SHORT_TO_ZONE.put((short) 608, "Australia/LHI");
        SHORT_TO_ZONE.put((short) 609, "Australia/Lindeman");
        SHORT_TO_ZONE.put((short) 610, "Australia/Lord_Howe");
        SHORT_TO_ZONE.put((short) 611, "Australia/Melbourne");
        SHORT_TO_ZONE.put((short) 612, "Australia/NSW");
        SHORT_TO_ZONE.put((short) 613, "Australia/North");
        SHORT_TO_ZONE.put((short) 614, "Australia/Perth");
        SHORT_TO_ZONE.put((short) 615, "Australia/Queensland");
        SHORT_TO_ZONE.put((short) 616, "Australia/South");
        SHORT_TO_ZONE.put((short) 617, "Australia/Sydney");
        SHORT_TO_ZONE.put((short) 618, "Australia/Tasmania");
        SHORT_TO_ZONE.put((short) 619, "Australia/Victoria");
        SHORT_TO_ZONE.put((short) 620, "Australia/West");
        SHORT_TO_ZONE.put((short) 621, "Australia/Yancowinna");
        SHORT_TO_ZONE.put((short) 622, "Brazil/Acre");
        SHORT_TO_ZONE.put((short) 623, "Brazil/DeNoronha");
        SHORT_TO_ZONE.put((short) 624, "Brazil/East");
        SHORT_TO_ZONE.put((short) 625, "Brazil/West");
        SHORT_TO_ZONE.put((short) 626, "CET");
        SHORT_TO_ZONE.put((short) 627, "CST6CDT");
        SHORT_TO_ZONE.put((short) 628, "Canada/Atlantic");
        SHORT_TO_ZONE.put((short) 629, "Canada/Central");
        SHORT_TO_ZONE.put((short) 630, "Canada/Eastern");
        SHORT_TO_ZONE.put((short) 631, "Canada/Mountain");
        SHORT_TO_ZONE.put((short) 632, "Canada/Newfoundland");
        SHORT_TO_ZONE.put((short) 633, "Canada/Pacific");
        SHORT_TO_ZONE.put((short) 634, "Canada/Saskatchewan");
        SHORT_TO_ZONE.put((short) 635, "Canada/Yukon");
        SHORT_TO_ZONE.put((short) 636, "Chile/Continental");
        SHORT_TO_ZONE.put((short) 637, "Chile/EasterIsland");
        SHORT_TO_ZONE.put((short) 638, "Cuba");
        SHORT_TO_ZONE.put((short) 639, "EET");
        SHORT_TO_ZONE.put((short) 640, "EST5EDT");
        SHORT_TO_ZONE.put((short) 641, "Egypt");
        SHORT_TO_ZONE.put((short) 642, "Eire");
        SHORT_TO_ZONE.put((short) 643, "Etc/GMT");
        SHORT_TO_ZONE.put((short) 644, "Etc/GMT+0");
        SHORT_TO_ZONE.put((short) 645, "Etc/GMT+1");
        SHORT_TO_ZONE.put((short) 646, "Etc/GMT+10");
        SHORT_TO_ZONE.put((short) 647, "Etc/GMT+11");
        SHORT_TO_ZONE.put((short) 648, "Etc/GMT+12");
        SHORT_TO_ZONE.put((short) 649, "Etc/GMT+2");
        SHORT_TO_ZONE.put((short) 650, "Etc/GMT+3");
        SHORT_TO_ZONE.put((short) 651, "Etc/GMT+4");
        SHORT_TO_ZONE.put((short) 652, "Etc/GMT+5");
        SHORT_TO_ZONE.put((short) 653, "Etc/GMT+6");
        SHORT_TO_ZONE.put((short) 654, "Etc/GMT+7");
        SHORT_TO_ZONE.put((short) 655, "Etc/GMT+8");
        SHORT_TO_ZONE.put((short) 656, "Etc/GMT+9");
        SHORT_TO_ZONE.put((short) 657, "Etc/GMT-0");
        SHORT_TO_ZONE.put((short) 658, "Etc/GMT-1");
        SHORT_TO_ZONE.put((short) 659, "Etc/GMT-10");
        SHORT_TO_ZONE.put((short) 660, "Etc/GMT-11");
        SHORT_TO_ZONE.put((short) 661, "Etc/GMT-12");
        SHORT_TO_ZONE.put((short) 662, "Etc/GMT-13");
        SHORT_TO_ZONE.put((short) 663, "Etc/GMT-14");
        SHORT_TO_ZONE.put((short) 664, "Etc/GMT-2");
        SHORT_TO_ZONE.put((short) 665, "Etc/GMT-3");
        SHORT_TO_ZONE.put((short) 666, "Etc/GMT-4");
        SHORT_TO_ZONE.put((short) 667, "Etc/GMT-5");
        SHORT_TO_ZONE.put((short) 668, "Etc/GMT-6");
        SHORT_TO_ZONE.put((short) 669, "Etc/GMT-7");
        SHORT_TO_ZONE.put((short) 670, "Etc/GMT-8");
        SHORT_TO_ZONE.put((short) 671, "Etc/GMT-9");
        SHORT_TO_ZONE.put((short) 672, "Etc/GMT0");
        SHORT_TO_ZONE.put((short) 673, "Etc/Greenwich");
        SHORT_TO_ZONE.put((short) 674, "Etc/UCT");
        SHORT_TO_ZONE.put((short) 675, "Etc/UTC");
        SHORT_TO_ZONE.put((short) 676, "Etc/Universal");
        SHORT_TO_ZONE.put((short) 677, "Etc/Zulu");
        SHORT_TO_ZONE.put((short) 678, "Europe/Amsterdam");
        SHORT_TO_ZONE.put((short) 679, "Europe/Andorra");
        SHORT_TO_ZONE.put((short) 680, "Europe/Astrakhan");
        SHORT_TO_ZONE.put((short) 681, "Europe/Athens");
        SHORT_TO_ZONE.put((short) 682, "Europe/Belfast");
        SHORT_TO_ZONE.put((short) 683, "Europe/Belgrade");
        SHORT_TO_ZONE.put((short) 684, "Europe/Berlin");
        SHORT_TO_ZONE.put((short) 685, "Europe/Bratislava");
        SHORT_TO_ZONE.put((short) 686, "Europe/Brussels");
        SHORT_TO_ZONE.put((short) 687, "Europe/Bucharest");
        SHORT_TO_ZONE.put((short) 688, "Europe/Budapest");
        SHORT_TO_ZONE.put((short) 689, "Europe/Busingen");
        SHORT_TO_ZONE.put((short) 690, "Europe/Chisinau");
        SHORT_TO_ZONE.put((short) 691, "Europe/Copenhagen");
        SHORT_TO_ZONE.put((short) 692, "Europe/Dublin");
        SHORT_TO_ZONE.put((short) 693, "Europe/Gibraltar");
        SHORT_TO_ZONE.put((short) 694, "Europe/Guernsey");
        SHORT_TO_ZONE.put((short) 695, "Europe/Helsinki");
        SHORT_TO_ZONE.put((short) 696, "Europe/Isle_of_Man");
        SHORT_TO_ZONE.put((short) 697, "Europe/Istanbul");
        SHORT_TO_ZONE.put((short) 698, "Europe/Jersey");
        SHORT_TO_ZONE.put((short) 699, "Europe/Kaliningrad");
        SHORT_TO_ZONE.put((short) 700, "Europe/Kiev");
        SHORT_TO_ZONE.put((short) 701, "Europe/Kirov");
        SHORT_TO_ZONE.put((short) 702, "Europe/Lisbon");
        SHORT_TO_ZONE.put((short) 703, "Europe/Ljubljana");
        SHORT_TO_ZONE.put((short) 704, "Europe/London");
        SHORT_TO_ZONE.put((short) 705, "Europe/Luxembourg");
        SHORT_TO_ZONE.put((short) 706, "Europe/Madrid");
        SHORT_TO_ZONE.put((short) 707, "Europe/Malta");
        SHORT_TO_ZONE.put((short) 708, "Europe/Mariehamn");
        SHORT_TO_ZONE.put((short) 709, "Europe/Minsk");
        SHORT_TO_ZONE.put((short) 710, "Europe/Monaco");
        SHORT_TO_ZONE.put((short) 711, "Europe/Moscow");
        SHORT_TO_ZONE.put((short) 712, "Europe/Nicosia");
        SHORT_TO_ZONE.put((short) 713, "Europe/Oslo");
        SHORT_TO_ZONE.put((short) 714, "Europe/Paris");
        SHORT_TO_ZONE.put((short) 715, "Europe/Podgorica");
        SHORT_TO_ZONE.put((short) 716, "Europe/Prague");
        SHORT_TO_ZONE.put((short) 717, "Europe/Riga");
        SHORT_TO_ZONE.put((short) 718, "Europe/Rome");
        SHORT_TO_ZONE.put((short) 719, "Europe/Samara");
        SHORT_TO_ZONE.put((short) 720, "Europe/San_Marino");
        SHORT_TO_ZONE.put((short) 721, "Europe/Sarajevo");
        SHORT_TO_ZONE.put((short) 722, "Europe/Saratov");
        SHORT_TO_ZONE.put((short) 723, "Europe/Simferopol");
        SHORT_TO_ZONE.put((short) 724, "Europe/Skopje");
        SHORT_TO_ZONE.put((short) 725, "Europe/Sofia");
        SHORT_TO_ZONE.put((short) 726, "Europe/Stockholm");
        SHORT_TO_ZONE.put((short) 727, "Europe/Tallinn");
        SHORT_TO_ZONE.put((short) 728, "Europe/Tirane");
        SHORT_TO_ZONE.put((short) 729, "Europe/Tiraspol");
        SHORT_TO_ZONE.put((short) 730, "Europe/Ulyanovsk");
        SHORT_TO_ZONE.put((short) 731, "Europe/Uzhgorod");
        SHORT_TO_ZONE.put((short) 732, "Europe/Vaduz");
        SHORT_TO_ZONE.put((short) 733, "Europe/Vatican");
        SHORT_TO_ZONE.put((short) 734, "Europe/Vienna");
        SHORT_TO_ZONE.put((short) 735, "Europe/Vilnius");
        SHORT_TO_ZONE.put((short) 736, "Europe/Volgograd");
        SHORT_TO_ZONE.put((short) 737, "Europe/Warsaw");
        SHORT_TO_ZONE.put((short) 738, "Europe/Zagreb");
        SHORT_TO_ZONE.put((short) 739, "Europe/Zaporozhye");
        SHORT_TO_ZONE.put((short) 740, "Europe/Zurich");
        SHORT_TO_ZONE.put((short) 741, "GB");
        SHORT_TO_ZONE.put((short) 742, "GB-Eire");
        SHORT_TO_ZONE.put((short) 743, "GMT");
        SHORT_TO_ZONE.put((short) 744, "GMT0");
        SHORT_TO_ZONE.put((short) 745, "Greenwich");
        SHORT_TO_ZONE.put((short) 746, "Hongkong");
        SHORT_TO_ZONE.put((short) 747, "Iceland");
        SHORT_TO_ZONE.put((short) 748, "Indian/Antananarivo");
        SHORT_TO_ZONE.put((short) 749, "Indian/Chagos");
        SHORT_TO_ZONE.put((short) 750, "Indian/Christmas");
        SHORT_TO_ZONE.put((short) 751, "Indian/Cocos");
        SHORT_TO_ZONE.put((short) 752, "Indian/Comoro");
        SHORT_TO_ZONE.put((short) 753, "Indian/Kerguelen");
        SHORT_TO_ZONE.put((short) 754, "Indian/Mahe");
        SHORT_TO_ZONE.put((short) 755, "Indian/Maldives");
        SHORT_TO_ZONE.put((short) 756, "Indian/Mauritius");
        SHORT_TO_ZONE.put((short) 757, "Indian/Mayotte");
        SHORT_TO_ZONE.put((short) 758, "Indian/Reunion");
        SHORT_TO_ZONE.put((short) 759, "Iran");
        SHORT_TO_ZONE.put((short) 760, "Israel");
        SHORT_TO_ZONE.put((short) 761, "Jamaica");
        SHORT_TO_ZONE.put((short) 762, "Japan");
        SHORT_TO_ZONE.put((short) 763, "Kwajalein");
        SHORT_TO_ZONE.put((short) 764, "Libya");
        SHORT_TO_ZONE.put((short) 765, "MET");
        SHORT_TO_ZONE.put((short) 766, "MST7MDT");
        SHORT_TO_ZONE.put((short) 767, "Mexico/BajaNorte");
        SHORT_TO_ZONE.put((short) 768, "Mexico/BajaSur");
        SHORT_TO_ZONE.put((short) 769, "Mexico/General");
        SHORT_TO_ZONE.put((short) 770, "NZ");
        SHORT_TO_ZONE.put((short) 771, "NZ-CHAT");
        SHORT_TO_ZONE.put((short) 772, "Navajo");
        SHORT_TO_ZONE.put((short) 773, "PRC");
        SHORT_TO_ZONE.put((short) 774, "PST8PDT");
        SHORT_TO_ZONE.put((short) 775, "Pacific/Apia");
        SHORT_TO_ZONE.put((short) 776, "Pacific/Auckland");
        SHORT_TO_ZONE.put((short) 777, "Pacific/Bougainville");
        SHORT_TO_ZONE.put((short) 778, "Pacific/Chatham");
        SHORT_TO_ZONE.put((short) 779, "Pacific/Chuuk");
        SHORT_TO_ZONE.put((short) 780, "Pacific/Easter");
        SHORT_TO_ZONE.put((short) 781, "Pacific/Efate");
        SHORT_TO_ZONE.put((short) 782, "Pacific/Enderbury");
        SHORT_TO_ZONE.put((short) 783, "Pacific/Fakaofo");
        SHORT_TO_ZONE.put((short) 784, "Pacific/Fiji");
        SHORT_TO_ZONE.put((short) 785, "Pacific/Funafuti");
        SHORT_TO_ZONE.put((short) 786, "Pacific/Galapagos");
        SHORT_TO_ZONE.put((short) 787, "Pacific/Gambier");
        SHORT_TO_ZONE.put((short) 788, "Pacific/Guadalcanal");
        SHORT_TO_ZONE.put((short) 789, "Pacific/Guam");
        SHORT_TO_ZONE.put((short) 790, "Pacific/Honolulu");
        SHORT_TO_ZONE.put((short) 791, "Pacific/Johnston");
        SHORT_TO_ZONE.put((short) 792, "Pacific/Kiritimati");
        SHORT_TO_ZONE.put((short) 793, "Pacific/Kosrae");
        SHORT_TO_ZONE.put((short) 794, "Pacific/Kwajalein");
        SHORT_TO_ZONE.put((short) 795, "Pacific/Majuro");
        SHORT_TO_ZONE.put((short) 796, "Pacific/Marquesas");
        SHORT_TO_ZONE.put((short) 797, "Pacific/Midway");
        SHORT_TO_ZONE.put((short) 798, "Pacific/Nauru");
        SHORT_TO_ZONE.put((short) 799, "Pacific/Niue");
        SHORT_TO_ZONE.put((short) 800, "Pacific/Norfolk");
        SHORT_TO_ZONE.put((short) 801, "Pacific/Noumea");
        SHORT_TO_ZONE.put((short) 802, "Pacific/Pago_Pago");
        SHORT_TO_ZONE.put((short) 803, "Pacific/Palau");
        SHORT_TO_ZONE.put((short) 804, "Pacific/Pitcairn");
        SHORT_TO_ZONE.put((short) 805, "Pacific/Pohnpei");
        SHORT_TO_ZONE.put((short) 806, "Pacific/Ponape");
        SHORT_TO_ZONE.put((short) 807, "Pacific/Port_Moresby");
        SHORT_TO_ZONE.put((short) 808, "Pacific/Rarotonga");
        SHORT_TO_ZONE.put((short) 809, "Pacific/Saipan");
        SHORT_TO_ZONE.put((short) 810, "Pacific/Samoa");
        SHORT_TO_ZONE.put((short) 811, "Pacific/Tahiti");
        SHORT_TO_ZONE.put((short) 812, "Pacific/Tarawa");
        SHORT_TO_ZONE.put((short) 813, "Pacific/Tongatapu");
        SHORT_TO_ZONE.put((short) 814, "Pacific/Truk");
        SHORT_TO_ZONE.put((short) 815, "Pacific/Wake");
        SHORT_TO_ZONE.put((short) 816, "Pacific/Wallis");
        SHORT_TO_ZONE.put((short) 817, "Pacific/Yap");
        SHORT_TO_ZONE.put((short) 818, "Poland");
        SHORT_TO_ZONE.put((short) 819, "Portugal");
        SHORT_TO_ZONE.put((short) 820, "ROK");
        SHORT_TO_ZONE.put((short) 821, "Singapore");
        SHORT_TO_ZONE.put((short) 822, "SystemV/AST4");
        SHORT_TO_ZONE.put((short) 823, "SystemV/AST4ADT");
        SHORT_TO_ZONE.put((short) 824, "SystemV/CST6");
        SHORT_TO_ZONE.put((short) 825, "SystemV/CST6CDT");
        SHORT_TO_ZONE.put((short) 826, "SystemV/EST5");
        SHORT_TO_ZONE.put((short) 827, "SystemV/EST5EDT");
        SHORT_TO_ZONE.put((short) 828, "SystemV/HST10");
        SHORT_TO_ZONE.put((short) 829, "SystemV/MST7");
        SHORT_TO_ZONE.put((short) 830, "SystemV/MST7MDT");
        SHORT_TO_ZONE.put((short) 831, "SystemV/PST8");
        SHORT_TO_ZONE.put((short) 832, "SystemV/PST8PDT");
        SHORT_TO_ZONE.put((short) 833, "SystemV/YST9");
        SHORT_TO_ZONE.put((short) 834, "SystemV/YST9YDT");
        SHORT_TO_ZONE.put((short) 835, "Turkey");
        SHORT_TO_ZONE.put((short) 836, "UCT");
        SHORT_TO_ZONE.put((short) 837, "US/Alaska");
        SHORT_TO_ZONE.put((short) 838, "US/Aleutian");
        SHORT_TO_ZONE.put((short) 839, "US/Arizona");
        SHORT_TO_ZONE.put((short) 840, "US/Central");
        SHORT_TO_ZONE.put((short) 841, "US/East-Indiana");
        SHORT_TO_ZONE.put((short) 842, "US/Eastern");
        SHORT_TO_ZONE.put((short) 843, "US/Hawaii");
        SHORT_TO_ZONE.put((short) 844, "US/Indiana-Starke");
        SHORT_TO_ZONE.put((short) 845, "US/Michigan");
        SHORT_TO_ZONE.put((short) 846, "US/Mountain");
        SHORT_TO_ZONE.put((short) 847, "US/Pacific");
        SHORT_TO_ZONE.put((short) 848, "US/Pacific-New");
        SHORT_TO_ZONE.put((short) 849, "US/Samoa");
        SHORT_TO_ZONE.put((short) 850, "UTC");
        SHORT_TO_ZONE.put((short) 851, "Universal");
        SHORT_TO_ZONE.put((short) 852, "W-SU");
        SHORT_TO_ZONE.put((short) 853, "WET");
        SHORT_TO_ZONE.put((short) 854, "Zulu");
    }
}
