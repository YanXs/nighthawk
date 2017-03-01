package com.nightawk.redis.test;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.EmptySpanCollectorMetricsHandler;
import com.github.kristofa.brave.Sampler;
import com.github.kristofa.brave.http.HttpSpanCollector;
import com.nightawk.redis.JedisInterceptor;
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
        jedis.close();
    }

    @Test
    public void test_jedis_tracer() throws Exception {
        Brave.Builder builder = new Brave.Builder("jedis-interceptor-test");
        builder.spanCollector(HttpSpanCollector.create("http://192.168.150.132:9411", new EmptySpanCollectorMetricsHandler()));
        builder.traceSampler(Sampler.ALWAYS_SAMPLE);
        Brave brave = builder.build();
        JedisInterceptor.setClientTracer(brave.clientTracer());

        JedisPoolConfig config = new JedisPoolConfig();
        JedisProxyPool proxyPool = new JedisProxyPool(config, "127.0.0.1", 6379);
        Jedis jedis = proxyPool.getResource();
        jedis.set("hello", "world");
        Assert.assertEquals(jedis.get("hello"), "world");
        jedis.hgetAll("hello-map");
        jedis.close();
        // sleep 3s in case spanCollector not flushed
        Thread.sleep(3000);
    }
}
