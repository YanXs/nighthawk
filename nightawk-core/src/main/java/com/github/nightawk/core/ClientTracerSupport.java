package com.github.nightawk.core;

import com.github.kristofa.brave.ClientTracer;
import com.github.kristofa.brave.SpanId;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public abstract class ClientTracerSupport implements TracingLoop {

    private TracingLoop tracingLoop = TracingLoop.DEFAULT_LOOP;

    private static final AtomicReference<ClientTracer> reference = new AtomicReference<>(null);

    public static void setClientTracer(final ClientTracer tracer) {
        reference.set(tracer);
    }

    protected static ClientTracer getClientTracer() {
        return reference.get();
    }

    public void setTracingLoop(TracingLoop loop) {
        this.tracingLoop = loop;
    }

    public boolean isTraceEnabled() {
        return getClientTracer() != null;
    }

    protected void beginTrace(Beginning beginning) {
        ClientTracer clientTracer = getClientTracer();
        if (clientTracer != null) {
            try {
                SpanId spanId = clientTracer.startNewSpan(beginning.spanName);
                if (spanId != null) {
                    joinTracingLoop();
                    submitBinaryAnnotation(clientTracer, beginning.annotations);
                    setClientSent(clientTracer, beginning.address, beginning.port, beginning.spanName);
                }
            } catch (Exception e) {
                if (inTracingLoop()) {
                    clientTracer.setClientSent();
                }
            }
        }
    }

    private void setClientSent(ClientTracer clientTracer, InetAddress address, int port, String serviceName) {
        int ipv4 = ByteBuffer.wrap(address.getAddress()).getInt();
        clientTracer.setClientSent(ipv4, port, serviceName);
    }

    protected void setClientSent() {
        if (inTracingLoop()) {
            ClientTracer clientTracer = getClientTracer();
            if (clientTracer != null) {
                clientTracer.setClientSent();
            }
        }
    }

    protected void endTrace(Ending ending) {
        try {
            ClientTracer clientTracer = getClientTracer();
            if (clientTracer != null && inTracingLoop()) {
                try {
                    submitBinaryAnnotation(clientTracer, ending.annotations);
                } finally {
                    clientTracer.setClientReceived();
                }
            }
        } finally {
            if (inTracingLoop()) {
                leaveTracingLoop();
            }
        }
    }

    private void submitBinaryAnnotation(ClientTracer clientTracer, Map<String, String> annotations) {
        for (Map.Entry<String, String> entry : annotations.entrySet()) {
            clientTracer.submitBinaryAnnotation(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean inTracingLoop() {
        return tracingLoop.inTracingLoop();
    }

    @Override
    public void joinTracingLoop() {
        tracingLoop.joinTracingLoop();
    }

    @Override
    public void leaveTracingLoop() {
        tracingLoop.leaveTracingLoop();
    }

    public static class Beginning {
        private String spanName;
        private InetAddress address;
        private int port;
        private Map<String, String> annotations = new HashMap<>();


        public void addAnnotation(String key, String value) {
            annotations.put(key, value);
        }

        public void setSpanName(String spanName) {
            this.spanName = spanName;
        }

        public InetAddress getAddress() {
            return address;
        }

        public void setAddress(InetAddress address) {
            this.address = address;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    public static class Ending {

        private Map<String, String> annotations = new HashMap<>();

        public void addAnnotation(String key, String value) {
            annotations.put(key, value);
        }

    }
}
