package com.nightawk.mybatis;

/**
 * OracleUrlParser to parse oracle url
 * jdbc:oracle:thin:@127.0.0.1:1521:MYDB
 */
public class OracleUrlParser extends AbstractDBUrlParser {

    @Override
    public void parse(String url) {
        String address = url.substring(url.indexOf("@") + 1);
        String[] strings = address.split(":");
        host = strings[0];
        port = Integer.valueOf(strings[1]);
        dataBase = strings[2];
    }
}
