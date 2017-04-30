package com.github.nightawk.core.util;

/**
 * @author xs.
 */
public enum  Headers {

    TraceId("X-B3-TraceId"),

    SpanId("X-B3-SpanId"),

    ParentSpanId("X-B3-ParentSpanId"),

    Sampled("X-B3-Sampled"),

    SpanName("X-B3-SpanName");

    private final String name;

    Headers(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
