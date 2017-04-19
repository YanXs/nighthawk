package net.nightawk.sphex;

import com.github.kristofa.brave.ClientTracer;
import net.nightawk.sphex.support.*;

import java.io.PrintWriter;
import java.io.StringWriter;
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

    private Map<String, URLParser> parsers;

    public StatementTracer() {
        parsers = new HashMap<>();
        parsers.put("mysql", new MysqlURLParser());
        parsers.put("oracle", new OracleURLParser());
        parsers.put("db2", new DB2URLParser());
    }

    public void setClientTracer(ClientTracer clientTracer) {
        this.clientTracer = clientTracer;
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

    public boolean isTraceEnabled() {
        return clientTracer != null;
    }

    public void beginTrace(String sql, String url) {
        if (clientTracer != null) {
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
                clientTracer.startNewSpan(scheme);
                clientTracer.submitBinaryAnnotation("execute.sql", sql);
                setClientSent(address, databaseUrl.getPort(), serviceName);
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
                    clientTracer.submitBinaryAnnotation("error.message", traceThrowable(throwable));
                }
            } finally {
                clientTracer.setClientReceived();
            }
        }
    }

    private String traceThrowable(Throwable throwable) {
        StringWriter stringWriter = new StringWriter(8096);
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
