package com.github.nightawk.mq.kafka;

import com.github.kristofa.brave.SpanId;
import com.github.nightawk.core.util.Codec;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * @author Xs
 */
public class MockTracingSerializer extends ByteArraySerializer {

    private Random random = new Random();

    @Override
    public byte[] serialize(String topic, byte[] data) {
        byte[] serializedValue = null;
        SpanId spanId = SpanId.builder()
                .traceId(random.nextLong())
                .spanId(random.nextLong())
                .parentId(random.nextLong())
                .sampled(Boolean.TRUE)
                .build();
        TracingPayload tp = new TracingPayload();
        tp.setTraceId(String.valueOf(spanId.traceId));
        tp.setSpanId(String.valueOf(spanId.spanId));
        tp.setParentSpanId(String.valueOf(spanId.parentId));
        tp.setSampled(String.valueOf(spanId.sampled()));
        serializedValue = reBuildData(tp, data);
        return serializedValue;
    }

    private byte[] reBuildData(TracingPayload tracingPayload, byte[] originData) {
        byte[] tpBytes = Codec.JSON.write(tracingPayload);
        short tpLength = (short) tpBytes.length;
        ByteBuffer byteBuffer = ByteBuffer.allocate(TracingPayload.TP_LENGTH + tpBytes.length + originData.length);
        byteBuffer.putShort(0, tpLength);
        byte[] hb = byteBuffer.array();
        System.arraycopy(tpBytes, 0, hb, TracingPayload.TP_LENGTH, tpLength);
        System.arraycopy(originData, 0, hb, TracingPayload.TP_LENGTH + tpLength, originData.length);
        return hb;
    }
}
