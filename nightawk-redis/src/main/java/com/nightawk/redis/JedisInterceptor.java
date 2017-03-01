package com.nightawk.redis;

import com.github.kristofa.brave.ClientTracer;
import com.nightawk.core.intercept.ByteBuddyInterceptor;
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
public class JedisInterceptor implements ByteBuddyInterceptor {

    private static final AtomicReference<ClientTracer> reference = new AtomicReference<>(null);

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
        } finally {
            if (getClientTracer() != null) {
                endTrace(getClientTracer(), exception);
            }
        }
        return null;
    }

    private void beginTrace(final ClientTracer tracer, Jedis jedis, Method method) {
        tracer.startNewSpan("redis.command");
        tracer.submitBinaryAnnotation("executed.command", method.getName());
        try {
            setClientSent(tracer, jedis);
        } catch (Exception e) {
            tracer.setClientSent();
        }
    }

    private void setClientSent(ClientTracer clientTracer, Jedis jedis) throws Exception {
        InetAddress address = Inet4Address.getByName(jedis.getClient().getHost());
        int ipv4 = ByteBuffer.wrap(address.getAddress()).getInt();
        int port = jedis.getClient().getPort();
        String serviceName = "redis-DB" + jedis.getClient().getDB();
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
}
