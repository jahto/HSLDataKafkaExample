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
package fi.ahto.example.traffic.data.contracts.internal.tests;

import fi.ahto.example.traffic.data.contracts.internal.GTFSLocalTime;
import fi.ahto.example.traffic.data.contracts.internal.RouteTypeExtended;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Jouni Ahto
 */

public class SimpleTests {
    @Test
    public void test_From() {
        RouteTypeExtended rt = RouteTypeExtended.from(3);
        assertThat(rt, is(RouteTypeExtended.BUS_SERVICE));
        rt = RouteTypeExtended.from(800);
        assertThat(rt, is(RouteTypeExtended.TROLLEYBUS_SERVICE));
        rt = RouteTypeExtended.from(-3);
        assertThat(rt, is(RouteTypeExtended.UNKNOWN));
        rt = RouteTypeExtended.from(9);
        assertThat(rt, is(RouteTypeExtended.UNKNOWN));
        rt = RouteTypeExtended.from(813);
        assertThat(rt, is(RouteTypeExtended.UNKNOWN));
    }
    
    @Test
    public void test_mainCategory() {
        RouteTypeExtended rt = RouteTypeExtended.mainCategory(RouteTypeExtended.RACK_AND_PINION_RAILWAY);
        assertThat(rt, is(RouteTypeExtended.RAILWAY_SERVICE));
        rt = RouteTypeExtended.mainCategory(RouteTypeExtended.TROLLEYBUS_SERVICE);
        assertThat(rt, is(RouteTypeExtended.BUS_SERVICE));
        rt = RouteTypeExtended.mainCategory(RouteTypeExtended.UNKNOWN);
        assertThat(rt, is(RouteTypeExtended.UNKNOWN));
    }

    @Test
    public void GTFSLocalTimeCompareTo() {
        // Write the test cases and asserts...
    }

    @Test
    public void GTFSLocalTimeValidStringFormats() {
        // Write the test cases and asserts...
        GTFSLocalTime t = GTFSLocalTime.parse("22:22");
        t = GTFSLocalTime.parse("2:22:22");
        t = GTFSLocalTime.parse("50:22:22");
        
        String str = t.toString();
        
        int i = 0;
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void GTFSLocalTimeInValidValueMinutes() {
        GTFSLocalTime t = GTFSLocalTime.parse("22:77");
    }

    @Test(expected = IllegalArgumentException.class)
    public void GTFSLocalTimeInValidValueSeconds() {
        GTFSLocalTime t = GTFSLocalTime.parse("22:33:77");
    
    }
    
    /*
    @Test(expected = IllegalArgumentException.class)
    public void GTFSLocalTimeInValidFormat() {
        // Write the test cases and asserts...
    }
    */
}
