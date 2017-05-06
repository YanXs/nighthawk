package com.github.nightawk.core.util;

public class ThreadLocalTracingLoop implements TracingLoop {

    public static final ThreadLocal<Boolean> tracingLoop = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    public boolean inTracingLoop() {
        return tracingLoop.get();
    }

    public void joinTracingLoop() {
        tracingLoop.set(Boolean.TRUE);
    }

    public void leaveTracingLoop() {
        tracingLoop.set(Boolean.FALSE);
    }

}
