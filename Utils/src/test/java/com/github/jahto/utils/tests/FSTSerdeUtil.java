/*
 * Copyright 2017-2018 the original author or authors.
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
package com.github.jahto.utils.tests;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectSerializer;

/**
 * Utility class.
 * 
 * @author Jouni Ahto
 */
public class FSTSerdeUtil {

    private final FSTConfiguration conf;
    private int size = 0;

    public FSTSerdeUtil() {
        this.conf = FSTConfiguration.createDefaultConfiguration();
    }

    public FSTSerdeUtil(Class c) {
        this.conf = FSTConfiguration.createDefaultConfiguration();
        this.conf.registerClass(c);
    }

    public FSTSerdeUtil(Class c, FSTObjectSerializer s) {
        this.conf = FSTConfiguration.createDefaultConfiguration();
        this.conf.registerClass(c);
        this.conf.registerSerializer(c, s, false);
    }

    public void add(Class c) {
        this.conf.registerClass(c);
    }
    
    public void add(Class c, FSTObjectSerializer s) {
        this.conf.registerClass(c);
        this.conf.registerSerializer(c, s, false);
    }

    public <T> T roundTrip(T data) throws IOException, ClassNotFoundException {
        byte[] result = serialize(data);
        return (T) deserialize(result);
    }
    
    public byte[] serialize(Object data) {
        byte[] result = conf.asByteArray(data);
        this.size = result.length;
        return result;
    }

    public Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bstream = new ByteArrayInputStream(data);
        ObjectInputStream ostream = new ObjectInputStream(bstream);
        Object result = ostream.readObject();
        return result;
    }
    
    public int getSize() {
        return this.size;
    }
}
