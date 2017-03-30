package net.nightawk.sphex.support;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Xs.
 */
public class DatabaseURL {

    static final ConcurrentHashMap<String, DatabaseURL> DATABASE_URLS = new ConcurrentHashMap<>();

    private final String host;

    private final int port;

    private final String dataBase;

    public DatabaseURL(String host, int port, String dataBase) {
        this.host = host;
        this.port = port;
        this.dataBase = dataBase;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDataBase() {
        return dataBase;
    }
}
