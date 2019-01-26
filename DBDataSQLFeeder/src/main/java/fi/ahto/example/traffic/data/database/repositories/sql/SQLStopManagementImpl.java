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

import fi.ahto.example.traffic.data.contracts.database.sql.DBStop;
import fi.ahto.example.traffic.data.contracts.internal.StopData;
import fi.ahto.example.traffic.data.contracts.internal.StopTimeData;
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
public class SQLStopManagementImpl implements SQLStopManagement {

    private static final Logger LOG = LoggerFactory.getLogger(SQLStopManagementImpl.class);
    public static ConcurrentHashMap<String, Long> stopnums = new ConcurrentHashMap<>();
    private static final Object stoplock = new Object();

    @Autowired
    private SQLStopRepository stopRepository;

    @Override
    public Long getStopNumber(StopTimeData id) {
        Long num = stopnums.get(id.getStopId());
        if (num != null) {
            return num;
        }

        synchronized (stoplock) {
            num = handleStop(id.getStopId(), id.getStop(), false);
        }
        return num;
    }

    @Override
    public Long handleStop(String key, StopData rt, boolean update) {
        Long num = handleStopInternal(key, rt, update);
        if (num != null) {
            stopnums.put(key, num);
        } else {
            // Assuming there was a duplicate key error and we already have the key.
            num = stopnums.get(key);
        }
        return num;
    }

    @Transactional
    private Long handleStopInternal(String key, StopData rt, boolean update) {
        LOG.info("Handling stop {}, update: {}", key, update);
        Long num = null;
        Optional<Long> res = stopRepository.findIdByStopId(key);
        if (res.isPresent()) {
            num = res.get();
        }

        if (!update && num != null) {
            return num;
        }

        try {
            DBStop dbrt = new DBStop(rt);
            dbrt.setStopId(key);
            dbrt.setStopNum(num);
            Long id = stopRepository.save(dbrt).getStopNum();
            if (id != null) {
                num = id;
            }
        } catch (org.springframework.data.relational.core.conversion.DbActionExecutionException e) {
            // Could have been org.springframework.dao.DuplicateKeyException, seems to be almost
            // impossible to totally get rid of it when running multiple threads. So we check again
            // if the key was already added to db while we were handling it and now exists. 
            res = stopRepository.findIdByStopId(key);
            if (res.isPresent()) {
                num = res.get();
                LOG.info("handleStop duplicate");
            } else {
                throw e;
            }
        }
        return num;
    }
}
