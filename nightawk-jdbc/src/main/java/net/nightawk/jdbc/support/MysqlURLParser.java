package net.nightawk.jdbc.support;

import java.net.URI;

/**
 * MysqlURLParser to parse mysql jdbc url
 * <p/>
 * jdbc:mysql://localhost:3306/test?useUnicode=true
 * <or/>
 * jdbc:mysql://localhost:3306/test
 */
public class MysqlURLParser implements URLParser {

    @Override
    public DatabaseURL parse(String url) {
        DatabaseURL databaseUrl = DatabaseURL.DATABASE_URLS.get(url);
        if (databaseUrl == null) {
            URI uri = URI.create(url.substring(5)); // strip "jdbc:"
            String host = uri.getHost();
            int port = uri.getPort();
            String dataBase = uri.getPath().substring(1); // path "/test"
            databaseUrl = new DatabaseURL(host, port, dataBase);
            DatabaseURL.DATABASE_URLS.putIfAbsent(url, databaseUrl);
        }
        return databaseUrl;
    }
}
