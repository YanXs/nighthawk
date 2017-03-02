package com.nightawk.mysql;

import com.github.kristofa.brave.ClientTracer;

import java.io.Closeable;
import java.io.IOException;

/**
 * copy from brave-jdbc
 *
 * @author Xs
 */
public class PreparedStatementInterceptorManagementBean implements Closeable {

    public PreparedStatementInterceptorManagementBean(final ClientTracer tracer) {
        PreparedStatementInterceptor.setClientTracer(tracer);
    }

    @Override
    public void close() throws IOException {
        PreparedStatementInterceptor.setClientTracer(null);
    }
}
