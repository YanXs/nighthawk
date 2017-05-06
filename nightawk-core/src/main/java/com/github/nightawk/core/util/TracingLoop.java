package com.github.nightawk.core.util;

public interface TracingLoop {

    TracingLoop DEFAULT_LOOP = new ThreadLocalTracingLoop();

    boolean inTracingLoop();

    void joinTracingLoop();

    void leaveTracingLoop();
}
