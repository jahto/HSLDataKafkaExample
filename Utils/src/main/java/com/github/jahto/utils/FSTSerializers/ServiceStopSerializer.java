/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jahto.utils.FSTSerializers;

import com.github.jahto.utils.FSTSerializers.java.time.SerializerImplementations;
import fi.ahto.example.traffic.data.contracts.internal.ServiceStop;
import java.io.IOException;
import java.time.LocalTime;
import org.nustaq.serialization.FSTBasicObjectSerializer;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

/**
 *
 * @author jah
 */
public class ServiceStopSerializer extends FSTBasicObjectSerializer {

    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        ServiceStop st = (ServiceStop) toWrite;
        writeUTFOrNull(st.stopid, out);
        out.writeInt(st.seq);
        SerializerImplementations.serializeLocalTime(st.arrivalTime, out);
        writeUTFOrNull(st.name, out);
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        ServiceStop st = new ServiceStop();
        st.stopid = readUTFOrNull(in);
        st.seq = in.readInt();
        st.arrivalTime = (LocalTime) SerializerImplementations.deserializeLocalTime(in);
        st.name = readUTFOrNull(in);
        return st;
    }

    private void writeUTFOrNull(String str, FSTObjectOutput out) throws IOException {
        if (str == null) {
            out.writeByte(FSTObjectOutput.NULL);
            return;
        }
        out.writeByte(FSTObjectOutput.STRING);
        out.writeUTF(str);
    }
    
    private String readUTFOrNull(FSTObjectInput in) throws IOException {
        byte code = in.readByte();
        if (code == FSTObjectOutput.NULL) {
            
        }
        // Shouldn't happen..
        if (code != FSTObjectOutput.STRING) {
            return null;
        }
        return in.readUTF();
    }
}
