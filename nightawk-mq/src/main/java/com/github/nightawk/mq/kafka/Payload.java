package com.github.nightawk.mq.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public class Payload<K, V> {

    private final TracingHeader tracingHeader;
    private final ConsumerRecord<K, V> dataRecord;
    private final boolean sampled;

    public Payload(TracingHeader tracingHeader, ConsumerRecord<K, V> dataRecord, boolean sampled) {
        this.tracingHeader = tracingHeader;
        this.dataRecord = dataRecord;
        this.sampled = sampled;
    }

    public boolean isSampled() {
        return sampled;
    }

    public TracingHeader getTracingHeader() {
        return tracingHeader;
    }

    public ConsumerRecord<K, V> record() {
        return dataRecord;
    }

    public K key() {
        return dataRecord.key();
    }

    public V value() {
        return dataRecord.value();
    }

    public String topic() {
        return dataRecord.topic();
    }

    public int partition() {
        return dataRecord.partition();
    }

    public long offset() {
        return dataRecord.offset();
    }
}
