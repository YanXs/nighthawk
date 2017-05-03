package com.github.nightawk.mq.test;

import com.github.nightawk.core.util.Codec;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Test;

import java.util.Properties;

public class KafkaTracingDeserializerTest {

    @Test
    public void test_create_deserializer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "127.0.0.1:9092");//该地址是集群的子集，用来探测集群。
        props.put("acks", "all");// 记录完整提交，最慢的但是最大可能的持久化
        props.put("retries", 3);// 请求失败重试的次数
        props.put("batch.size", 16384);// batch的大小
        props.put("linger.ms", 1);// 默认情况即使缓冲区有剩余的空间，也会立即发送请求，设置一段时间用来等待从而将缓冲区填的更多，单位为毫秒，producer发送数据会延迟1ms，可以减少发送到kafka服务器的请求数据
        props.put("buffer.memory", 33554432);// 提供给生产者缓冲内存总量
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer.tracing.codec", Codec.JSON);
        props.put("value.deserializer", "com.github.nightawk.mq.kafka.TracingDeserializer");
        Consumer<String, byte[]> consumer = new KafkaConsumer<>(props);
        ConsumerRecords<String, byte[]> records = consumer.poll(2000);
        for (ConsumerRecord<String, byte[]> record : records) {
            byte[] value = record.value();
        }
    }
}
