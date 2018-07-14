/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jahto.utils.FSTSerializers;

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
        out.writeUTF(st.stopid);
        out.writeInt(st.seq);
        SerializerImplementations.serializeLocalTime(st.arrivalTime, out);
        if (st.name == null) {
            out.writeUTF("\u0000");
        } else {
            out.writeUTF(st.name);
        }
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        ServiceStop st = new ServiceStop();
        st.stopid = in.readUTF();
        st.seq = in.readInt();
        st.arrivalTime = (LocalTime) SerializerImplementations.deserializeLocalTime(in);
        String name  = in.readUTF();
        if ("\u0000".equals(name)) {
            st.name = null;
        }
        else {
            st.name = name;
        }
        return st;
    }
}
