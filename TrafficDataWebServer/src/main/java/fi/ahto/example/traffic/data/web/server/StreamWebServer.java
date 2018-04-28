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
package fi.ahto.example.traffic.data.web.server;

import fi.ahto.example.traffic.data.contracts.internal.RouteData;
import fi.ahto.example.traffic.data.contracts.internal.ShapeSet;
import fi.ahto.example.traffic.data.contracts.internal.StopData;
import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import fi.ahto.example.traffic.data.contracts.internal.VehicleDataList;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
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
    private ReadOnlyKeyValueStore<String, VehicleActivity> vehicleDataStore;
    
    @Autowired
    @Lazy(true)
    private ReadOnlyKeyValueStore<String, VehicleDataList> vehicleHistoryDataStore;
    
    @Autowired
    @Lazy(true)
    private ReadOnlyKeyValueStore<String, RouteData> routeDataStore;
    
    @Autowired
    @Lazy(true)
    private ReadOnlyKeyValueStore<String, StopData> stopDataStore;
    
    @Autowired
    @Lazy(true)
    private ReadOnlyKeyValueStore<String, ShapeSet> shapeDataStore;
    
    @RequestMapping(value = "/line/{id}", method = RequestMethod.GET)
    public VehicleDataList findLine(@PathVariable String id) {
        return lineDataStore.get(id);
    }

    @RequestMapping(value = "/vehicle/{id}", method = RequestMethod.GET)
    public VehicleActivity findVehicle(@PathVariable String id) {
        return vehicleDataStore.get(id);
    }

    @RequestMapping(value = "/history/vehicle/{id}", method = RequestMethod.GET)
    public VehicleDataList findVehicleHistory(@PathVariable String id) {
        return vehicleHistoryDataStore.get(id);
    }

    @RequestMapping(value = "/route/{id}", method = RequestMethod.GET)
    public RouteData findRoute(@PathVariable String id) {
        return routeDataStore.get(id);
    }
    
    @RequestMapping(value = "/stop/{id}", method = RequestMethod.GET)
    public StopData findStop(@PathVariable String id) {
        return stopDataStore.get(id);
    }
    
    @RequestMapping(value = "/shape/{id}", method = RequestMethod.GET)
    public ShapeSet findShape(@PathVariable String id) {
        return shapeDataStore.get(id);
    }

    @RequestMapping(value = "/lines", method = RequestMethod.GET)
    public Map<String, VehicleDataList> findLinesAll() {
        Map<String, VehicleDataList> map = new HashMap<>();
        KeyValueIterator<String, VehicleDataList> iter = lineDataStore.all();
        while (iter.hasNext()) {
            KeyValue<String, VehicleDataList> next = iter.next();
            map.put(next.key, next.value);
        }
        return map;
    }

    @RequestMapping(value = "/vehicles", method = RequestMethod.GET)
    public Map<String, VehicleActivity> findVehicleAll() {
        Map<String, VehicleActivity> map = new HashMap<>();
        KeyValueIterator<String, VehicleActivity> iter = vehicleDataStore.all();
        while (iter.hasNext()) {
            KeyValue<String, VehicleActivity> next = iter.next();
            map.put(next.key, next.value);
        }
        return map;
    }

    @RequestMapping(value = "/history/vehicles", method = RequestMethod.GET)
    public Map<String, VehicleDataList> findVehicleHistoryAll() {
        Map<String, VehicleDataList> map = new HashMap<>();
        KeyValueIterator<String, VehicleDataList> iter = vehicleHistoryDataStore.all();
        while (iter.hasNext()) {
            KeyValue<String, VehicleDataList> next = iter.next();
            map.put(next.key, next.value);
        }
        return map;
    }

    @RequestMapping(value = "/routes", method = RequestMethod.GET)
    public Map<String, RouteData> findRouteAll() {
        Map<String, RouteData> map = new HashMap<>();
        KeyValueIterator<String, RouteData> iter = routeDataStore.all();
        while (iter.hasNext()) {
            KeyValue<String, RouteData> next = iter.next();
            map.put(next.key, next.value);
        }
        return map;
    }
    
    @RequestMapping(value = "/stops", method = RequestMethod.GET)
    public Map<String, StopData> findStopAll() {
        Map<String, StopData> map = new HashMap<>();
        KeyValueIterator<String, StopData> iter = stopDataStore.all();
        while (iter.hasNext()) {
            KeyValue<String, StopData> next = iter.next();
            map.put(next.key, next.value);
        }
        return map;
    }
    
    @RequestMapping(value = "/shapes", method = RequestMethod.GET)
    public Map<String, ShapeSet> findShapeAll(@PathVariable String id) {
        Map<String, ShapeSet> map = new HashMap<>();
        KeyValueIterator<String, ShapeSet> iter = shapeDataStore.all();
        while (iter.hasNext()) {
            KeyValue<String, ShapeSet> next = iter.next();
            map.put(next.key, next.value);
        }
        return map;
    }

}
