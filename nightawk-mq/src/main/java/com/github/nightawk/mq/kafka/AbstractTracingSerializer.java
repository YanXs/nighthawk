package com.github.nightawk.mq.kafka;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.IdConversion;
import com.github.kristofa.brave.SpanId;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public abstract class AbstractTracingSerializer<T> implements Serializer<T> {

    protected Brave brave;

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        if (isKey) {
            throw new IllegalArgumentException("this must be a value serializer");
        }
        brave = (Brave) configs.get("tracing.component");
        if (brave == null) {
            throw new IllegalArgumentException("brave must not be null in tracingSerializer");
        }
    }

    protected TracingPayload startTracing(String topic) {
        SpanId spanId = brave.clientTracer().startNewSpan("kafka - " + topic);
        TracingPayload tp = null;
        if (spanId != null) {
            tp = new TracingPayload();
            tp.setTraceId(IdConversion.convertToString(spanId.traceId));
            tp.setSpanId(IdConversion.convertToString(spanId.spanId));
            tp.setParentSpanId(IdConversion.convertToString(spanId.parentId));
            tp.setSampled("1");
        }
        return tp;
    }

    protected byte[] assemblePayload(String topic, byte[] data) {
        TracingPayload tp = startTracing(topic);
        if (tp == null) {
            return data;
        } else {
            return PayloadCodec.encodePayload(tp, data);
        }
    }

    protected abstract byte[] encode(T data);

    @Override
    public void close() {
    }

}
