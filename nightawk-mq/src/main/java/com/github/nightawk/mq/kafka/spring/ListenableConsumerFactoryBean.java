package com.github.nightawk.mq.kafka.spring;

import com.github.nightawk.core.util.Codec;
import com.github.nightawk.mq.kafka.ListenableConsumer;
import com.github.nightawk.mq.kafka.ListenableTracingConsumer;
import com.github.nightawk.mq.kafka.PayloadListener;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class ListenableConsumerFactoryBean implements FactoryBean<ListenableConsumer>, InitializingBean {

    private String[] topics;

    private String topicPatternString;

    private PayloadListener payloadListener;

    private Map<String, Object> configs;

    private ListenableConsumer listenableConsumer;

    public void setTopics(String[] topics) {
        this.topics = topics;
    }

    public void setTopicPatternString(String topicPatternString) {
        this.topicPatternString = topicPatternString;
    }

    public void setPayloadListener(PayloadListener payloadListener) {
        this.payloadListener = payloadListener;
    }

    public void setConfigs(Map<String, Object> configs) {
        this.configs = configs;
    }

    @Override
    public ListenableConsumer getObject() throws Exception {
        if (listenableConsumer == null) {
            afterPropertiesSet();
        }
        return listenableConsumer;
    }

    @Override
    public Class<?> getObjectType() {
        return listenableConsumer == null ? ListenableConsumer.class : listenableConsumer.getClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() throws Exception {
        if (topics == null && topicPatternString == null) {
            throw new IllegalArgumentException("topic info must not be null");
        }
        Assert.notEmpty(configs, "configs must not be null");
        Assert.notNull(payloadListener, "payloadListener must be null");
        String valueDeserializerKlass = (String) configs.get("value.deserializer");
        configs.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        Consumer<String, byte[]> consumer = new KafkaConsumer<>(configs);
        if (topics != null) {
            listenableConsumer =
                    new ListenableTracingConsumer<>(consumer, Arrays.asList(topics), createDeserializer(valueDeserializerKlass));
        } else {
            listenableConsumer =
                    new ListenableTracingConsumer<>(consumer, Pattern.compile(topicPatternString), createDeserializer(valueDeserializerKlass));
        }
        if (payloadListener != null) {
            listenableConsumer.addListener(payloadListener);
        }
        listenableConsumer.start();
    }

    private Deserializer createDeserializer(String klass) throws Exception {
        Class<?> clz = Class.forName(klass);
        return (Deserializer) clz.newInstance();
    }
}
