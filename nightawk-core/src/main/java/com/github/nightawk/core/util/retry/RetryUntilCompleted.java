package com.github.nightawk.core.util.retry;

public class RetryUntilCompleted extends RetryNTimes {

    public RetryUntilCompleted(int sleepMsBetweenRetries) {
        super(Integer.MAX_VALUE, sleepMsBetweenRetries);
    }
}
