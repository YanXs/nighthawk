package com.github.nightawk.mq.kafka.spring;

import com.github.kristofa.brave.Brave;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Properties;

public class KafkaProducerFactoryBean implements FactoryBean<Producer>, InitializingBean {

    private Brave brave;

    private Producer producer;

    public void setBrave(Brave brave) {
        this.brave = brave;
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
        Properties props = new Properties();
        props.put("bootstrap.servers", "127.0.0.1:9092");//该地址是集群的子集，用来探测集群。
        props.put("acks", "all");// 记录完整提交，最慢的但是最大可能的持久化
        props.put("retries", 3);// 请求失败重试的次数
        props.put("batch.size", 16384);// batch的大小
        props.put("linger.ms", 1);// 默认情况即使缓冲区有剩余的空间，也会立即发送请求，设置一段时间用来等待从而将缓冲区填的更多，单位为毫秒，producer发送数据会延迟1ms，可以减少发送到kafka服务器的请求数据
        props.put("buffer.memory", 33554432);// 提供给生产者缓冲内存总量
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("tracing.component", brave);
        props.put("value.serializer", "com.github.nightawk.mq.kafka.TracingSerializer");
        producer = new KafkaProducer<>(props);
    }
}
