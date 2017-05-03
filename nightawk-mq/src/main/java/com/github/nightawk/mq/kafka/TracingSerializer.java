package com.github.nightawk.mq.kafka;

import org.apache.kafka.common.serialization.ByteArraySerializer;

import java.util.Map;

public class TracingSerializer extends ByteArraySerializer {

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, byte[] data) {
        return new byte[0];
    }

}
