/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.kafkaspringlistener;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

/**
 *
 * @author Jouni Ahto
 */
@Service
@DependsOn("TrafficDataStreamsListener")
public class TrafficDataStores {
    
}
