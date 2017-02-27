package com.nightawk.mysql;

import com.github.kristofa.brave.ClientTracer;

import java.io.Closeable;
import java.io.IOException;

/**
 * copy from brave-mysql
 *
 * @author Xs
 */
public class PreparedStatementInterceptorManagementBean implements Closeable {
    /**
     * 慢查询时间
     */
    private Long longQueryMs = 0L;

    public void setLongQueryMs(Long longQueryMs) {
        this.longQueryMs = longQueryMs;
    }

    public PreparedStatementInterceptorManagementBean(final ClientTracer tracer) {
        PreparedStatementInterceptor.setClientTracer(tracer);
        PreparedStatementInterceptor.setLongQueryMs(longQueryMs);
    }

    @Override
    public void close() throws IOException {
        PreparedStatementInterceptor.setClientTracer(null);
    }
}
