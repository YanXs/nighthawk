package net.nightawk.sphex.mybatis;

import javax.sql.DataSource;

public interface DataSourceAdapter extends DataSource{

    String getUrl();

    DataSource getDataSource();
}
