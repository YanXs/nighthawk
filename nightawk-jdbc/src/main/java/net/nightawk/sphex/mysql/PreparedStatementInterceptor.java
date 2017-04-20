package net.nightawk.sphex.mysql;

import com.mysql.jdbc.*;
import net.nightawk.sphex.StatementTracer;

import java.sql.SQLException;
import java.util.Properties;

/**
 * copy from brave-mysql , whereas just care about PreparedStatement
 *
 * @author Xs
 */
public class PreparedStatementInterceptor implements StatementInterceptorV2 {

    private StatementTracer statementTracer;

    public PreparedStatementInterceptor() {
    }

    public PreparedStatementInterceptor(StatementTracer statementTracer) {
        this.statementTracer = statementTracer;
    }

    public void setStatementTracer(StatementTracer statementTracer) {
        this.statementTracer = statementTracer;
    }

    @Override
    public ResultSetInternalMethods preProcess(final String sql, final Statement interceptedStatement, final Connection connection) throws SQLException {
        final String sqlToLog;
        // When running a prepared statement, sql will be null and we must fetch the sql from the statement itself
        if (interceptedStatement instanceof PreparedStatement) {
            sqlToLog = ((PreparedStatement) interceptedStatement).getPreparedSql();
            beginTrace(sqlToLog, connection);
        }
        return null;
    }

    @Override
    public ResultSetInternalMethods postProcess(final String sql, final Statement interceptedStatement, final ResultSetInternalMethods originalResultSet,
                                                final Connection connection, final int warningCount, final boolean noIndexUsed, final boolean noGoodIndexUsed,
                                                final SQLException statementException) throws SQLException {
        statementTracer.endTrace(warningCount, statementException);
        return null;
    }

    private void beginTrace(final String sql, final Connection connection) throws SQLException {
        statementTracer.beginTrace(sql, connection.getMetaData().getURL());
    }

    @Override
    public boolean executeTopLevelOnly() {
        // True means that we don't get notified about queries that other interceptors issue
        return true;
    }

    @Override
    public void init(final Connection connection, final Properties properties) throws SQLException {
        // Don't care
    }

    @Override
    public void destroy() {
        // Don't care
    }
}
