package net.nightawk.sphex.basic;

import org.apache.commons.dbcp.DelegatingConnection;
import org.apache.commons.dbcp.DelegatingPreparedStatement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Xs
 */
public class SphexDelegatingPreparedStatement extends DelegatingPreparedStatement {

    private final String sql;

    private final String url;

    private final List<Interceptor> interceptors;

    public SphexDelegatingPreparedStatement(DelegatingConnection c, PreparedStatement s,
                                            String sql, String url, List<Interceptor> interceptors) {
        super(c, s);
        this.sql = sql;
        this.url = url;
        this.interceptors = interceptors;
    }

    public void setDelegate(PreparedStatement s) {
        super.setDelegate(s);
        _stmt = s;
    }

    public ResultSet executeQuery() throws SQLException {
        checkOpen();
        Interceptor.Chain chain = new StatementInterceptorChain(0, StatementType.Query, (PreparedStatement) _stmt);
        return (ResultSet) chain.proceed();
    }

    public int executeUpdate() throws SQLException {
        checkOpen();
        Interceptor.Chain chain = new StatementInterceptorChain(0, StatementType.Update, (PreparedStatement) _stmt);
        return (int) chain.proceed();
    }

    public boolean execute() throws SQLException {
        checkOpen();
        Interceptor.Chain chain = new StatementInterceptorChain(0, StatementType.Universal, (PreparedStatement) _stmt);
        return (boolean) chain.proceed();
    }

    enum StatementType {
        Query, Update, Universal
    }

    class StatementInterceptorChain implements Interceptor.Chain {

        private final int index;

        private final PreparedStatement statement;

        private final StatementType statementType;

        public StatementInterceptorChain(int index, StatementType statementType, PreparedStatement statement) {
            this.index = index;
            this.statementType = statementType;
            this.statement = statement;
        }

        @Override
        public Object proceed() throws SQLException {
            if (interceptors != null && index < interceptors.size()) {
                Interceptor.Chain chain = new StatementInterceptorChain(index + 1, statementType, statement);
                Interceptor interceptor = interceptors.get(index);
                return interceptor.intercept(chain);
            }
            if (statementType == StatementType.Query) {
                try {
                    return statement.executeQuery();
                } catch (SQLException e) {
                    handleException(e);
                    throw new AssertionError();
                }
            } else if (statementType == StatementType.Update) {
                try {
                    return statement.executeUpdate();
                } catch (SQLException e) {
                    handleException(e);
                    return 0;
                }
            } else {
                try {
                    return statement.execute();
                } catch (SQLException e) {
                    handleException(e);
                    return false;
                }
            }
        }

        @Override
        public String sql() {
            return sql;
        }

        @Override
        public String url() {
            return url;
        }
    }


    @Override
    public void closeOnCompletion() throws SQLException {

    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }
}
