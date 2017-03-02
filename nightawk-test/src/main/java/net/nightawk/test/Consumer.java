package net.nightawk.test;

import net.nightawk.test.entity.Employee;
import net.nightawk.test.service.FirstService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class Consumer {

    public static void main(String[] args) throws IOException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("appContext-consumer.xml");
        context.start();
        FirstService firstService = (FirstService) context.getBean("service1");
        for (int i = 0; i < 10000; i++) {
            Employee employee = firstService.getEmployee(1);
        }
        System.in.read();
    }
}
