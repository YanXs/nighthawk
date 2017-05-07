package com.github.nightawk.mq.kafka;

import com.github.nightawk.core.util.Codec;
import com.github.nightawk.core.util.ErrorHandler;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

public class ListenableTracingConsumer<K, V> implements ListenableConsumer<K, V>, Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Consumer<K, byte[]> delegate;

    private final Pattern topicPattern;

    private final Map<TopicPartition, PayloadContainer> payloadContainers = new HashMap<>();

    private volatile boolean running = false;

    private PayloadListener<K, V> listener;

    private long pollTimeout = 1000;

    private final PayloadDecoder<K, V> payloadDecoder;

    private ErrorHandler errorHandler;

    public ListenableTracingConsumer(Consumer<K, byte[]> delegate, Pattern topicPattern, Deserializer<V> valueDeserializer) {
        this.delegate = delegate;
        this.topicPattern = topicPattern;
        this.payloadDecoder = new PayloadDecoder<>(Codec.JSON, valueDeserializer);
    }

    public ConsumerRebalanceListener createRebalanceListener(final Consumer consumer) {
        return new ConsumerRebalanceListener() {

            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
                for (TopicPartition partition : partitions) {
                    offsets.put(partition, new OffsetAndMetadata(consumer.position(partition)));
                }
                consumer.commitSync(offsets);
            }
        };
    }

    public void start() {
        if (isRunning()) {
            return;
        }
        if (listener == null) {
            throw new IllegalStateException("no listener set");
        }
        ConsumerRebalanceListener rebalanceListener = createRebalanceListener(delegate);
        delegate.subscribe(topicPattern, rebalanceListener);
        Thread executeThread = new Thread(this);
        executeThread.start();
        setRunning(true);
    }

    public void addListener(PayloadListener<K, V> payloadListener) {
        if (isRunning()) {
            throw new IllegalStateException("listener should be set before consumer start");
        }
        this.listener = payloadListener;
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

    public ConsumerRecords<K, V> poll(long timeout) {
        ConsumerRecords<K, byte[]> records = delegate.poll(timeout);
        Map<TopicPartition, List<ConsumerRecord<K, V>>> dataRecords = new HashMap<>();
        for (ConsumerRecord<K, byte[]> record : records) {
            TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
            Payload<K, V> payload = payloadDecoder.decode(record);
            List<ConsumerRecord<K, V>> list = dataRecords.get(topicPartition);
            if (list == null) {
                list = new ArrayList<>();
                dataRecords.put(topicPartition, list);
            }
            list.add(payload.record());
            PayloadContainer payloadContainer = payloadContainers.get(topicPartition);
            if (payloadContainer == null) {
                payloadContainer = new PayloadContainer(topicPartition);
                payloadContainers.put(topicPartition, payloadContainer);
            }
            payloadContainer.add(payload);
        }
        return new ConsumerRecords<>(dataRecords);
    }

    @Override
    public void commitSync(final Map<TopicPartition, OffsetAndMetadata> offsets) {
        delegate.commitSync(offsets);
    }

    @Override
    public void commitAsync(final Map<TopicPartition, OffsetAndMetadata> offsets, OffsetCommitCallback callback) {
        delegate.commitAsync();
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
    public void close() {
        delegate.close();
    }

    @Override
    public void wakeup() {
        delegate.wakeup();
    }

    @Override
    public void run() {
        while (isRunning()) {
            try {
                ConsumerRecords<K, V> records = poll(getPollTimeout());
                if (records != null && this.logger.isDebugEnabled()) {
                    this.logger.debug("Received: " + records.count() + " records");
                }
                if (records != null && records.count() > 0) {
                    invokePayloadListener(records);
                }
            } catch (Exception e) {
                if (errorHandler != null) {
                    errorHandler.handle(e);
                } else {
                    this.logger.error("caught exception", e);
                }
            }
        }
        try {
            unsubscribe();
        } catch (WakeupException e) {
            // No-op. Continue process
        }
        close();
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Consumer stopped");
        }
    }

    @SuppressWarnings("unchecked")
    private void invokePayloadListener(final ConsumerRecords<K, V> records) {
        for (ConsumerRecord<K, V> record : records) {
            TopicPartition topicPartition = topicPartition(record);
            PayloadContainer payloadContainer = this.payloadContainers.get(topicPartition(record));
            if (payloadContainer == null || payloadContainer.isEmpty()) {
                throw new IllegalStateException("payload should not be null");
            }
            Payload pl = payloadContainer.poll();
            this.listener.preProcessPayload(pl);
            Throwable t = null;
            try {
                this.listener.onPayload(pl);
            } catch (Exception e) {
                t = e;
                throw e;
            } finally {
                this.listener.postProcessPayload(pl, t);
            }
            Map<TopicPartition, OffsetAndMetadata> commits = Collections.singletonMap(
                    topicPartition, new OffsetAndMetadata(record.offset() + 1));
            commitSync(commits);
        }
    }

    private boolean isRunning() {
        return this.running;
    }


    public long getPollTimeout() {
        return pollTimeout;
    }

    public void setPollTimeout(long pollTimeout) {
        this.pollTimeout = pollTimeout;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    private TopicPartition topicPartition(ConsumerRecord consumerRecord) {
        return new TopicPartition(consumerRecord.topic(), consumerRecord.partition());
    }

    private static final class PayloadContainer {

        private final TopicPartition topicPartition;

        private final Queue<Payload> pendingPayloads = new LinkedList<>();

        private PayloadContainer(TopicPartition topicPartition) {
            this.topicPartition = topicPartition;
        }

        public TopicPartition getTopicPartition() {
            return topicPartition;
        }

        public void add(Payload payload) {
            pendingPayloads.add(payload);
        }

        public Payload poll() {
            return pendingPayloads.poll();
        }

        public boolean isEmpty() {
            return pendingPayloads.isEmpty();
        }
    }

    public void commitSync() {
        delegate.commitSync();
    }

    public void commitAsync() {
        delegate.commitAsync();
    }

    public void commitAsync(OffsetCommitCallback callback) {
        delegate.commitAsync(callback);
    }
}
