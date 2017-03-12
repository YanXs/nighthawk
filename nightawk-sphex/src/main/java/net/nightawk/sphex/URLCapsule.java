package net.nightawk.sphex;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Xs.
 */
public class URLCapsule {

    static final ConcurrentHashMap<String, URLCapsule> URL_CAPSULES = new ConcurrentHashMap<>();

    private final String host;

    private final int port;

    private final String dataBase;

    public URLCapsule(String host, int port, String dataBase) {
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
