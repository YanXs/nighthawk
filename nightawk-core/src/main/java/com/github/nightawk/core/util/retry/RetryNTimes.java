package com.github.nightawk.core.util.retry;

import com.github.nightawk.core.util.Sleeper;

import java.util.concurrent.TimeUnit;

public class RetryNTimes implements RetryPolicy {

    private final int n;

    private final int sleepMsBetweenRetries;

    public RetryNTimes(int n, int sleepMsBetweenRetries) {
        this.n = n;
        this.sleepMsBetweenRetries = sleepMsBetweenRetries;
    }

    public int getN() {
        return n;
    }

    public int getSleepMs() {
        return sleepMsBetweenRetries;
    }

    @Override
    public boolean allowRetry(int retryCount, long elapsedTimeMs, Sleeper sleeper) {
        if (retryCount < n) {
            try {
                sleeper.sleepFor(getSleepMs(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            return true;
        }
        return false;
    }
}
