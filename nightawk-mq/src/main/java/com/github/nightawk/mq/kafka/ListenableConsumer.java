package com.github.nightawk.mq.kafka;

import org.apache.kafka.clients.consumer.Consumer;

public interface ListenableConsumer<K, V> extends Consumer<K, V>{

    void start();

    void addListener(PayloadListener<K, V> listener);
}
