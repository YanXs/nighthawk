package com.github.nightawk.redis;

import com.github.nightawk.core.ClientTracerSupport;
import com.github.nightawk.core.intercept.ByteBuddyInterceptor;
import net.bytebuddy.implementation.bind.annotation.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.concurrent.Callable;

/**
 * @author Xs.
 */
public class JaRedisInterceptor extends ClientTracerSupport implements ByteBuddyInterceptor {


    @RuntimeType
    public Object intercept(@SuperCall Callable<?> superMethod, @Origin Method method, @AllArguments Object[] args, @This Object me) {
        beginTrace((Jedis) me, method);
        Throwable error = null;
        try {
            return superMethod.call();
        } catch (Throwable t) {
            error = t;
            throw new JaRedisCallException("Call superMethod error.", t);
        } finally {
            endTrace(error);
        }
    }

    private void beginTrace(Jedis jedis, Method method) {
        if (isTraceEnabled()) {
            try {
                InetAddress address = Inet4Address.getByName(jedis.getClient().getHost());
                int port = jedis.getClient().getPort();
                String serviceName = "redis-DB-" + jedis.getClient().getDB();
                Beginning beginning = new Beginning();
                beginning.setSpanName(serviceName);
                beginning.setAddress(address);
                beginning.setPort(port);
                beginning.addAnnotation("execute.command", method.getName());
                beginTrace(beginning);
            } catch (Exception e) {
                setClientSent();
            }
        }
    }

    private void endTrace(Throwable throwable) {
        if (isTraceEnabled()) {
            Ending ending = new Ending();
            if (throwable != null) {
                ending.addAnnotation("error", ExceptionUtils.getStackTrace(throwable));
            }
            endTrace(ending);
        }
    }
}
