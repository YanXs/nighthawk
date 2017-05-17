package com.github.nightawk.core.util.retry;

import com.github.nightawk.core.util.Sleeper;

import java.util.concurrent.TimeUnit;

public class DelayRetryPolicy extends RetryUntilElapsed {

    private volatile boolean started;

    private final int delay;

    public DelayRetryPolicy(int delay, int maxElapsedTimeMs, int sleepMsBetweenRetries) {
        super(maxElapsedTimeMs, sleepMsBetweenRetries);
        this.delay = delay;
    }

    @Override
    public boolean allowRetry(int retryCount, long elapsedTimeMs, Sleeper sleeper) {
        if (!started) {
            try {
                sleeper.sleepFor(delay, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
            started = true;
        }
        return super.allowRetry(retryCount, elapsedTimeMs, sleeper);
    }
}
