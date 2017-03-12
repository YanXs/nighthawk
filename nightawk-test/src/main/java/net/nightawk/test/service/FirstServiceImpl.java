package net.nightawk.test.service;

import net.nightawk.redis.JaRedisPool;
import net.nightawk.test.entity.Employee;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

public class FirstServiceImpl implements FirstService {

    private JaRedisPool jedisProxyPool;

    private SecondService secondService;

    public void setJedisProxyPool(JaRedisPool jedisProxyPool) {
        this.jedisProxyPool = jedisProxyPool;
    }

    public void setSecondService(SecondService secondService) {
        this.secondService = secondService;
    }

    @Override
    public Employee getEmployee(Integer id) {
        Jedis jedis = jedisProxyPool.getResource();
        jedis.hgetAll(String.valueOf(id));
        return secondService.getEmployee(id);
    }

    @Override
    public List<Employee> getEmployees() {
        return null;
    }
}
