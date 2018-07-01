/*
 * Copyright 2014 Ruediger Moeller.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jahto.utils.FSTSerializers;

import org.nustaq.serialization.FSTBasicObjectSerializer;
import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import java.io.IOException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: ruedi
 * Date: 10.11.12
 * Time: 17:47
 * To change this template use File | Settings | File Templates.
 */
public class FSTSetSerializer extends FSTBasicObjectSerializer {

    @Override
    public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
        Set col = (Set) toWrite;
        out.writeInt(col.size());
        FSTClazzInfo lastVClzI = null;
        Class lastVClz = null;
        for (Iterator iterator = col.iterator(); iterator.hasNext(); ) {
            Object value = iterator.next();
            if ( value != null ) {
                lastVClzI = out.writeObjectInternal(value, value.getClass() == lastVClz ? lastVClzI : null, (Class[]) null);
                lastVClz = value.getClass();
            } else
            {
                out.writeObjectInternal(value, null, (Class[]) null);
            }
        }
    }

    @Override
    public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo, FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
        Object res = null;
        int len = in.readInt();
        if ( objectClass == HashSet.class ) {
            res = new HashSet(len);
        } else
        if ( objectClass == TreeSet.class ) {
            res = new TreeSet();
        } else
        {
            res = objectClass.newInstance();
        }
        in.registerObject(res, streamPosition,serializationInfo, referencee);
        Set col = (Set)res;
        for ( int i = 0; i < len; i++ ) {
            Object val = in.readObjectInternal((Class[]) null);
            col.add(val);
        }
        return res;
    }
}
