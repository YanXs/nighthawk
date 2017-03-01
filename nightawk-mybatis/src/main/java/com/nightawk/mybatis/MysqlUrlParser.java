package com.nightawk.mybatis;

import java.net.URI;

/**
 * MysqlUrlParser to parse mysql jdbc url
 * <p/>
 * jdbc:mysql://localhost:3306/test?useUnicode=true
 * <or/>
 * jdbc:mysql://localhost:3306/test
 */
public class MysqlUrlParser extends AbstractDBUrlParser {

    @Override
    public void parse(String url) {
        URI uri = URI.create(url.substring(5)); // strip "jdbc:"
        host = uri.getHost();
        port = uri.getPort();
        dataBase = uri.getPath().substring(1); // path "/test"
    }
}
