package com.github.nightawk.mq.kafka;

public class ByteArrayTracingSerializer extends AbstractTracingSerializer<byte[]> {

    @Override
    public byte[] serialize(String topic, byte[] data) {
        return assemblePayload(topic, encode(data));
    }

    @Override
    protected byte[] encode(byte[] data) {
        return data;
    }
}
