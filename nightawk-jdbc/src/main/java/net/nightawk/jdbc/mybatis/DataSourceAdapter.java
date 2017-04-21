package net.nightawk.jdbc.mybatis;

import javax.sql.DataSource;

public interface DataSourceAdapter extends DataSource {

    String getUrl();

    DataSource getDataSource();
}
