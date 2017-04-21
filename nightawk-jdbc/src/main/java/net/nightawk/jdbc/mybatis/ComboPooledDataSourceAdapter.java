package net.nightawk.jdbc.mybatis;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import javax.sql.DataSource;

public class ComboPooledDataSourceAdapter extends AbstractDataSourceAdapter {

    private final ComboPooledDataSource comboPooledDataSource;

    public ComboPooledDataSourceAdapter(ComboPooledDataSource comboPooledDataSource) {
        this.comboPooledDataSource = comboPooledDataSource;
    }

    @Override
    public String getUrl() {
        return comboPooledDataSource.getJdbcUrl();
    }

    @Override
    public DataSource getDataSource() {
        return comboPooledDataSource;
    }
}
