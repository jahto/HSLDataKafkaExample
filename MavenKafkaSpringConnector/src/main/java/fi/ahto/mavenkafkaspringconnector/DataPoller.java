/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.mavenkafkaspringconnector;

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
 * @author jah
 */
@Component
public class DataPoller {
    private static final Logger log = LoggerFactory.getLogger(DataPoller.class);
    private static final Lock lock = new ReentrantLock();
    
    @Autowired
    private KafkaTemplate<Integer, FakeTestMessage> template;
    
    @Scheduled(fixedRate = 5000)
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
            FakeTestMessage msg = new FakeTestMessage("Polling data...");
            template.send("data-raw", msg);
            Thread.sleep(6000);
        }
        catch (InterruptedException e) {
            // Nothing to do, but must be catched.
        }
        finally {
            lock.unlock();
        }
    }
}
