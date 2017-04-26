package net.xmoshi.nightawk.jdbc.mybatis;

import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class RoutingDataSourceAdapter extends AbstractDataSourceAdapter implements DataSourceAdapter, InitializingBean {

    private static final ThreadLocal<String> dataSourceKey = new InheritableThreadLocal<>();

    private Map<String, DataSourceAdapter> targetDataSources;

    private DataSourceAdapter defaultTargetDataSource;

    private boolean lenientFallback = true;

    public static void setDataSourceKey(String dataSource) {
        dataSourceKey.set(dataSource);
    }

    public void setTargetDataSources(Map<String, DataSourceAdapter> targetDataSources) {
        this.targetDataSources = targetDataSources;
    }

    public void setDefaultTargetDataSource(DataSourceAdapter defaultTargetDataSource) {
        this.defaultTargetDataSource = defaultTargetDataSource;
    }

    public void setLenientFallback(boolean lenientFallback) {
        this.lenientFallback = lenientFallback;
    }

    @Override
    public void afterPropertiesSet() {
        if (this.targetDataSources == null) {
            throw new IllegalArgumentException("Property 'targetDataSources' is required");
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return determineTargetDataSource().getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return determineTargetDataSource().getConnection(username, password);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        return determineTargetDataSource().unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return (iface.isInstance(this) || determineTargetDataSource().isWrapperFor(iface));
    }

    protected DataSourceAdapter determineTargetDataSource() {
        String lookupKey = determineCurrentLookupKey();
        DataSourceAdapter dataSource = targetDataSources.get(lookupKey);
        if (dataSource == null && (this.lenientFallback || lookupKey == null)) {
            dataSource = this.defaultTargetDataSource;
        }
        if (dataSource == null) {
            throw new IllegalStateException("Cannot determine target DataSource for lookup key [" + lookupKey + "]");
        }
        return dataSource;
    }

    private String determineCurrentLookupKey() {
        return dataSourceKey.get();
    }

    @Override
    public String getUrl() {
        return determineTargetDataSource().getUrl();
    }

    @Override
    public DataSource getDataSource() {
        return determineTargetDataSource().getDataSource();
    }
}
