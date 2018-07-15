/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.jahto.utils.FSTSerializers;

import fi.ahto.example.traffic.data.contracts.internal.ServiceStop;
import fi.ahto.example.traffic.data.contracts.internal.TripStop;
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
public class TripStopSerializer extends FSTBasicObjectSerializer {

    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        TripStop st = (TripStop) toWrite;
        out.writeUTF(st.stopid);
        out.writeInt(st.seq);
        out.writeInt(st.arrivalTime);
        //SerializerImplementations.serializeLocalTime(st.arrivalTime, out);
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        TripStop st = new TripStop();
        st.stopid = in.readUTF();
        st.seq = in.readInt();
        st.arrivalTime = in.readInt();
        //st.arrivalTime = (LocalTime) SerializerImplementations.deserializeLocalTime(in);
        return st;
    }
    @Override
    public boolean alwaysCopy() {
        return true;
    }
}
