/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    
    @Autowired
    private TrafficDataStores dataStores;
    
    @RequestMapping("/test1")
    public FakeTestMessage findAll() {
        return listener.getLatestFake();
    }

    @RequestMapping("/test2")
    public FakeTestMessage findLatest() {
        return listener.getLatestFakeFromStore();
    }
}
