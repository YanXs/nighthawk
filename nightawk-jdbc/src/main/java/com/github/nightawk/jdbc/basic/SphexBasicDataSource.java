package com.github.nightawk.jdbc.basic;

import org.apache.commons.dbcp.BasicDataSource;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Xs
 */
public class SphexBasicDataSource extends BasicDataSource {

    private List<Interceptor> interceptors;

    public void setInterceptors(List<Interceptor> interceptors) {
        this.interceptors = interceptors;
    }

    protected void createDataSourceInstance() throws SQLException {
        SphexPoolingDataSource pds = new SphexPoolingDataSource(connectionPool, url, interceptors);
        pds.setAccessToUnderlyingConnectionAllowed(isAccessToUnderlyingConnectionAllowed());
        pds.setLogWriter(logWriter);
        dataSource = pds;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
