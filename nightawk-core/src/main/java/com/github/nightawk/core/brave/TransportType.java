package com.github.nightawk.core.brave;

/**
 * @author Xs.
 */
public enum TransportType {

    Http("http"),

    Kafka("kafka"),

    Scribe("scribe");

    private String value;

    TransportType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
