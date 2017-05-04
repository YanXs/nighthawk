package com.github.nightawk.mq.kafka;

import com.github.kristofa.brave.KeyValueAnnotation;
import com.github.kristofa.brave.ServerResponseAdapter;
import com.github.nightawk.core.util.ExceptionTracer;

import java.util.Collection;
import java.util.Collections;

public class KafkaServerResponseAdapter implements ServerResponseAdapter {

    private final Throwable error;

    public KafkaServerResponseAdapter(Throwable error) {
        this.error = error;
    }

    @Override
    public Collection<KeyValueAnnotation> responseAnnotations() {
        KeyValueAnnotation statusAnnotation;
        if (error == null) {
            statusAnnotation = KeyValueAnnotation.create("process.status", "OK");
        } else {
            statusAnnotation = KeyValueAnnotation.create("process.status", ExceptionTracer.trace(error));
        }
        return Collections.singletonList(statusAnnotation);
    }
}
