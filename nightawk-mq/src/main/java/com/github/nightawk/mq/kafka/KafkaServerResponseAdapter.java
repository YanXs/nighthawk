package com.github.nightawk.mq.kafka;

import com.github.kristofa.brave.KeyValueAnnotation;
import com.github.kristofa.brave.ServerResponseAdapter;

import java.util.Collection;
import java.util.Collections;

public class KafkaServerResponseAdapter implements ServerResponseAdapter {

    @Override
    public Collection<KeyValueAnnotation> responseAnnotations() {
        return Collections.emptyList();
    }
}
