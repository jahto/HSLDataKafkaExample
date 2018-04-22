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
package fi.ahto.example.entur.data.connector;

import java.net.URI;
import java.net.URISyntaxException;
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
