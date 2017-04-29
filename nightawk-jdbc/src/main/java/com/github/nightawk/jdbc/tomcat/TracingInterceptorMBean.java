package com.github.nightawk.jdbc.tomcat;

import com.github.nightawk.jdbc.StatementTracer;

/**
 * @author Xs
 */
public class TracingInterceptorMBean {

    public TracingInterceptorMBean(StatementTracer tracer) {
        TomcatDataSourceTracingInterceptor.setStatementTracer(tracer);
    }

    public void close() {
        TomcatDataSourceTracingInterceptor.setStatementTracer(null);
    }
}
