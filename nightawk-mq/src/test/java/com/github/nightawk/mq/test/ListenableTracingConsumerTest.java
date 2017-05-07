package com.github.nightawk.mq.test;

import com.github.kristofa.brave.Brave;
import com.github.nightawk.core.brave.BraveFactoryBean;
import com.github.nightawk.core.util.Codec;
import com.github.nightawk.core.util.Sleeper;
import com.github.nightawk.mq.kafka.AbstractTracingListener;
import com.github.nightawk.mq.kafka.ListenableTracingConsumer;
import com.github.nightawk.mq.kafka.Payload;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.Test;

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class ListenableTracingConsumerTest {

    @Test
    public void test() throws Exception {
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
        ListenableTracingConsumer<String, String> listenableTracingConsumer =
                new ListenableTracingConsumer<>(consumer, Pattern.compile("test"), new StringDeserializer());
        BraveFactoryBean factoryBean = new BraveFactoryBean();
        factoryBean.setServiceName("kafka-test");
        factoryBean.setTransport("http");
        factoryBean.setTransportAddress("192.168.150.133:9411");
        factoryBean.afterPropertiesSet();
        Brave brave = factoryBean.getObject();
        listenableTracingConsumer.addListener(new AbstractTracingListener<String, String>(brave) {
            @Override
            public void onPayload(Payload<String, String> payload) {
                try {
                    Sleeper.JUST_SLEEP.sleepFor(2000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        listenableTracingConsumer.start();
        System.in.read();
    }
}
