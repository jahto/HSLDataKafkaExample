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

import fi.ahto.example.traffic.data.contracts.database.sql.DBRoute;
import fi.ahto.example.traffic.data.contracts.internal.RouteData;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
/**
 *
 * @author Jouni Ahto
 */
@Service
@Repository
public class SQLRouteManagementImpl implements SQLRouteManagement {
    private static final Logger LOG = LoggerFactory.getLogger(SQLStopManagementImpl.class);
    private static ConcurrentHashMap<String, Long> routenums = new ConcurrentHashMap<>();
    private static final Object routelock = new Object();
    
    @Autowired
    private SQLRouteRepository routeRepository;

    public Long getRouteNumber(String key, RouteData rt) {
        Long num = routenums.get(key);
        if (num != null) {
            return num;
        }
        synchronized (routelock) {
            num = handleRoute(key, rt, false);
        }
        return num;
    }

    public Long handleRoute(String key, RouteData rt, boolean update) {
        LOG.info("Handling route {}, update: {}", key, update);
        Long num = null;
        Optional<Long> res = routeRepository.findIdByRouteId(key);
        if (res.isPresent()) {
            num = res.get();
            routenums.put(key, num);
        }

        if (!update && num != null) {
            return num;
        }

        try {
            DBRoute dbrt = new DBRoute(rt);
            dbrt.setRouteId(key);
            dbrt.setRouteNum(num);
            Long id = routeRepository.save(dbrt).getRouteNum();
            if (id != null) {
                routenums.put(dbrt.getRouteId(), id);
                num = id;
            }
        } catch (org.springframework.data.relational.core.conversion.DbActionExecutionException e) {
            // Could have been org.springframework.dao.DuplicateKeyException, seems to be almost
            // impossible to totally get rid of it when running multiple threads. So we check again
            // if the key was already added to db while we were handling it and now exists. 
            res = routeRepository.findIdByRouteId(key);
            if (res.isPresent()) {
                num = res.get();
                LOG.info("handleRoute duplicate");
            } else {
                throw e;
            }
        }
        return num;
    }
}
