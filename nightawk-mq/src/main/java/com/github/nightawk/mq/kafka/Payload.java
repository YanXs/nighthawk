package com.github.nightawk.mq.kafka;

import com.github.nightawk.core.util.Codec;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;

import java.nio.ByteBuffer;

public class Payload {

    private final Codec codec;
    private TracingPayload tracingPayload;
    private ConsumerRecord<String, byte[]> dataRecord;
    private final TopicPartition topicPartition;
    private final Long initialOffset;
    private boolean sampled;

    public Payload(Codec codec, TopicPartition topicPartition, Long initialOffset) {
        this.codec = codec;
        this.topicPartition = topicPartition;
        this.initialOffset = initialOffset;
    }

    public void process(ConsumerRecord<String, byte[]> originConsumerRecord) {
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
        dataRecord = new ConsumerRecord<>(originConsumerRecord.topic(),
                originConsumerRecord.partition(), originConsumerRecord.offset(), originConsumerRecord.key(), vData);

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

    public ConsumerRecord<String, byte[]> getDataRecord() {
        return dataRecord;
    }

}
