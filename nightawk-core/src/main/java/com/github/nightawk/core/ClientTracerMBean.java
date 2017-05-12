package com.github.nightawk.core;

import com.github.kristofa.brave.ClientTracer;

import java.io.Closeable;
import java.io.IOException;

public class ClientTracerMBean implements Closeable {

    public ClientTracerMBean(final ClientTracer tracer) {
        ClientTracerSupport.setClientTracer(tracer);
    }

    @Override
    public void close() throws IOException {
        ClientTracerSupport.setClientTracer(null);
    }
}
