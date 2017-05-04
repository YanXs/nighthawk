package com.github.nightawk.mq.kafka;

public interface PayloadListener<K, V> {

    void preProcessPayload(Payload<K, V> payload);

    void onPayload(Payload<K, V> payload);

    void postProcessPayload(Payload<K, V> payload, Throwable t);
}
