package com.nightawk.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class SecondServiceMain {
    public static void main(String[] args) throws IOException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("appContext-service2.xml");
        context.start();
        System.in.read();
    }
}
