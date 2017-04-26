package net.xmoshi.nightawk.jdbc.mybatis;

import javax.sql.DataSource;

/**
 * @author Xs
 */
public class TomcatDataSourceAdapter extends AbstractDataSourceAdapter{

    private final org.apache.tomcat.jdbc.pool.DataSource dataSource;

    public TomcatDataSourceAdapter(org.apache.tomcat.jdbc.pool.DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String getUrl() {
        return dataSource.getUrl();
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }
}
