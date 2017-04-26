package net.xmoshi.nightawk.test.service;

import net.xmoshi.nightawk.dubbo.TraceIdWatcher;
import net.xmoshi.nightawk.redis.JaRedisPool;
import net.xmoshi.nightawk.test.entity.Employee;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.UUID;

public class FirstServiceImpl implements FirstService {

    private TraceIdWatcher traceIdWatcher;

    private JaRedisPool jedisProxyPool;

    private SecondService secondService;

    public void setTraceIdWatcher(TraceIdWatcher traceIdWatcher) {
        this.traceIdWatcher = traceIdWatcher;
    }

    public void setJedisProxyPool(JaRedisPool jedisProxyPool) {
        this.jedisProxyPool = jedisProxyPool;
    }

    public void setSecondService(SecondService secondService) {
        this.secondService = secondService;
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
        return secondService.getEmployee(id);
    }

    @Override
    public List<Employee> getEmployees() {
        return null;
    }
}
