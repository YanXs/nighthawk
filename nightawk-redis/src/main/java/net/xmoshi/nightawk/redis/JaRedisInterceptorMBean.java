package net.xmoshi.nightawk.redis;

import com.github.kristofa.brave.ClientTracer;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Xs.
 */
public class JaRedisInterceptorMBean implements Closeable {

    public JaRedisInterceptorMBean(final ClientTracer tracer) {
        JaRedisInterceptor.setClientTracer(tracer);
    }

    @Override
    public void close() throws IOException {
        JaRedisInterceptor.setClientTracer(null);
    }
}
