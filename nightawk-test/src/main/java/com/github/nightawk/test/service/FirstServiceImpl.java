package com.github.nightawk.test.service;

import com.github.nightawk.dubbo.TraceIdWatcher;
import com.github.nightawk.redis.JaRedisPool;
import com.github.nightawk.test.entity.Employee;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.UUID;

public class FirstServiceImpl implements FirstService {

    private TraceIdWatcher traceIdWatcher;

    private JaRedisPool jedisProxyPool;

    private SecondService secondService;

    private Producer<String, byte[]> kafkaProducer;


    public void setTraceIdWatcher(TraceIdWatcher traceIdWatcher) {
        this.traceIdWatcher = traceIdWatcher;
    }

    public void setJedisProxyPool(JaRedisPool jedisProxyPool) {
        this.jedisProxyPool = jedisProxyPool;
    }

    public void setSecondService(SecondService secondService) {
        this.secondService = secondService;
    }


    public void setKafkaProducer(Producer<String, byte[]> kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public Employee getEmployee(Integer id) {
        System.out.println(traceIdWatcher.getTraceId().get());
        Jedis jedis = jedisProxyPool.getResource();
        jedis.append(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //kafka
        kafkaProducer.send(new ProducerRecord<>("test", "hello", ("kafka - " + System.currentTimeMillis()).getBytes()));
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return secondService.getEmployee(id);
    }

    @Override
    public List<Employee> getEmployees() {
        return null;
    }
}
