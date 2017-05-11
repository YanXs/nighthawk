package com.github.nightawk.test;

import com.github.nightawk.core.util.Sleeper;
import com.github.nightawk.test.entity.Employee;
import com.github.nightawk.test.service.FirstService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Consumer {

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("appContext-consumer.xml");
        context.start();
        FirstService firstService = (FirstService) context.getBean("service1");
        for (int i = 0; i < 10000; i++) {
            Employee employee = firstService.getEmployee(1);
            Sleeper.JUST.sleepFor(500, TimeUnit.MILLISECONDS);
        }
        System.in.read();
    }
}
