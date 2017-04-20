package net.nightawk.sphex.basic;

import net.nightawk.sphex.StatementTracer;

import java.sql.SQLException;

public class BasicTracingInterceptor implements Interceptor {

    private StatementTracer statementTracer;

    public BasicTracingInterceptor() {
    }

    public BasicTracingInterceptor(StatementTracer statementTracer) {
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
