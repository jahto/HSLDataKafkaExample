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

import fi.ahto.example.traffic.data.contracts.internal.VehicleActivity;
import fi.ahto.example.traffic.data.contracts.internal.VehicleDataList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 *
 * @author Jouni Ahto
 */

@Component
public class BrokerSender {
    private static final Logger LOG = LoggerFactory.getLogger(BrokerSender.class);
    
    private SimpMessagingTemplate template;
    
    @Autowired
    public BrokerSender(SimpMessagingTemplate template) {
        LOG.debug("BrokerSender created.");
        this.template = template;
    }
    
    public void sendLine(String key, VehicleDataList vdl) {
        template.convertAndSend("/rt/" + key, vdl);
    }
    
    public void sendVehicle(String key, VehicleActivity va) {
        
    }
}
