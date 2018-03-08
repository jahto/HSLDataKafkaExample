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
package fi.ahto.kafkaspringlistener;

import fi.ahto.kafkaspringdatacontracts.FakeTestMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Jouni Ahto
 */
@RestController
public class StreamWebServer {
    @Autowired
    private FakeMessageStreamListener listener;
    
    // @Autowired
    // private TrafficDataStores dataStores;
    
    @RequestMapping("/test1")
    public FakeTestMessage findAll() {
        return listener.getLatestFake();
    }

    @RequestMapping("/test2")
    public FakeTestMessage findLatest() {
        return listener.getLatestFakeFromStore();
    }
}
