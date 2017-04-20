package net.nightawk.sphex.basic;

import org.apache.commons.dbcp.DelegatingCallableStatement;
import org.apache.commons.dbcp.DelegatingConnection;

import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * @author Xs
 */
public class SphexDelegatingCallableStatement extends DelegatingCallableStatement {


    /**
     * Create a wrapper for the Statement which traces this
     * Statement to the Connection which created it and the
     * code which created it.
     *
     * @param c the {@link DelegatingConnection} that created this statement
     * @param s the {@link CallableStatement} to delegate all calls to
     */
    public SphexDelegatingCallableStatement(DelegatingConnection c, CallableStatement s) {
        super(c, s);
    }

    @Override
    public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
        return null;
    }

    @Override
    public <T> T getObject(String parameterName, Class<T> type) throws SQLException {
        return null;
    }

    @Override
    public void closeOnCompletion() throws SQLException {

    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }
}
