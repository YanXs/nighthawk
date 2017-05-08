package com.github.nightawk.test.service;

import com.github.kristofa.brave.Brave;
import com.github.nightawk.core.util.Sleeper;
import com.github.nightawk.mq.kafka.AbstractTracingListener;
import com.github.nightawk.mq.kafka.Payload;

import java.util.concurrent.TimeUnit;

public class KafkaService extends AbstractTracingListener {

    public KafkaService(Brave brave) {
        super(brave);
    }

    @Override
    public void onPayload(Payload payload) {
        try {
            Sleeper.JUST_SLEEP.sleepFor(2000, TimeUnit.MILLISECONDS);
            System.out.println("finished...");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
