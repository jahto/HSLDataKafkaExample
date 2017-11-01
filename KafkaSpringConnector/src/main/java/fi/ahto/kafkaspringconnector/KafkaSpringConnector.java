/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.kafkaspringconnector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
/**
 *
 * @author Jouni Ahto
 */
@SpringBootApplication
@EnableScheduling
public class KafkaSpringConnector {

    public static void main(String[] args) {
        SpringApplication.run(KafkaSpringConnector.class, args);
    }
}
