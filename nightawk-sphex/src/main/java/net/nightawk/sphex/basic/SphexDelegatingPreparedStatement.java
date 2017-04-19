package net.nightawk.sphex.basic;

import org.apache.commons.dbcp.DelegatingConnection;
import org.apache.commons.dbcp.DelegatingPreparedStatement;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Xs
 */
public class SphexDelegatingPreparedStatement extends DelegatingPreparedStatement {

    private String sql;

    public SphexDelegatingPreparedStatement(DelegatingConnection c, PreparedStatement s, String sql) {
        super(c, s);
        this.sql = sql;
    }

    @Override
    public void closeOnCompletion() throws SQLException {

    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }
}
