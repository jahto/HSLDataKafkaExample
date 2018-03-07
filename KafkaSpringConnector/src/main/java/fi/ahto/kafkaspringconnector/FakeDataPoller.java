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
package fi.ahto.kafkaspringconnector;

import fi.ahto.kafkaspringdatacontracts.FakeTestMessage;
import java.time.LocalDateTime;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 *
 * @author Jouni Ahto
 */
@Component
public class FakeDataPoller {
    private static final Logger log = LoggerFactory.getLogger(FakeDataPoller.class);
    private static final Lock lock = new ReentrantLock();
    
    @Autowired
    private KafkaTemplate<String, FakeTestMessage> msgtemplate;

    // @Scheduled(fixedRate = 1000)
    public void pollData() {
        // Seems to be unnecessary when using the default executor, it doesn't start a new
        // task anyway until the previous one has finished. But things could change if
        // some other executor is used...
        if (!lock.tryLock()) {
            log.info("Skipping polling");
            return;
        }
        try {
            log.info("Polling data...");
            FakeTestMessage msg = new FakeTestMessage("Polling data..." + LocalDateTime.now());
            msgtemplate.send("data-fake-raw", "latest", msg);
            // strtemplate.send("data-fake-raw", "Polling data...");
            // Thread.sleep(6000);
            Thread.sleep(6);
        }
        catch (InterruptedException e) {
            // Nothing to do, but must be catched.
        }
        finally {
            lock.unlock();
        }
    }
}
