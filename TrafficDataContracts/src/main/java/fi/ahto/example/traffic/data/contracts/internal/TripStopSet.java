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

import java.io.Serializable;
import java.util.TreeSet;

/**
 *
 * @author Jouni Ahto
 */

public class TripStopSet extends TreeSet<TripStop> implements Serializable {
    private static final long serialVersionUID = 2289084775213919155L;

    public TripStopSet() {
        // super((Comparator<TripStop> & Serializable) (TripStop o1, TripStop o2) -> Integer.compare(o1.seq, o2.seq));
        super(new TripStopSetComparator());
    }
    
    public TripStopSet(TripStopSetComparator comp) {
        super(comp);
    }
}
