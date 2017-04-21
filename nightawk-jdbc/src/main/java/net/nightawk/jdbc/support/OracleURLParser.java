package net.nightawk.jdbc.support;

/**
 * OracleURLParser to parse oracle url
 * jdbc:oracle:thin:@127.0.0.1:1521:MYDB
 */
public class OracleURLParser implements URLParser {

    @Override
    public DatabaseURL parse(String url) {
        DatabaseURL databaseUrl = DatabaseURL.DATABASE_URLS.get(url);
        if (databaseUrl == null) {
            String address = url.substring(url.indexOf("@") + 1);
            String[] strings = address.split(":");
            String host = strings[0];
            int port = Integer.valueOf(strings[1]);
            String dataBase = strings[2];
            databaseUrl = new DatabaseURL(host, port, dataBase);
            DatabaseURL.DATABASE_URLS.putIfAbsent(url, databaseUrl);
        }
        return databaseUrl;
    }
}
