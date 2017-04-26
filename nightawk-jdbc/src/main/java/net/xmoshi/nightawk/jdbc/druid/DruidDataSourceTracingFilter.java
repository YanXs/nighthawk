package net.xmoshi.nightawk.jdbc.druid;

import com.alibaba.druid.filter.FilterEventAdapter;
import com.alibaba.druid.proxy.jdbc.ResultSetProxy;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import net.xmoshi.nightawk.jdbc.StatementTracer;

/**
 * @author Xs.
 */
public class DruidDataSourceTracingFilter extends FilterEventAdapter {

    private StatementTracer statementTracer;

    public DruidDataSourceTracingFilter() {
    }

    public DruidDataSourceTracingFilter(StatementTracer statementTracer) {
        this.statementTracer = statementTracer;
    }

    public void setStatementTracer(StatementTracer statementTracer) {
        this.statementTracer = statementTracer;
    }

    protected void statementExecuteUpdateBefore(StatementProxy statement, String sql) {
        beginTrace(statement, sql);
    }

    protected void statementExecuteUpdateAfter(StatementProxy statement, String sql, int updateCount) {
        executeSucceeded();
    }

    protected void statementExecuteQueryBefore(StatementProxy statement, String sql) {
        beginTrace(statement, sql);
    }

    protected void statementExecuteQueryAfter(StatementProxy statement, String sql, ResultSetProxy resultSet) {
        executeSucceeded();
    }

    protected void statementExecuteBefore(StatementProxy statement, String sql) {
        beginTrace(statement, sql);
    }

    protected void statementExecuteAfter(StatementProxy statement, String sql, boolean result) {
        executeSucceeded();
    }

    protected void statement_executeErrorAfter(StatementProxy statement, String sql, Throwable error) {
        executeFailed(error);
    }

    private void beginTrace(StatementProxy statement, String sql) {
        statementTracer.beginTrace(sql, statement.getConnectionProxy().getDirectDataSource().getUrl());
    }

    private void executeSucceeded() {
        statementTracer.endTrace(0, null);
    }

    private void executeFailed(Throwable error) {
        statementTracer.endTrace(0, error);
    }
}
