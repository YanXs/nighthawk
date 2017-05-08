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

import java.util.Properties;
import java.util.regex.Pattern;

public class ListenableConsumerFactoryBean implements FactoryBean<ListenableConsumer>, InitializingBean {


    private PayloadListener payloadListener;

    private ListenableConsumer listenableConsumer;

    public void setPayloadListener(PayloadListener payloadListener) {
        this.payloadListener = payloadListener;
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
        Properties props = new Properties();
        props.put("bootstrap.servers", "127.0.0.1:9092");//该地址是集群的子集，用来探测集群。
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group");
        props.put("acks", "all");// 记录完整提交，最慢的但是最大可能的持久化
        props.put("retries", 3);// 请求失败重试的次数
        props.put("batch.size", 16384);// batch的大小
        props.put("linger.ms", 1);// 默认情况即使缓冲区有剩余的空间，也会立即发送请求，设置一段时间用来等待从而将缓冲区填的更多，单位为毫秒，producer发送数据会延迟1ms，可以减少发送到kafka服务器的请求数据
        props.put("buffer.memory", 33554432);// 提供给生产者缓冲内存总量
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer.tracing.codec", Codec.JSON);
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");
        Consumer<String, byte[]> consumer = new KafkaConsumer<>(props);
        listenableConsumer =
                new ListenableTracingConsumer<>(consumer, Pattern.compile("test"), new StringDeserializer());
        if(payloadListener != null){
            listenableConsumer.addListener(payloadListener);
        }

        listenableConsumer.start();
    }
}
