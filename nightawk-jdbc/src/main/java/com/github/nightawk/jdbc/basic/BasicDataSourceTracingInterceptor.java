package com.github.nightawk.jdbc.basic;

import com.github.nightawk.jdbc.Interceptor;
import com.github.nightawk.jdbc.StatementTracer;

import java.sql.SQLException;

public class BasicDataSourceTracingInterceptor implements Interceptor {

    private StatementTracer statementTracer;

    public BasicDataSourceTracingInterceptor() {
    }

    public BasicDataSourceTracingInterceptor(StatementTracer statementTracer) {
        this.statementTracer = statementTracer;
    }

    public void setStatementTracer(StatementTracer statementTracer) {
        this.statementTracer = statementTracer;
    }

    @Override
    public Object intercept(Chain chain) throws SQLException {
        statementTracer.beginTrace(chain.sql(), chain.url());
        Object retVal = null;
        Throwable error = null;
        try {
            retVal = chain.proceed();
        } catch (SQLException e) {
            error = e;
            throw e;
        } finally {
            statementTracer.endTrace(0, error);
        }
        return retVal;
    }
}
