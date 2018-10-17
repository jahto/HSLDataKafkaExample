/*
 * Copyright 2018 the original author or authors.
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
package fi.ahto.example.traffic.data.contracts.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Jouni Ahto
 */
public class GTFSLocalTime implements Serializable, Comparable<GTFSLocalTime> {

    private static final long serialVersionUID = -704773418366604253L;

    private int secs;
    private LocalTime time;
    private int daysForward;

    private GTFSLocalTime() {
    }

    public static GTFSLocalTime ofCutOffAndZonedDateTime(LocalTime cutoff, ZonedDateTime time) {
        return new GTFSLocalTime(cutoff, time.toLocalDateTime().toLocalTime());
    }
    
    public static GTFSLocalTime ofZonedDateTime(ZonedDateTime time) {
        return new GTFSLocalTime(LocalTime.of(0, 0), time.toLocalDateTime().toLocalTime());
    }
    
    public static GTFSLocalTime ofLocalTime(LocalTime time) {
        return new GTFSLocalTime(LocalTime.of(0, 0), time);
    }
    
    public static GTFSLocalTime ofCutOffAndLocalTime(LocalTime cutoff, LocalTime time) {
        return new GTFSLocalTime(cutoff, time);
    }
    
    private GTFSLocalTime(LocalTime cutoff, LocalTime time) {
        Objects.requireNonNull(cutoff);
        Objects.requireNonNull(time);

        this.time = time;
        this.secs = time.toSecondOfDay();

        if (time.isBefore(cutoff)) {
            daysForward++;
            secs += 86400;
        }
    }

    public static GTFSLocalTime parse(String str) {
        return new GTFSLocalTime(str);
    }
    
    private GTFSLocalTime(String str) {
        Objects.requireNonNull(str);

        int hours = 0;
        int minutes = 0;
        int seconds = 0;

        // Write here all possible versions of possible string observed
        // in feeds, and also not observed just for completeness sake,
        // and convert to seconds.
        int localsecs = 0;
        Pattern p1 = Pattern.compile("^(\\d{2})[:.-]?(\\d{2})[:.-]?(\\d{2})?$");
        Matcher m1 = p1.matcher(str);

        Pattern p2 = Pattern.compile("^(\\d{1,2})[:-](\\d{1,2})([:-](\\d{1,2}))?$");
        Matcher m2 = p2.matcher(str);
        if (m1.matches()) {
            String hoursstr = m1.group(1);
            String minutesstr = m1.group(2);
            String secondsstr = m1.group(3);

            hours = Integer.parseInt(hoursstr);
            minutes = Integer.parseInt(minutesstr);
            if (secondsstr != null) {
                seconds = Integer.parseInt(secondsstr);
            }
        }
        else if (m2.matches()) {
            String hoursstr = m2.group(1);
            String minutesstr = m2.group(2);
            String secondsstr = m2.group(4);

            hours = Integer.parseInt(hoursstr);
            minutes = Integer.parseInt(minutesstr);
            if (secondsstr != null) {
                seconds = Integer.parseInt(secondsstr);
            }
        } else {
            throw new IllegalArgumentException(str + " is not a valid time format.");
        }
        
        // NOTE: hours can be over 23 in GTFS and means the next day,
        // so we dont't check for that one.
        if (minutes > 59 || seconds > 59) {
            throw new IllegalArgumentException(str + " is not a valid time format.");
        }

        localsecs = hours * 60 * 60 + minutes * 60 + seconds;
        convert(localsecs);
    }

    public static GTFSLocalTime ofSeconds(int secs) {
        return new GTFSLocalTime(secs);
    }
    
    private GTFSLocalTime(int secs) {
        convert(secs);
    }

    private void convert(int secs) {
        this.secs = secs;

        while (secs > 86399) {
            secs -= 86400;
            this.daysForward++;
        }

        this.time = LocalTime.ofSecondOfDay(secs);
    }
    
    public LocalTime getLocalTime() {
        return this.time;
    }
    
    public boolean isBefore(LocalTime time) {
        // TODO: this probably has a logic error... write some tests.
        return this.secs < time.toSecondOfDay();
    }

    public boolean isBefore(GTFSLocalTime time) {
        return this.secs < time.secs;
    }
    
    public boolean isAfter(GTFSLocalTime time) {
        return this.secs > time.secs;
    }
    
    public GTFSLocalTime plusSeconds(int secs) {
        return new GTFSLocalTime(this.secs + secs); 
    }
    
    @Override
    public int compareTo(GTFSLocalTime o) {
        return Integer.compare(this.toSecondOfDay(), o.toSecondOfDay());
    }

    public int toSecondOfDay() {
        return secs;
    }

    public int getDaysForward() {
        return daysForward;
    }

    // TODO: write these methods. We only need to read/write the seconds.
    /*
    private void readObject(ObjectInputStream inputStream) throws ClassNotFoundException, IOException {
    }

    private void writeObject(ObjectOutputStream outputStream) throws IOException {
    }
     */

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.secs;
        hash = 29 * hash + Objects.hashCode(this.time);
        hash = 29 * hash + this.daysForward;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GTFSLocalTime other = (GTFSLocalTime) obj;
        if (this.secs != other.secs) {
            return false;
        }
        if (this.daysForward != other.daysForward) {
            return false;
        }
        if (!Objects.equals(this.time, other.time)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "GTFSLocalTime{" + "secs=" + secs + ", time=" + time + ", daysForward=" + daysForward + '}';
    }
}
