package net.nightawk.sphex.basic;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.PoolingDataSource;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * @author Xs
 */
public class SphexBasicDataSource extends BasicDataSource {


    protected void createDataSourceInstance() throws SQLException {
        SphexPoolingDataSource pds = new SphexPoolingDataSource(connectionPool, url);
        pds.setAccessToUnderlyingConnectionAllowed(isAccessToUnderlyingConnectionAllowed());
        pds.setLogWriter(logWriter);
        dataSource = pds;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }
}
