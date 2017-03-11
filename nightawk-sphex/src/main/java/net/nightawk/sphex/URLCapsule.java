package net.nightawk.sphex;

/**
 * @author Xs.
 */
public class URLCapsule {

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
