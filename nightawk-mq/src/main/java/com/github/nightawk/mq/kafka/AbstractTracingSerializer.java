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

    protected TracingHeader startTracing(String topic) {
        SpanId spanId = brave.clientTracer().startNewSpan("kafka - " + topic);
        TracingHeader tp = null;
        if (spanId != null) {
            tp = new TracingHeader();
            tp.setTraceId(IdConversion.convertToString(spanId.traceId));
            tp.setSpanId(IdConversion.convertToString(spanId.spanId));
            tp.setParentSpanId(IdConversion.convertToString(spanId.parentId));
            tp.setSampled("1");
        }
        return tp;
    }

    protected byte[] assemblePayload(String topic, byte[] data) {
        TracingHeader tp = startTracing(topic);
        if (tp == null) {
            return data;
        } else {
            return PayloadCodec.encodePayload(tp, data);
        }
    }

    @Override
    public byte[] serialize(String topic, T data) {
        return assemblePayload(topic, doSerialize(data));
    }


    protected abstract byte[] doSerialize(T data);

    @Override
    public void close() {
    }
}
