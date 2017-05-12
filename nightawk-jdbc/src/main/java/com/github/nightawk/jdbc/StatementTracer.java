package com.github.nightawk.jdbc;

import com.github.nightawk.core.ClientTracerSupport;
import com.github.nightawk.jdbc.support.*;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Xs.
 */
public class StatementTracer extends ClientTracerSupport {

    private Map<String, URLParser> parsers;

    public StatementTracer() {
        parsers = new HashMap<>();
        parsers.put("mysql", new MysqlURLParser());
        parsers.put("oracle", new OracleURLParser());
        parsers.put("db2", new DB2URLParser());
    }

    public void setParsers(Map<String, URLParser> parsers) {
        if (!parsers.isEmpty()) {
            mergeParsers(parsers);
        }
    }

    private void mergeParsers(Map<String, URLParser> parsers) {
        for (Map.Entry<String, URLParser> entry : parsers.entrySet()) {
            this.parsers.put(entry.getKey(), entry.getValue());
        }
    }


    public void beginTrace(String sql, String url) {
        if (isTraceEnabled()) {
            try {
                String afterJDBC = url.substring(5); // jdbc:
                String scheme = afterJDBC.substring(0, afterJDBC.indexOf(":"));
                URLParser parser = parsers.get(scheme);
                if (parser == null) {
                    throw new IllegalStateException("unknown db scheme: " + scheme);
                }
                DatabaseURL databaseUrl = parser.parse(url);
                String serviceName = scheme + "-" + databaseUrl.getDataBase();
                InetAddress address = Inet4Address.getByName(databaseUrl.getHost());
                Beginning beginning = new Beginning();
                beginning.setSpanName(serviceName);
                beginning.setAddress(address);
                beginning.setPort(databaseUrl.getPort());
                beginning.addAnnotation("execute.sql", sql);
                beginTrace(beginning);
            } catch (Exception e) {
                setClientSent();
            }
        }
    }

    public void endTrace(final int warningCount, final Throwable throwable) {
        if (isTraceEnabled()) {
            Ending ending = new Ending();
            if (warningCount > 0) {
                ending.addAnnotation("warning.count", String.valueOf(warningCount));
            }
            if (throwable != null) {
                ending.addAnnotation("error.message", ExceptionUtils.getStackTrace(throwable));
            }
            endTrace(ending);
        }
    }
}
