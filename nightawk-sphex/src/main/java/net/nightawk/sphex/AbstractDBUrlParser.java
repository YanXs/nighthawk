package net.nightawk.sphex;

public abstract class AbstractDBUrlParser implements DBUrlParser {

    protected String host;

    protected int port;

    protected String dataBase;

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    public String getDataBase(){
        return dataBase;
    }
}
