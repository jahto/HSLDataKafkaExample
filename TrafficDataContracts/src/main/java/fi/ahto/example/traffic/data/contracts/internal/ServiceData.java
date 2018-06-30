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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jouni Ahto
 */
// Only for internal use, so we can safely use the most efficient form.
// @JsonFormat(shape=JsonFormat.Shape.ARRAY)
// @JsonPropertyOrder({"serviceId", "blockIds", "routeIds", "validfrom", "validuntil", "notinuse", "weekdays"})
public class ServiceData implements Serializable {
    private static final long serialVersionUID = 3738645659204806594L;

    public String serviceId;
    public List<String> blockIds = new ArrayList<>();
    public List<String> routeIds = new ArrayList<>();
    public LocalDate validfrom;
    public LocalDate validuntil;
    public List<LocalDate> notinuse = new ArrayList<>();
    public byte weekdays = 0;
}
