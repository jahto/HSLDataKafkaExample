/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.kafkaspringlistener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 * @author Jouni Ahto
 */
@SpringBootApplication
public class KafkaSpringListener {

    public static void main(String[] args) {
        SpringApplication.run(KafkaSpringListener.class, args);
    }

}
