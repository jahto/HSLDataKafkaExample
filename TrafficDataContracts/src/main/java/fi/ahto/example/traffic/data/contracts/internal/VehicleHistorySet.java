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
import java.util.Comparator;
import java.util.TreeSet;

/**
 *
 * @author Jouni Ahto
 */
public class VehicleHistorySet extends TreeSet<VehicleHistoryRecord> implements Serializable {
    private static final long serialVersionUID = 4892554282933714559L;

    public VehicleHistorySet() {
        // super((Comparator<VehicleHistoryRecord> & Serializable) (VehicleHistoryRecord o1, VehicleHistoryRecord o2) -> o1.getRecordTime().compareTo(o2.getRecordTime()));
        super(new VehicleHistorySetComparator());
    }
}
