package net.nightawk.sphex;

public interface DBUrlParser {

    void parse(String url);

    String getDataBase();

    String getHost();

    int getPort();

}
