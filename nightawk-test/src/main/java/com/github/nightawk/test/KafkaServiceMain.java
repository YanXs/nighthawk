package com.github.nightawk.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class KafkaServiceMain {

    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("appContext-kafka.xml");
        context.start();
        System.in.read();
    }
}
