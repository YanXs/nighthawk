package com.github.nightawk.test;

import com.github.nightawk.test.entity.Employee;
import com.github.nightawk.test.service.FirstService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class Consumer {

    public static void main(String[] args) throws IOException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("appContext-consumer.xml");
        context.start();
        FirstService firstService = (FirstService) context.getBean("service1");
        for (int i = 0; i < 100; i++) {
            Employee employee = firstService.getEmployee(1);
        }
        System.in.read();
    }
}
