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
package fi.ahto.example.traffic.data.web.server;

import fi.ahto.example.traffic.data.contracts.internal.VehicleDataList;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.kafka.streams.kstream.internals.Change;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author Jouni Ahto
 */
@Component
public class LineDataProcessorSupplier implements ProcessorSupplier<String, Change<VehicleDataList>> {

    private static final Logger LOG = LoggerFactory.getLogger(LineDataProcessorSupplier.class);

    @Autowired
    private LineDataProcessor lineProcessor;

    public LineDataProcessorSupplier() {
        LOG.debug("LineDataProcessorSupplier created.");
    }

    @Override
    public Processor<String, Change<VehicleDataList>> get() {
        return lineProcessor;
    }

    @Component
    static class LineDataProcessor implements Processor<String, Change<VehicleDataList>> {

        @Autowired
        BrokerSender sender;

        public LineDataProcessor() {
            LOG.debug("LineDataProcessor created.");
        }

        @Override
        public void init(ProcessorContext pc) {
            // Nothing to do.
        }

        @Override
        public void process(String k, Change<VehicleDataList> v) {
            if (v.newValue != null) {
                sender.sendLine(k, v.newValue);
            }
        }

        @Override
        @SuppressWarnings("deprecation")
        public void punctuate(long l) {
            // Nothing to do. Also deperecated, but must still be overridden.
        }

        @Override
        public void close() {
            // Nothing to do. Kafka streams will handle closing automatically-
        }
    }
}
