package com.github.nightawk.mq.kafka;

import com.github.kristofa.brave.Brave;

public abstract class AbstractTracingListener<K, V> implements PayloadListener<K, V> {

    private final Brave brave;

    public AbstractTracingListener(Brave brave) {
        this.brave = brave;
    }

    @Override
    public void preProcessPayload(Payload<K, V> payload) {
        if (payload.isSampled()) {
            brave.serverRequestInterceptor().handle(new KafkaServerRequestAdapter(payload.getTracingPayload()));
        }
    }

    @Override
    public void postProcessPayload(Payload<K, V> payload, Throwable t) {
        if (payload.isSampled()){
            brave.serverResponseInterceptor().handle(new KafkaServerResponseAdapter(t));
        }
    }

}
