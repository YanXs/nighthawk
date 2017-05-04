package com.github.nightawk.mq.kafka;

import com.github.nightawk.core.util.Codec;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.ByteBuffer;

public class Payload<K, V> {

    private final Codec codec;
    private final Deserializer<V> valueDeserializer;
    private TracingPayload tracingPayload;
    private ConsumerRecord<K, V> dataRecord;
    private boolean sampled;

    public Payload(Codec codec, Deserializer<V> valueDeserializer) {
        this.codec = codec;
        this.valueDeserializer = valueDeserializer;
    }

    public void process(ConsumerRecord<K, byte[]> originConsumerRecord) {
        byte[] data = originConsumerRecord.value();
        ByteBuffer byteBuf = ByteBuffer.allocate(data.length);
        byteBuf.put(data);
        // get tracing payload length
        int tpLen = byteBuf.getShort(0);
        if (tpLen > 0) {
            byte[] tpBytes = new byte[tpLen];
            System.arraycopy(byteBuf.array(), TracingPayload.TP_LENGTH, tpBytes, 0, tpLen);
            this.tracingPayload = decode(tpBytes);
            this.sampled = true;
        }
        int dataOffset = tpLen + TracingPayload.TP_LENGTH;
        byte[] vData = new byte[byteBuf.array().length - dataOffset];
        System.arraycopy(byteBuf.array(), dataOffset, vData, 0, vData.length);
        this.dataRecord = new ConsumerRecord<>(originConsumerRecord.topic(),
                originConsumerRecord.partition(), originConsumerRecord.offset(),
                originConsumerRecord.key(), valueDeserializer.deserialize(originConsumerRecord.topic(), vData));

    }

    private TracingPayload decode(byte[] bytes) {
        return codec.read(bytes, TracingPayload.class);
    }

    public boolean isSampled() {
        return sampled;
    }

    public TracingPayload getTracingPayload() {
        return tracingPayload;
    }

    public ConsumerRecord<K, V> getDataRecord() {
        return dataRecord;
    }
}
