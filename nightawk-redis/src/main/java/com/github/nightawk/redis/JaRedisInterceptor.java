package com.github.nightawk.redis;

import com.github.kristofa.brave.ClientTracer;
import com.github.kristofa.brave.SpanId;
import com.github.nightawk.core.intercept.ByteBuddyInterceptor;
import com.github.nightawk.core.util.TracingLoop;
import net.bytebuddy.implementation.bind.annotation.*;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Xs.
 */
public class JaRedisInterceptor implements ByteBuddyInterceptor, TracingLoop {

    private static final AtomicReference<ClientTracer> reference = new AtomicReference<>(null);

    private final TracingLoop delegate;

    public JaRedisInterceptor() {
        delegate = TracingLoop.DEFAULT_LOOP;
    }

    public static void setClientTracer(final ClientTracer tracer) {
        reference.set(tracer);
    }

    private static ClientTracer getClientTracer() {
        return reference.get();
    }

    @RuntimeType
    public Object intercept(@SuperCall Callable<?> superMethod, @Origin Method method, @AllArguments Object[] args, @This Object me) {
        ClientTracer clientTracer = getClientTracer();
        if (clientTracer != null) {
            beginTrace(getClientTracer(), (Jedis) me, method);
        }
        Exception exception = null;
        try {
            return superMethod.call();
        } catch (Exception e) {
            exception = e;
            throw new JaRedisCallException("Call superMethod error.", e);
        } finally {
            if (getClientTracer() != null && inTracingLoop()) {
                endTrace(getClientTracer(), exception);
            }
        }
    }

    private void beginTrace(final ClientTracer tracer, Jedis jedis, Method method) {
        try {
            SpanId spanId = tracer.startNewSpan("redis");
            if (spanId != null) {
                joinTracingLoop();
                tracer.submitBinaryAnnotation("execute.command", method.getName());
                setClientSent(tracer, jedis);
            }
        } catch (Exception e) {
            if (inTracingLoop()) {
                tracer.setClientSent();
            }
        }
    }

    private void setClientSent(ClientTracer clientTracer, Jedis jedis) throws Exception {
        InetAddress address = Inet4Address.getByName(jedis.getClient().getHost());
        int ipv4 = ByteBuffer.wrap(address.getAddress()).getInt();
        int port = jedis.getClient().getPort();
        String serviceName = "redis-DB-" + jedis.getClient().getDB();
        clientTracer.setClientSent(ipv4, port, serviceName);
    }

    private void endTrace(final ClientTracer tracer, Exception exception) {
        try {
            if (exception != null) {
                tracer.submitBinaryAnnotation("exception", exception.getMessage());
            }
        } finally {
            tracer.setClientReceived();
        }
    }

    @Override
    public boolean inTracingLoop() {
        return delegate.inTracingLoop();
    }

    @Override
    public void joinTracingLoop() {
        delegate.joinTracingLoop();
    }

    @Override
    public void leaveTracingLoop() {
        delegate.leaveTracingLoop();
    }
}
