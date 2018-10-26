/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.example.traffic.data.contracts.internal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

/**
 *
 * @author jah
 */
public class GTFSLocalTimeJsonDeserializer extends JsonDeserializer<GTFSLocalTime> {

    @Override
    public GTFSLocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return GTFSLocalTime.ofSecondOfDay(p.getIntValue());
    }
    
}
