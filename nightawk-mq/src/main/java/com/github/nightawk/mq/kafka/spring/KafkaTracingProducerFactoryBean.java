package com.github.nightawk.mq.kafka.spring;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.Map;

public class KafkaTracingProducerFactoryBean implements FactoryBean<Producer>, InitializingBean {

    private Producer producer;

    private Map<String, Object> configs;

    public void setConfigs(Map<String, Object> configs) {
        this.configs = configs;
    }

    @Override
    public Producer getObject() throws Exception {
        if (producer == null) {
            afterPropertiesSet();
        }
        return producer;
    }

    @Override
    public Class<?> getObjectType() {
        return producer == null ? Producer.class : producer.getClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notEmpty(configs, "kafka producer must not be null");
        producer = new KafkaProducer<>(configs);
    }
}
