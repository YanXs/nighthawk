package com.nightawk.redis;

import com.github.kristofa.brave.ClientTracer;
import com.nightawk.core.intercept.ByteBuddyInterceptor;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
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

    public Object intercept(@SuperCall Callable<?> superMethod, @Origin Method method, @AllArguments Object[] args, @This Object me) {
        if (!(superMethod instanceof Jedis)) {
            throw new IllegalStateException("superMethod must be subClass of Jedis");
        }
        beginTrace(getClientTracer(), superMethod, method);
        Exception exception = null;
        try {
            return superMethod.call();
        } catch (Exception e) {
            exception = e;
        } finally {
            endTrace(getClientTracer(), exception);
        }
        return null;
    }

    private void beginTrace(final ClientTracer tracer, Callable<?> superMethod, Method method) {
        tracer.startNewSpan("execute.redis.command");
        tracer.submitBinaryAnnotation("executed.command", method.getName());
        try {
            setClientSent(tracer, (Jedis) superMethod);
        } catch (Exception e) {
            tracer.setClientSent();
        }
    }

    private void setClientSent(ClientTracer clientTracer, Jedis jedis) throws Exception {
        InetAddress address = Inet4Address.getByName(jedis.getClient().getHost());
        int ipv4 = ByteBuffer.wrap(address.getAddress()).getInt();
        int port = jedis.getClient().getPort();
        String serviceName = "redis" + jedis.getClient().getDB();
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
