package net.nightawk.redis;

import com.github.kristofa.brave.ClientTracer;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author Xs.
 */
public class JedisInterceptorManagementBean implements Closeable {

    public JedisInterceptorManagementBean(final ClientTracer tracer) {
        JedisInterceptor.setClientTracer(tracer);
    }

    @Override
    public void close() throws IOException {
        JedisInterceptor.setClientTracer(null);
    }
}
