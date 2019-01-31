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

import fi.ahto.example.traffic.data.contracts.database.sql.DBCalendar;
import fi.ahto.example.traffic.data.contracts.database.sql.DBCalendarDate;
import fi.ahto.example.traffic.data.contracts.internal.ServiceData;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
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
public class SQLCalendarManagementImpl implements SQLCalendarManagement {

    private static final Logger LOG = LoggerFactory.getLogger(SQLCalendarManagementImpl.class);
    public static AtomicLong counter = new AtomicLong(1);
    public static ConcurrentHashMap<String, Long> servicenums = new ConcurrentHashMap<>();
    private static final Object servicelock = new Object();

    @Autowired
    private SQLCalendarRepository calendarRepository;
    @Autowired
    private SQLCalendarDateRepository calendarDateRepository;

    @Override
    public Long getServiceNumber(String key, ServiceData rt) {
        if (key == null) {
            LOG.warn("Shouldn't be possible to happen, but happens anyway...");
        }
        Long num = servicenums.get(key);
        if (num != null) {
            return num;
        }
        synchronized (servicelock) {
            num = handleService(key, rt, false);
        }
        return num;
    }

    public Long handleService(String key, ServiceData rt, boolean update) {
        Long num = handleServiceInternal(key, rt, update);
        if (num != null) {
            servicenums.put(key, num);
            LOG.info("Added key {} -> {} to servicenums", key, num);
        } else {
            // Assuming there was a duplicate key error and we already have the key.
            num = servicenums.get(key);
            LOG.info("Returning key {} -> {} from servicenums", key, num);
        }
        return num;
    }

    @Transactional
    private Long handleServiceInternal(String key, ServiceData rt, boolean update) {
        LOG.info("Handling service {}, update: {}", key, update);
        Long num = null;
        Optional<Long> res = calendarRepository.findIdByServiceId(key);
        if (res.isPresent()) {
            num = res.get();
        }
        if (!update && num != null) {
            LOG.info("Service {} already exists in db, returning {}", key, num);
            return num;
        }
        try {
            DBCalendar dbc = new DBCalendar(rt);
            dbc.setServiceNum(num);
            Long sid = calendarRepository.save(dbc).getServiceNum();
            num = sid;
            LOG.info("Saved service {}, num {} to db", key, sid);
            if (counter.addAndGet(1) % 100 == 0) {
                LOG.info("Handled {} records", counter);
            }
        } catch (org.springframework.data.relational.core.conversion.DbActionExecutionException e) {
            // Could have been org.springframework.dao.DuplicateKeyException, seems to be almost
            // impossible to totally get rid of it when running multiple threads. So we check again
            // if the key was already added to db while we were handling it and now exists. 
            res = calendarRepository.findIdByServiceId(key);
            if (res.isPresent()) {
                num = res.get();
                LOG.info("handleService duplicate");
            } else {
                throw e;
            }
        }

        if (!update) {
            LOG.info("Service {} now exists in db, returning {}", key, num);
            return num;
        }
        try {
            // Just checking if the idea works. Should add everything
            // into a batch update and run it. Ok, it seems work...
            // So we can run several threads all pumping data to the db.
            List<DBCalendarDate> dates = new ArrayList<>();
            for (LocalDate ld : rt.getInUse()) {
                DBCalendarDate dbcd = new DBCalendarDate();
                dbcd.setServiceId(num);
                dbcd.setExceptionDate(ld);
                dbcd.setExceptionType((short) 1);
                dates.add(dbcd);
            }
            for (LocalDate ld : rt.getNotInUse()) {
                DBCalendarDate dbcd = new DBCalendarDate();
                dbcd.setServiceId(num);
                dbcd.setExceptionDate(ld);
                dbcd.setExceptionType((short) 2);
                dates.add(dbcd);
            }
            calendarDateRepository.deleteByServiceNum(num);
            calendarDateRepository.saveBatch(dates);
        } catch (Exception e) {
            LOG.error("handleService", e);
            throw e;
        }
        LOG.info("Service {} now updated in db with exception dates, returning {}", key, num);
        return num;
    }
}
