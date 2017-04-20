package net.nightawk.sphex.basic;

import java.sql.SQLException;

public interface Interceptor {

    Object intercept(Chain chain) throws SQLException;

    interface Chain {
        Object proceed() throws SQLException;

        String sql();

        String url();
    }
}
