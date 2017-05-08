package com.github.nightawk.mq.kafka.spring;

import com.github.nightawk.core.util.Codec;
import com.github.nightawk.mq.kafka.ListenableConsumer;
import com.github.nightawk.mq.kafka.ListenableTracingConsumer;
import com.github.nightawk.mq.kafka.PayloadListener;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class ListenableConsumerFactoryBean implements FactoryBean<ListenableConsumer>, InitializingBean {

    private PayloadListener payloadListener;

    private Map<String, Object> configs;

    private ListenableConsumer listenableConsumer;

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
    public void afterPropertiesSet() throws Exception {
        Assert.notEmpty(configs, "configs must not be null");
        Assert.notNull(payloadListener, "payloadListener must be null");
        configs.put("value.deserializer.tracing.codec", Codec.JSON);
        Consumer<String, byte[]> consumer = new KafkaConsumer<>(configs);
        listenableConsumer =
                new ListenableTracingConsumer<>(consumer, Pattern.compile("test"), new StringDeserializer());
        if(payloadListener != null){
            listenableConsumer.addListener(payloadListener);
        }

        listenableConsumer.start();
    }
}
