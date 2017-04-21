package net.nightawk.jdbc.mybatis;

import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;


public class BasicDataSourceAdapter extends AbstractDataSourceAdapter {

    private final BasicDataSource basicDataSource;

    public BasicDataSourceAdapter(BasicDataSource basicDataSource) {
        this.basicDataSource = basicDataSource;
    }

    @Override
    public String getUrl() {
        return basicDataSource.getUrl();
    }

    @Override
    public DataSource getDataSource() {
        return basicDataSource;
    }
}
