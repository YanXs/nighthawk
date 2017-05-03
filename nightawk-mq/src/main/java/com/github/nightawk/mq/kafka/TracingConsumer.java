package com.github.nightawk.mq.kafka;

import com.github.kristofa.brave.Brave;
import com.github.nightawk.core.util.Codec;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.util.Assert;

import java.util.*;

public class TracingConsumer extends KafkaConsumer<String, byte[]> {

    private final Brave brave;

    public TracingConsumer(Map<String, Object> configs, Brave brave) {
        super(configs);
        this.brave = brave;
    }

    public TracingConsumer(Map<String, Object> configs, Deserializer<String> keyDeserializer, Deserializer<byte[]> valueDeserializer, Brave brave) {
        super(configs, keyDeserializer, valueDeserializer);
        this.brave = brave;
    }

    public TracingConsumer(Properties properties, Brave brave) {
        super(properties);
        this.brave = brave;
    }

    public TracingConsumer(Properties properties, Deserializer<String> keyDeserializer, Deserializer<byte[]> valueDeserializer, Brave brave) {
        super(properties, keyDeserializer, valueDeserializer);
        this.brave = brave;
    }

    public ConsumerRecords<String, byte[]> poll(long timeout) {
        ConsumerRecords<String, byte[]> records = super.poll(timeout);
        Map<TopicPartition, List<ConsumerRecord<String, byte[]>>> dataRecords = new HashMap<>();
        for (ConsumerRecord<String, byte[]> record : records) {
            TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
            Payload payload = new Payload(Codec.JSON, topicPartition, record.offset());
            payload.process(record);
            if (payload.isSampled()) {
                Assert.notNull(brave, "brave must not be null");
                brave.serverRequestInterceptor().handle(new KafkaServerRequestAdapter(payload.getTracingPayload()));
            }
            List<ConsumerRecord<String, byte[]>> list = dataRecords.get(topicPartition);
            if (list == null) {
                list = new ArrayList<>();
                dataRecords.put(topicPartition, list);
            }
            list.add(payload.getDataRecord());
        }
        return new ConsumerRecords<>(dataRecords);
    }

    @Override
    public void commitSync(final Map<TopicPartition, OffsetAndMetadata> offsets) {
        super.commitSync(offsets);
    }

    @Override
    public void commitAsync(final Map<TopicPartition, OffsetAndMetadata> offsets, OffsetCommitCallback callback) {
        super.commitAsync(offsets, new OffsetCommitCallback() {
            @Override
            public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {

            }
        });
    }

    public void commitSync() {
        throw new UnsupportedOperationException();
    }

    public void commitAsync() {
        throw new UnsupportedOperationException();
    }

    public void commitAsync(OffsetCommitCallback callback) {
        throw new UnsupportedOperationException();
    }
}
