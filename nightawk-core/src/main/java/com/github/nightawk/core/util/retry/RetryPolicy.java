package com.github.nightawk.core.util.retry;

import com.github.nightawk.core.util.Sleeper;

public interface RetryPolicy {

    boolean allowRetry(int retryCount, long elapsedTimeMs, Sleeper sleeper);

}
