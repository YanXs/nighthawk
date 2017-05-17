package com.github.nightawk.mq.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class AbstractListenableConsumer<K, V> implements ListenableConsumer<K, V> {

    private final Consumer<K, byte[]> delegate;

    protected AbstractListenableConsumer(Consumer<K, byte[]> delegate) {
        this.delegate = delegate;
    }

    @Override
    public Set<TopicPartition> assignment() {
        return delegate.assignment();
    }

    @Override
    public Set<String> subscription() {
        return delegate.subscription();
    }

    @Override
    public void subscribe(Collection<String> topics) {
        delegate.subscribe(topics);
    }

    @Override
    public void subscribe(Collection<String> topics, ConsumerRebalanceListener callback) {
        delegate.subscribe(topics, callback);
    }

    @Override
    public void assign(Collection<TopicPartition> partitions) {
        delegate.assign(partitions);
    }

    @Override
    public void subscribe(Pattern pattern, ConsumerRebalanceListener callback) {
        delegate.subscribe(pattern, callback);
    }

    @Override
    public void unsubscribe() {
        delegate.unsubscribe();
    }

    @Override
    public void commitSync() {
        delegate.commitSync();
    }

    @Override
    public void commitSync(Map<TopicPartition, OffsetAndMetadata> offsets) {
        delegate.commitSync(offsets);
    }

    @Override
    public void commitAsync() {
        delegate.commitAsync();
    }

    @Override
    public void commitAsync(OffsetCommitCallback callback) {
        delegate.commitAsync(callback);
    }

    @Override
    public void commitAsync(Map<TopicPartition, OffsetAndMetadata> offsets, OffsetCommitCallback callback) {
        delegate.commitAsync(offsets, callback);
    }

    @Override
    public void seek(TopicPartition partition, long offset) {
        delegate.seek(partition, offset);
    }

    @Override
    public void seekToBeginning(Collection<TopicPartition> partitions) {
        delegate.seekToBeginning(partitions);
    }

    @Override
    public void seekToEnd(Collection<TopicPartition> partitions) {
        delegate.seekToEnd(partitions);
    }

    @Override
    public long position(TopicPartition partition) {
        return delegate.position(partition);
    }

    @Override
    public OffsetAndMetadata committed(TopicPartition partition) {
        return delegate.committed(partition);
    }

    @Override
    public Map<MetricName, ? extends Metric> metrics() {
        return delegate.metrics();
    }

    @Override
    public List<PartitionInfo> partitionsFor(String topic) {
        return delegate.partitionsFor(topic);
    }

    @Override
    public Map<String, List<PartitionInfo>> listTopics() {
        return delegate.listTopics();
    }

    @Override
    public Set<TopicPartition> paused() {
        return delegate.paused();
    }

    @Override
    public void pause(Collection<TopicPartition> partitions) {
        delegate.pause(partitions);
    }

    @Override
    public void resume(Collection<TopicPartition> partitions) {
        delegate.resume(partitions);
    }

    @Override
    public Map<TopicPartition, OffsetAndTimestamp> offsetsForTimes(Map<TopicPartition, Long> timestampsToSearch) {
        return delegate.offsetsForTimes(timestampsToSearch);
    }

    @Override
    public Map<TopicPartition, Long> beginningOffsets(Collection<TopicPartition> partitions) {
        return delegate.beginningOffsets(partitions);
    }

    @Override
    public Map<TopicPartition, Long> endOffsets(Collection<TopicPartition> partitions) {
        return delegate.endOffsets(partitions);
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public void wakeup() {
        delegate.wakeup();
    }
}
