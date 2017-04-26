package net.xmoshi.nightawk.jdbc.basic;

import net.xmoshi.nightawk.jdbc.Interceptor;
import net.xmoshi.nightawk.jdbc.StatementTracer;

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
