/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.example.traffic.data.database.repositories.mongo;

import fi.ahto.example.traffic.data.contracts.database.DBStop;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 *
 * @author jah
 */
public interface StopRepository extends MongoRepository<DBStop, String>{
    
}
