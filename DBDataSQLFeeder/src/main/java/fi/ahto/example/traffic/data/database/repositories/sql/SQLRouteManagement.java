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

import fi.ahto.example.traffic.data.contracts.internal.RouteData;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
/**
 *
 * @author Jouni Ahto
 */
@Service
@Repository
public interface SQLRouteManagement {
    Long handleRoute(String key, RouteData rt, boolean update);
    Long getRouteNumber(String key, RouteData rt);
}
