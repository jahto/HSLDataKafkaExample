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
package fi.ahto.example.hsl.data.web.server;

import fi.ahto.example.hsl.data.contracts.siri.VehicleDataList;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Jouni Ahto
 */
@RestController
public class StreamWebServer {
    @Autowired
    @Lazy(true)
    private ReadOnlyKeyValueStore<String, VehicleDataList> lineDataStore;
    
    @Autowired
    @Lazy(true)
    private ReadOnlyKeyValueStore<String, VehicleDataList> vehicleDataStore;
    
    @RequestMapping(value = "/line/{id}", method = RequestMethod.GET)
    public VehicleDataList findLine(@PathVariable String id) {
        return lineDataStore.get(id);
    }

    @RequestMapping(value = "/vehicle/{id}", method = RequestMethod.GET)
    public VehicleDataList findVehicle(@PathVariable String id) {
        return vehicleDataStore.get(id);
    }
}
