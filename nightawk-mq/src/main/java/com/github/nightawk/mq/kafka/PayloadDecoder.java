package com.github.nightawk.mq.kafka;

import com.github.nightawk.core.util.Codec;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.ByteBuffer;

public class PayloadDecoder<K, V> {

    private final Codec tracingPayloadCodec;

    private final Deserializer<V> valueDeserializer;

    public PayloadDecoder(Codec tracingPayloadCodec, Deserializer<V> valueDeserializer) {
        this.tracingPayloadCodec = tracingPayloadCodec;
        this.valueDeserializer = valueDeserializer;
    }

    public Payload<K, V> decode(ConsumerRecord<K, byte[]> originConsumerRecord) {
        TracingPayload tracingPayload = null;
        ConsumerRecord<K, V> dataRecord = null;
        boolean sampled = false;
        byte[] data = originConsumerRecord.value();
        byte[] vData = null;
        if (data.length <= TracingPayload.HEADER_LENGTH) {
            vData = data;
        } else {
            ByteBuffer byteBuf = ByteBuffer.allocate(data.length);
            byteBuf.put(data);
            // get tracing payload length
            int tpLen = byteBuf.getShort(0);
            if (tpLen > 256) {
                vData = data;
            } else {
                byte[] tpBytes = new byte[tpLen];
                System.arraycopy(byteBuf.array(), TracingPayload.TP_LENGTH, tpBytes, 0, tpLen);
                tracingPayload = tracingPayloadCodec.read(tpBytes, TracingPayload.class);
                sampled = true;
                int dataOffset = tpLen + TracingPayload.TP_LENGTH;
                vData = new byte[byteBuf.array().length - dataOffset];
                System.arraycopy(byteBuf.array(), dataOffset, vData, 0, vData.length);
            }
        }
        dataRecord = new ConsumerRecord<>(originConsumerRecord.topic(),
                originConsumerRecord.partition(), originConsumerRecord.offset(),
                originConsumerRecord.key(), valueDeserializer.deserialize(originConsumerRecord.topic(), vData));
        return new Payload<>(tracingPayload, dataRecord, sampled);
    }
}
