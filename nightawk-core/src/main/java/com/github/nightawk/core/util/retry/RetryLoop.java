package com.github.nightawk.core.util.retry;

import com.github.nightawk.core.util.Sleeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class RetryLoop {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final long startTimeMs = System.currentTimeMillis();

    private final RetryPolicy retryPolicy;

    private volatile boolean isDone = false;

    private int retryCount = 0;

    private static final Sleeper sleeper = Sleeper.JUST;

    RetryLoop(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public static RetryLoop create(RetryPolicy retryPolicy) {
        return new RetryLoop(retryPolicy);
    }

    public static Sleeper getDefaultRetrySleeper() {
        return sleeper;
    }

    public void callWithLoop(Callable<Boolean> proc) throws Exception {
        while (shouldRetry()) {
            try {
                boolean completed = proc.call();
                if (completed) {
                    markComplete();
                    break;
                }
            } catch (Exception e) {
                takeException(e);
            }
        }
    }

    public boolean isDone() {
        return isDone;
    }

    public <T> T callWithRetry(Callable<T> proc) throws Exception {
        T result = null;
        while (shouldContinue()) {
            try {
                result = proc.call();
                markComplete();
            } catch (Exception e) {
                takeException(e);
            }
        }
        return result;
    }


    public boolean shouldContinue() {
        return !isDone;
    }

    public boolean shouldRetry() {
        return !isDone && retryPolicy.allowRetry(retryCount++, System.currentTimeMillis() - startTimeMs, sleeper);
    }

    public void markComplete() {
        isDone = true;
    }

    // TODO
    public static boolean isRetryException(Throwable exception) {
        return exception instanceof RetryableException;
    }

    public void takeException(Exception exception) throws Exception {
        boolean rethrow = true;
        if (isRetryException(exception)) {
            LOGGER.info("Retry-able exception received", exception);
            if (retryPolicy.allowRetry(retryCount++, System.currentTimeMillis() - startTimeMs, sleeper)) {
                LOGGER.info("Retrying operation");
                rethrow = false;
            } else {
                LOGGER.info("Retry policy not allowing retry");
            }
        }
        if (rethrow) {
            throw exception;
        }
    }

    private static class RetryableException extends Exception {
    }
}
