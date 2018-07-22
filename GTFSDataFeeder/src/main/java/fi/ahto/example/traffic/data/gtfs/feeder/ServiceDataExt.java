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

import fi.ahto.example.traffic.data.contracts.internal.ServiceData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jouni Ahto
 */
public class ServiceDataExt extends ServiceData {
    public List<String> blockIds = new ArrayList<>();
    public List<String> routeIds = new ArrayList<>();

    ServiceData toServiceData() {
        ServiceData sd = new ServiceData();
        sd.inuse = this.inuse;
        sd.notinuse = this.notinuse;
        sd.serviceId = this.serviceId;
        sd.validfrom = this.validfrom;
        sd.validuntil = this.validuntil;
        sd.weekdays = this.weekdays;
        sd.extra = this.extra;
        return sd;
    }
}
