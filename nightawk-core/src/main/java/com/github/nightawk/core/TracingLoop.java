package com.github.nightawk.core;

public interface TracingLoop {

    TracingLoop DEFAULT_LOOP = new ThreadLocalTracingLoop();

    boolean inTracingLoop();

    void joinTracingLoop();

    void leaveTracingLoop();
}
