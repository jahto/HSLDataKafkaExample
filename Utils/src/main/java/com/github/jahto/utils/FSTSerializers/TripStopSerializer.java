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
package com.github.jahto.utils.FSTSerializers;

import com.github.jahto.utils.FSTSerializers.java.time.SerializerImplementations;
import fi.ahto.example.traffic.data.contracts.internal.TripStop;
import java.io.IOException;
import org.nustaq.serialization.FSTBasicObjectSerializer;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

/**
 *
 * @author Jouni Ahto
 */
public class TripStopSerializer extends FSTBasicObjectSerializer {

    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        TripStop st = (TripStop) toWrite;
        writeUTFOrNull(st.stopid, out);
        out.writeInt(st.seq);
        FSTGTFSLocalTimeSerializer.serialize(out, st.arrivalTime);
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        TripStop st = new TripStop();
        st.stopid = readUTFOrNull(in);
        st.seq = in.readInt();
        st.arrivalTime = FSTGTFSLocalTimeSerializer.deserialize(in);
        return st;
    }

    @Override
    public boolean alwaysCopy() {
        return true;
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
