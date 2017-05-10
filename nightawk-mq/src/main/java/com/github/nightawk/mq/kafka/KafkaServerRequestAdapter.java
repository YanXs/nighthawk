package com.github.nightawk.mq.kafka;

import com.github.kristofa.brave.KeyValueAnnotation;
import com.github.kristofa.brave.ServerRequestAdapter;
import com.github.kristofa.brave.SpanId;
import com.github.kristofa.brave.TraceData;

import java.util.Collection;
import java.util.Collections;

import static com.github.kristofa.brave.IdConversion.convertToLong;

public class KafkaServerRequestAdapter implements ServerRequestAdapter {

    private final TracingPayload tracingPayload;

    public KafkaServerRequestAdapter(TracingPayload tracingPayload) {
        this.tracingPayload = tracingPayload;
    }

    @Override
    public TraceData getTraceData() {
        String sampled = tracingPayload.getSampled();
        if (sampled == null || sampled.equals("0")) {
            return TraceData.builder().sample(false).build();
        } else {
            final String parentSpanId = tracingPayload.getParentSpanId();
            final String traceId = tracingPayload.getTraceId();
            final String spanId = tracingPayload.getSpanId();

            if (traceId != null && spanId != null) {
                SpanId span = getSpanId(traceId, spanId, parentSpanId);
                return TraceData.builder().sample(true).spanId(span).build();
            }
        }
        return TraceData.builder().build();
    }

    @Override
    public String getSpanName() {
        return "kafka-mq";
    }

    @Override
    public Collection<KeyValueAnnotation> requestAnnotations() {
        return Collections.emptyList();
    }

    private SpanId getSpanId(String traceId, String spanId, String parentSpanId) {
        return SpanId.builder()
                .traceId(convertToLong(traceId))
                .spanId(convertToLong(spanId))
                .parentId(parentSpanId == null ? null : convertToLong(parentSpanId)).build();
    }
}
