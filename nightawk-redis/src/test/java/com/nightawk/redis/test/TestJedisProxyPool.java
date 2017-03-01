package com.nightawk.redis.test;

import com.nightawk.redis.JedisProxyPool;
import org.junit.Assert;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

public class TestJedisProxyPool {

    @Test
    public void test_jedis_proxy() {
        JedisPoolConfig config = new JedisPoolConfig();
        JedisProxyPool proxyPool = new JedisProxyPool(config, "127.0.0.1", 6379);
        Jedis jedis = proxyPool.getResource();
        jedis.set("hello", "world");
        Assert.assertEquals(jedis.get("hello"), "world");

        jedis.toString();
        jedis.close();
    }

    @Test
    public void test_jedis_tracer() {

    }
}
