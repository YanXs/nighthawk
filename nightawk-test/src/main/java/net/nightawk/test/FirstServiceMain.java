package net.nightawk.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class FirstServiceMain {
    public static void main(String[] args) throws IOException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("appContext-service1.xml");
        context.start();
        System.in.read();
    }
}
