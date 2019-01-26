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
package fi.ahto.example.traffic.data.gtfs.feeder;

import java.util.Comparator;
import java.util.TreeSet;

/**
 *
 * @author Jouni Ahto
 */

public class StopTimeExtSet extends TreeSet<StopTimeExt> {
    public StopTimeExtSet() {
        super((Comparator<StopTimeExt>) (StopTimeExt o1, StopTimeExt o2) -> Integer.compare(o1.getStopSequence(), o2.getStopSequence()));
    }
}
