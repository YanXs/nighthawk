package com.github.nightawk.mq.kafka;

public class ByteArrayTracingSerializer extends AbstractTracingSerializer<byte[]> {

    @Override
    protected byte[] doSerialize(byte[] data) {
        return data;
    }
}
