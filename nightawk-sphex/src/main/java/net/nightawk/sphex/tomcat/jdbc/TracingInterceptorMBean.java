package net.nightawk.sphex.tomcat.jdbc;

import net.nightawk.sphex.StatementTracer;

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
