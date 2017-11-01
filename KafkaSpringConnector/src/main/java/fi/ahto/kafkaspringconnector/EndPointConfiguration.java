/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.kafkaspringconnector;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Jouni Ahto
 */
@Configuration
public class EndPointConfiguration {
    @Value("${SIRI_JSON_SERVICE:http://api.digitransit.fi/realtime/vehicle-positions/v1/siriaccess/vm/json}")
    private String siriJsonService;
    
    @Bean
    public URI getServiceURI() {
        try {
            return new URI(siriJsonService);
        } catch (URISyntaxException ex) {
            return null;
        }
    }

}
