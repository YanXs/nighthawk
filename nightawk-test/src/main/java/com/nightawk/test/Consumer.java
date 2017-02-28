package com.nightawk.test;

import com.nightawk.test.entity.Employee;
import com.nightawk.test.service.FirstService;
import com.nightawk.test.service.FirstServiceImpl;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class Consumer {

    public static void main(String[] args) throws IOException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("appContext-consumer.xml");
        context.start();
        FirstService firstService = (FirstService) context.getBean("service1");
        Employee employee = firstService.getEmployee(1);
        System.out.println(employee.getName());
        System.in.read();
    }
}
