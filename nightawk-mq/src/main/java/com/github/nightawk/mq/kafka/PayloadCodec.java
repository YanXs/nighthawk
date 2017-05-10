package com.github.nightawk.mq.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.ByteBuffer;

public class PayloadCodec {

    public static final int HEADER_LENGTH = 4;

    public static final short MAGIC = (short) 0xbabe;

    public static <K, V> Payload<K, V> decodePayload(Deserializer<V> valueDeserializer, ConsumerRecord<K, byte[]> originConsumerRecord) {
        TracingPayload tracingPayload = null;
        ConsumerRecord<K, V> dataRecord = null;
        boolean sampled = false;
        byte[] data = originConsumerRecord.value();
        byte[] vData = null;
        if (data.length <= HEADER_LENGTH) {
            vData = data;
        } else {
            ByteBuffer byteBuf = ByteBuffer.wrap(data);
            short magic = byteBuf.getShort(0);
            short tpLen = byteBuf.getShort(2);
            if (magic == MAGIC && tpLen == TracingPayload.LENGTH) {
                byte[] tpBytes = new byte[tpLen];
                System.arraycopy(byteBuf.array(), HEADER_LENGTH, tpBytes, 0, tpLen);
                tracingPayload = TracingPayload.fromBytes(tpBytes);
                sampled = true;
                int dataOffset = tpLen + HEADER_LENGTH;
                vData = new byte[byteBuf.array().length - dataOffset];
                System.arraycopy(byteBuf.array(), dataOffset, vData, 0, vData.length);
            } else {
                vData = data;
            }
        }
        dataRecord = new ConsumerRecord<>(originConsumerRecord.topic(),
                originConsumerRecord.partition(), originConsumerRecord.offset(),
                originConsumerRecord.key(), valueDeserializer.deserialize(originConsumerRecord.topic(), vData));
        return new Payload<>(tracingPayload, dataRecord, sampled);
    }


    public static byte[] encodePayload(TracingPayload tp, byte[] data) {
        byte[] tpBytes = tp.toBytes();
        short tpLength = (short) tpBytes.length;
        ByteBuffer byteBuffer = ByteBuffer.allocate(HEADER_LENGTH + tpBytes.length + data.length);
        // header
        byteBuffer.putShort(0, MAGIC);
        byteBuffer.putShort(2, tpLength);

        byte[] hb = byteBuffer.array();
        System.arraycopy(tpBytes, 0, hb, HEADER_LENGTH, tpLength);
        System.arraycopy(data, 0, hb, HEADER_LENGTH + tpLength, data.length);
        return hb;
    }

}
