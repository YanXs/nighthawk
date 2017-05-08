package com.github.nightawk.mq.kafka;

import com.github.kristofa.brave.KeyValueAnnotation;
import com.github.kristofa.brave.ServerResponseAdapter;
import com.github.nightawk.core.util.ExceptionTracer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KafkaServerResponseAdapter implements ServerResponseAdapter {

    private final Throwable error;

    private final Payload payload;

    public KafkaServerResponseAdapter(Payload payload, Throwable error) {
        this.payload = payload;
        this.error = error;
    }

    @Override
    public Collection<KeyValueAnnotation> responseAnnotations() {
        List<KeyValueAnnotation> responseAnnotations = new ArrayList<>();
        responseAnnotations.add(KeyValueAnnotation.create("kafka.topic", payload.topic()));
        responseAnnotations.add(KeyValueAnnotation.create("kafka.partition", String.valueOf(payload.partition())));
        responseAnnotations.add(KeyValueAnnotation.create("kafka.offset", String.valueOf(payload.offset())));
        if (error == null) {
            responseAnnotations.add(KeyValueAnnotation.create("process.status", "OK"));
        } else {
            responseAnnotations.add(KeyValueAnnotation.create("process.status", ExceptionTracer.trace(error)));
        }
        return responseAnnotations;
    }
}
