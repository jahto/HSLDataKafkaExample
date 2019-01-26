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
package fi.ahto.example.traffic.data.database.repositories.sql;

import fi.ahto.example.traffic.data.contracts.database.sql.DBFrequency;
import fi.ahto.example.traffic.data.contracts.database.sql.DBStopTime;
import fi.ahto.example.traffic.data.contracts.database.sql.DBTrip;
import fi.ahto.example.traffic.data.contracts.internal.FrequencyData;
import fi.ahto.example.traffic.data.contracts.internal.StopTimeData;
import fi.ahto.example.traffic.data.contracts.internal.TripData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
/**
 *
 * @author Jouni Ahto
 */
@Service
@Repository
public class SQLTripManagementImpl implements SQLTripManagement {
    private static final Logger LOG = LoggerFactory.getLogger(SQLTripManagementImpl.class);
    public static ConcurrentHashMap<String, Long> tripnums = new ConcurrentHashMap<>();
    
    @Autowired
    private SQLTripRepository tripRepository;
    
    @Autowired
    private SQLFrequencyRepository frequencyRepository;
    
    @Autowired
    private SQLStopTimeRepository stopTimeRepository;
    
    @Autowired
    private SQLRouteManagement routeManagement;

    @Autowired
    private SQLStopManagement stopManagement;

    @Autowired
    private SQLCalendarManagement calendarManagement;

    public Long handleTrip(String key, TripData tr) {
        Long sid = calendarManagement.getServiceNumber(tr.getServiceId(), tr.getService());
        if (sid == null) {
            LOG.error("Shouldn't happen, num for service {} is still null.", tr.getServiceId());
            return null;
        }
        Long rid = routeManagement.getRouteNumber(tr.getRouteId(), tr.getRoute());
        if (rid == null) {
            LOG.error("Shouldn't happen, num for route {} is still null.", tr.getRouteId());
            return null;
        }

        // Only for side-effects. This makes it sure that later in handleStopTimes
        // the stop already exists both in database and it's key/num in local hash,
        // so adding it now happens outside transaction.
        for (StopTimeData st : tr.getStopTimes()) {
            Long stid = stopManagement.getStopNumber(st);
        }
        
        Long num = handleTripInternal(key, tr, sid, rid);
        if (num != null) {
            tripnums.put(key, num);
        }
        else {
            // Assuming there was a duplicate key error and we already have the key.
            num = tripnums.get(key);
        }
        return num;
    }
    
    @Transactional
    private Long handleTripInternal(String key, TripData tr, Long sid, Long rid) {
        LOG.info("Handling trip {}", tr.getTripId());
        DBTrip dbrt = new DBTrip(sid, rid, tr);
        Optional<Long> res = tripRepository.findIdByTripId(tr.getTripId());
        if (res.isPresent()) {
            sid = res.get();
            dbrt.setTripNum(sid);
            stopTimeRepository.deleteByTripNum(sid);
            frequencyRepository.deleteByTripNum(sid);
        }
        try {
            Long tid = tripRepository.save(dbrt).getTripNum();
            handleStopTimes(tid, tr.getStopTimes());
            handleFrequencies(tid, tr.getFrequencies());
        } catch (Exception e) {
            LOG.error("Continuing anyway...", e);
        }
        return sid;
    }
    
    private void handleStopTimes(Long tid, Collection<StopTimeData> stc) {
        List<DBStopTime> times = new ArrayList<>();
        for (StopTimeData st : stc) {
            Long sid = stopManagement.getStopNumber(st);
            if (sid == null) {
                LOG.warn("Check stop {}", st.getStopId());
                continue;
            }
            DBStopTime dbst = new DBStopTime(tid, sid, st);
            times.add(dbst);
        }
        stopTimeRepository.saveBatch(times);
    }

    private void handleFrequencies(Long tid, Collection<FrequencyData> fr) {
        List<DBFrequency> times = new ArrayList<>();
        for (FrequencyData freq : fr) {
            DBFrequency dbfr = new DBFrequency(tid, freq);
            times.add(dbfr);
        }
        frequencyRepository.saveBatch(times);
    }
}
