package com.github.nightawk.mq.kafka;

import com.github.nightawk.core.util.ErrorHandler;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

public class ListenableTracingConsumer<K, V> extends AbstractListenableConsumer<K, V> implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Consumer<K, byte[]> delegate;

    private final Collection<String> topics;

    private final Pattern topicPattern;

    private final Deserializer<V> valueDeserializer;

    private final Map<TopicPartition, PayloadContainer> payloadContainers = new HashMap<>();

    private volatile boolean running = false;

    private PayloadListener<K, V> listener;

    private long pollTimeout = 1000;

    private ErrorHandler errorHandler;

    public ListenableTracingConsumer(Consumer<K, byte[]> delegate, Collection<String> topics, Deserializer<V> valueDeserializer) {
        super(delegate);
        this.delegate = delegate;
        this.topics = topics;
        this.topicPattern = null;
        this.valueDeserializer = valueDeserializer;
    }

    public ListenableTracingConsumer(Consumer<K, byte[]> delegate, Pattern topicPattern, Deserializer<V> valueDeserializer) {
        super(delegate);
        this.delegate = delegate;
        this.topicPattern = topicPattern;
        this.topics = null;
        this.valueDeserializer = valueDeserializer;
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
        if (topics != null) {
            delegate.subscribe(topics, rebalanceListener);
        } else {
            delegate.subscribe(topicPattern, rebalanceListener);
        }
        Thread executeThread = new Thread(this);
        executeThread.setDaemon(true);
        executeThread.start();
        setRunning(true);
    }

    public void addListener(PayloadListener<K, V> payloadListener) {
        if (isRunning()) {
            throw new IllegalStateException("listener should be set before consumer start");
        }
        this.listener = payloadListener;
    }

    public ConsumerRecords<K, V> poll(long timeout) {
        ConsumerRecords<K, byte[]> records = delegate.poll(timeout);
        Map<TopicPartition, List<ConsumerRecord<K, V>>> dataRecords = new HashMap<>();
        for (ConsumerRecord<K, byte[]> record : records) {
            TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
            Payload<K, V> payload = PayloadCodec.decodePayload(valueDeserializer, record);
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
}
