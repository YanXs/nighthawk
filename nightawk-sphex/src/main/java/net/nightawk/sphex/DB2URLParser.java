package net.nightawk.sphex;

import java.net.URI;

/**
 * jdbc:db2://192.168.0.2:50000/test
 *
 * @author Xs.
 */
public class DB2URLParser implements URLParser {

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
