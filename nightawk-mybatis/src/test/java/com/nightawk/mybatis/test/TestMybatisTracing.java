package com.nightawk.mybatis.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:appContext-mybatis-tracing.xml"})
public class TestMybatisTracing {

    @Autowired
    private EmployeeDao employeeDao;

    @Test
    public void test_query_tracing() throws Exception {
        try {
            Employee employee = employeeDao.getEmployee(123);
        }catch (Exception ignored){
        }
        Thread.sleep(2000);
    }

    @Test
    public void test_insert_tracing() {

    }
}
