/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.example.traffic.data.contracts.internal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

/**
 *
 * @author jah
 */
public class GTFSLocalTimeJsonSerializer extends JsonSerializer<GTFSLocalTime> {

    @Override
    public void serialize(GTFSLocalTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeNumber(value.toSecondOfDay());
    }
}
