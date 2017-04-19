package net.nightawk.sphex.basic;


import org.apache.commons.dbcp.DelegatingConnection;
import org.apache.commons.dbcp.DelegatingStatement;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.dbcp.SQLNestedException;
import org.apache.commons.pool.impl.GenericObjectPool;

import java.sql.*;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 * @author Xs
 */
public class SphexPoolingDataSource extends PoolingDataSource {

    private String url;

    public SphexPoolingDataSource(GenericObjectPool connectionPool, String url) {
        super(connectionPool);
    }


    public Connection getConnection() throws SQLException {
        try {
            Connection conn = (Connection) (_pool.borrowObject());
            if (conn != null) {
                conn = new SphexPoolGuardConnectionWrapper(conn);
            }
            return conn;
        } catch (SQLException e) {
            throw e;
        } catch (NoSuchElementException e) {
            throw new SQLNestedException("Cannot get a connection, pool error " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new SQLNestedException("Cannot get a connection, general error", e);
        }
    }

    private class SphexPoolGuardConnectionWrapper extends DelegatingConnection {

        private Connection delegate;

        SphexPoolGuardConnectionWrapper(Connection delegate) {
            super(delegate);
            this.delegate = delegate;
        }

        protected void checkOpen() throws SQLException {
            if (delegate == null) {
                throw new SQLException("Connection is closed.");
            }
        }

        public void close() throws SQLException {
            if (delegate != null) {
                this.delegate.close();
                this.delegate = null;
                super.setDelegate(null);
            }
        }

        public boolean isClosed() throws SQLException {
            return delegate == null || delegate.isClosed();
        }

        public void clearWarnings() throws SQLException {
            checkOpen();
            delegate.clearWarnings();
        }

        public void commit() throws SQLException {
            checkOpen();
            delegate.commit();
        }

        public Statement createStatement() throws SQLException {
            checkOpen();
            return new DelegatingStatement(this, delegate.createStatement());
        }

        public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
            checkOpen();
            return new DelegatingStatement(this, delegate.createStatement(resultSetType, resultSetConcurrency));
        }

        public boolean innermostDelegateEquals(Connection c) {
            Connection innerCon = super.getInnermostDelegate();
            if (innerCon == null) {
                return c == null;
            } else {
                return innerCon.equals(c);
            }
        }

        public boolean getAutoCommit() throws SQLException {
            checkOpen();
            return delegate.getAutoCommit();
        }

        public String getCatalog() throws SQLException {
            checkOpen();
            return delegate.getCatalog();
        }

        public DatabaseMetaData getMetaData() throws SQLException {
            checkOpen();
            return delegate.getMetaData();
        }

        public int getTransactionIsolation() throws SQLException {
            checkOpen();
            return delegate.getTransactionIsolation();
        }

        public Map getTypeMap() throws SQLException {
            checkOpen();
            return delegate.getTypeMap();
        }

        public SQLWarning getWarnings() throws SQLException {
            checkOpen();
            return delegate.getWarnings();
        }

        public int hashCode() {
            if (delegate == null) {
                return 0;
            }
            return delegate.hashCode();
        }

        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            Connection conn = super.getInnermostDelegate();
            if (conn == null) {
                return false;
            }
            if (obj instanceof DelegatingConnection) {
                DelegatingConnection c = (DelegatingConnection) obj;
                return c.innermostDelegateEquals(conn);
            } else {
                return conn.equals(obj);
            }
        }

        public boolean isReadOnly() throws SQLException {
            checkOpen();
            return delegate.isReadOnly();
        }

        public String nativeSQL(String sql) throws SQLException {
            checkOpen();
            return delegate.nativeSQL(sql);
        }

        public CallableStatement prepareCall(String sql) throws SQLException {
            checkOpen();
            return new SphexDelegatingCallableStatement(this, delegate.prepareCall(sql));
        }

        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            checkOpen();
            return new SphexDelegatingCallableStatement(this, delegate.prepareCall(sql, resultSetType, resultSetConcurrency));
        }

        public PreparedStatement prepareStatement(String sql) throws SQLException {
            checkOpen();
            return new SphexDelegatingPreparedStatement(this, delegate.prepareStatement(sql), sql);
        }

        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
            checkOpen();
            return new SphexDelegatingPreparedStatement(this, delegate.prepareStatement(sql, resultSetType, resultSetConcurrency), sql);
        }

        public void rollback() throws SQLException {
            checkOpen();
            delegate.rollback();
        }

        public void setAutoCommit(boolean autoCommit) throws SQLException {
            checkOpen();
            delegate.setAutoCommit(autoCommit);
        }

        public void setCatalog(String catalog) throws SQLException {
            checkOpen();
            delegate.setCatalog(catalog);
        }

        public void setReadOnly(boolean readOnly) throws SQLException {
            checkOpen();
            delegate.setReadOnly(readOnly);
        }

        public void setTransactionIsolation(int level) throws SQLException {
            checkOpen();
            delegate.setTransactionIsolation(level);
        }

        public void setTypeMap(Map map) throws SQLException {
            checkOpen();
            delegate.setTypeMap(map);
        }

        public String toString() {
            if (delegate == null) {
                return "NULL";
            }
            return delegate.toString();
        }

        public int getHoldability() throws SQLException {
            checkOpen();
            return delegate.getHoldability();
        }

        public void setHoldability(int holdability) throws SQLException {
            checkOpen();
            delegate.setHoldability(holdability);
        }

        public java.sql.Savepoint setSavepoint() throws SQLException {
            checkOpen();
            return delegate.setSavepoint();
        }

        public java.sql.Savepoint setSavepoint(String name) throws SQLException {
            checkOpen();
            return delegate.setSavepoint(name);
        }

        public void releaseSavepoint(java.sql.Savepoint savepoint) throws SQLException {
            checkOpen();
            delegate.releaseSavepoint(savepoint);
        }

        public void rollback(java.sql.Savepoint savepoint) throws SQLException {
            checkOpen();
            delegate.rollback(savepoint);
        }

        public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            checkOpen();
            return new DelegatingStatement(this, delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
        }

        public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            checkOpen();
            return new SphexDelegatingCallableStatement(this, delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
        }

        public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
            checkOpen();
            return new SphexDelegatingPreparedStatement(this, delegate.prepareStatement(sql, autoGeneratedKeys), sql);
        }

        public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
            checkOpen();
            return new SphexDelegatingPreparedStatement(this, delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability), sql);
        }

        public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
            checkOpen();
            return new SphexDelegatingPreparedStatement(this, delegate.prepareStatement(sql, columnIndexes), sql);
        }

        public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
            checkOpen();
            return new SphexDelegatingPreparedStatement(this, delegate.prepareStatement(sql, columnNames), sql);
        }

        @Override
        public void setSchema(String schema) throws SQLException {

        }

        @Override
        public String getSchema() throws SQLException {
            return null;
        }

        @Override
        public void abort(Executor executor) throws SQLException {

        }

        @Override
        public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {

        }

        @Override
        public int getNetworkTimeout() throws SQLException {
            return 0;
        }

        /**
         * @see org.apache.commons.dbcp.DelegatingConnection#getDelegate()
         */
        public Connection getDelegate() {
            if (isAccessToUnderlyingConnectionAllowed()) {
                return super.getDelegate();
            } else {
                return null;
            }
        }

        /**
         * @see org.apache.commons.dbcp.DelegatingConnection#getInnermostDelegate()
         */
        public Connection getInnermostDelegate() {
            if (isAccessToUnderlyingConnectionAllowed()) {
                return super.getInnermostDelegate();
            } else {
                return null;
            }
        }
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
