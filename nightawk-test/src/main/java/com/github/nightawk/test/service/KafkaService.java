package com.github.nightawk.test.service;

import com.github.kristofa.brave.Brave;
import com.github.nightawk.core.util.Sleeper;
import com.github.nightawk.mq.kafka.AbstractTracingListener;
import com.github.nightawk.mq.kafka.Payload;
import com.github.nightawk.redis.JaRedisPool;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class KafkaService extends AbstractTracingListener {

    private JaRedisPool jedisProxyPool;

    public KafkaService(Brave brave) {
        super(brave);
    }

    public void setJedisProxyPool(JaRedisPool jedisProxyPool) {
        this.jedisProxyPool = jedisProxyPool;
    }

    @Override
    public void onPayload(Payload payload) {
        try {
            Sleeper.JUST.sleepFor(1000, TimeUnit.MILLISECONDS);
            System.out.println(payload.record().value());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Jedis jedis = jedisProxyPool.getResource();
        jedis.append(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
