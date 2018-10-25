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
package com.github.jahto.utils.tests;

import com.github.jahto.utils.FSTSerde;
import com.github.jahto.utils.FSTSerializers.FSTGTFSLocalTimeSerializer;
import com.github.jahto.utils.FSTSerializers.TripStopSerializer;
import fi.ahto.example.traffic.data.contracts.internal.GTFSLocalTime;
import fi.ahto.example.traffic.data.contracts.internal.TripStop;
import java.time.ZoneId;
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
public class ProjectLocalTypesTest {

    @Test
    public void test_GTFSLocalTime() {
        final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.registerSerializer(GTFSLocalTime.class, new FSTGTFSLocalTimeSerializer(), false);
        conf.registerClass(GTFSLocalTime.class);
        FSTSerde<GTFSLocalTime> serde = new FSTSerde<>(GTFSLocalTime.class, conf);

        GTFSLocalTime t1 = GTFSLocalTime.parse("2:22:22");
        byte[] b1 = serde.serializer().serialize("", t1);
        GTFSLocalTime r1 = serde.deserializer().deserialize("", b1);
        assertThat(t1, is(r1));

        GTFSLocalTime t2 = GTFSLocalTime.parse("26:22:22");
        byte[] b2 = serde.serializer().serialize("", t2);
        GTFSLocalTime r2 = serde.deserializer().deserialize("", b2);
        assertThat(t2, is(r2));
    }

    @Test
    public void test_TripStop() {
        final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
        conf.registerSerializer(TripStop.class, new TripStopSerializer(), false);
        conf.registerClass(TripStop.class);
        FSTSerde<TripStop> serde = new FSTSerde<>(TripStop.class, conf);
        
        TripStop st = new TripStop();
        st.seq = 1;
        st.arrivalTime = GTFSLocalTime.parse("26:22:22");
        st.stopid = "PR:EF:IX:STOP1";
        
        byte[] b = serde.serializer().serialize("", st);
        TripStop res = serde.deserializer().deserialize("", b);
        assertThat(st, is(res));
    }
}
