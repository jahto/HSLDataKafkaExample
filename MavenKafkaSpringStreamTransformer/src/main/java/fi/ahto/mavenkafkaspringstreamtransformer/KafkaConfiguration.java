/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.mavenkafkaspringstreamtransformer;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;

/**
 *
 * @author jah
 */
@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaConfiguration {
    
}
