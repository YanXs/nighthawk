package net.nightawk.sphex;

/**
 * OracleURLParser to parse oracle url
 * jdbc:oracle:thin:@127.0.0.1:1521:MYDB
 */
public class OracleURLParser implements URLParser {

    @Override
    public URLCapsule parse(String url) {
        URLCapsule urlCapsule = URLCapsule.URL_CAPSULES.get(url);
        if (urlCapsule == null) {
            String address = url.substring(url.indexOf("@") + 1);
            String[] strings = address.split(":");
            String host = strings[0];
            int port = Integer.valueOf(strings[1]);
            String dataBase = strings[2];
            urlCapsule = new URLCapsule(host, port, dataBase);
            URLCapsule.URL_CAPSULES.putIfAbsent(url, urlCapsule);
        }
        return urlCapsule;
    }
}
