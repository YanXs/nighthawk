package net.nightawk.sphex.mybatis;

import com.alibaba.druid.pool.DruidDataSource;

import javax.sql.DataSource;

public class DruidDatasourceAdapter extends AbstractDataSourceAdapter {

    private final DruidDataSource druidDataSource;

    public DruidDatasourceAdapter(DruidDataSource druidDataSource) {
        this.druidDataSource = druidDataSource;
    }

    @Override
    public String getUrl() {
        return druidDataSource.getUrl();
    }

    @Override
    public DataSource getDataSource() {
        return druidDataSource;
    }
}
