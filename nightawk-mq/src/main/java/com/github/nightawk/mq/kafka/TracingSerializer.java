package com.github.nightawk.mq.kafka;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.IdConversion;
import com.github.kristofa.brave.SpanId;
import com.github.nightawk.core.util.Codec;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import java.nio.ByteBuffer;
import java.util.Map;

public class TracingSerializer extends ByteArraySerializer {

    public static final String TRACING_COMPONENT = "tracing.component";

    private Brave brave;

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        if (isKey) {
            throw new IllegalArgumentException("this must be a value serializer");
        }
        brave = (Brave) configs.get(TRACING_COMPONENT);
        if (brave == null) {
            throw new IllegalArgumentException("brave must not be null in tracingSerializer");
        }
    }

    @Override
    public byte[] serialize(String topic, byte[] data) {
        byte[] serializedValue;
        SpanId spanId = brave.clientTracer().startNewSpan("kafka - " + topic);
        if (spanId != null) {
            TracingPayload tp = new TracingPayload();
            tp.setTraceId(IdConversion.convertToString(spanId.traceId));
            tp.setSpanId(IdConversion.convertToString(spanId.spanId));
            tp.setParentSpanId(IdConversion.convertToString(spanId.parentId));
            tp.setSampled("1");
            serializedValue = rebuild(tp, data);
        } else {
            serializedValue = data;
        }
        return serializedValue;
    }

    private byte[] rebuild(TracingPayload tracingPayload, byte[] originData) {
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
