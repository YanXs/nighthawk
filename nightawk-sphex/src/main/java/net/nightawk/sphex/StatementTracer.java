package net.nightawk.sphex;

import com.github.kristofa.brave.ClientTracer;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Xs.
 */
public class StatementTracer {

    private volatile ClientTracer clientTracer;

    private Map<String, DBUrlParser> parsers;

    public StatementTracer() {
        parsers = new HashMap<>();
        parsers.put("mysql", new MysqlUrlParser());
        parsers.put("oracle", new OracleUrlParser());
    }

    public void setClientTracer(ClientTracer clientTracer) {
        this.clientTracer = clientTracer;
    }

    public void setParsers(Map<String, DBUrlParser> parsers) {
        if (!parsers.isEmpty()) {
            mergeParsers(parsers);
        }
    }

    private void mergeParsers(Map<String, DBUrlParser> parsers) {
        for (Map.Entry<String, DBUrlParser> entry : parsers.entrySet()) {
            this.parsers.put(entry.getKey(), entry.getValue());
        }
    }

    public boolean isTraceEnabled() {
        return clientTracer != null;
    }

    public void beginTrace(String sql, String url) {
        if (clientTracer != null) {
            try {
                String afterJDBC = url.substring(5); // jdbc:
                String scheme = afterJDBC.substring(0, afterJDBC.indexOf(":"));
                DBUrlParser parser = parsers.get(scheme);
                if (parser == null) {
                    throw new IllegalStateException("unknown db scheme: " + scheme);
                }
                parser.parse(url);
                String serviceName = scheme + "-" + parser.getDataBase();
                InetAddress address = Inet4Address.getByName(parser.getHost());
                clientTracer.startNewSpan(scheme);
                clientTracer.submitBinaryAnnotation("execute.sql", sql);
                setClientSent(address, parser.getPort(), serviceName);
            } catch (Exception e) {
                clientTracer.setClientSent();
            }
        }
    }

    private void setClientSent(InetAddress address, int port, String serviceName) {
        int ipv4 = ByteBuffer.wrap(address.getAddress()).getInt();
        clientTracer.setClientSent(ipv4, port, serviceName);
    }

    public void endTrace(final int warningCount, final Throwable throwable) {
        if (clientTracer != null) {
            try {
                if (warningCount > 0) {
                    clientTracer.submitBinaryAnnotation("warning.count", warningCount);
                }
                if (throwable != null) {
                    clientTracer.submitBinaryAnnotation("error.message", throwable.getMessage());
                }
            } finally {
                clientTracer.setClientReceived();
            }
        }
    }
}
