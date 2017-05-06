package com.github.nightawk.jdbc;

import com.github.kristofa.brave.ClientTracer;
import com.github.kristofa.brave.SpanId;
import com.github.nightawk.core.util.TracingLoop;
import com.github.nightawk.jdbc.support.*;

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
public class StatementTracer implements TracingLoop {

    private volatile ClientTracer clientTracer;

    private Map<String, URLParser> parsers;

    private final TracingLoop delegate;

    public StatementTracer() {
        delegate = TracingLoop.DEFAULT_LOOP;
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
                SpanId spanId = clientTracer.startNewSpan(scheme);
                if (spanId != null) {
                    joinTracingLoop();
                    clientTracer.submitBinaryAnnotation("execute.sql", sql);
                    setClientSent(address, databaseUrl.getPort(), serviceName);
                }
            } catch (Exception e) {
                if (inTracingLoop()) {
                    clientTracer.setClientSent();
                }
            }
        }
    }

    private void setClientSent(InetAddress address, int port, String serviceName) {
        int ipv4 = ByteBuffer.wrap(address.getAddress()).getInt();
        clientTracer.setClientSent(ipv4, port, serviceName);
    }

    public void endTrace(final int warningCount, final Throwable throwable) {
        try {
            if (clientTracer != null && inTracingLoop()) {
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
        } finally {
            if (inTracingLoop()) {
                leaveTracingLoop();
            }
        }
    }

    private String traceThrowable(Throwable throwable) {
        StringWriter stringWriter = new StringWriter(8096);
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    @Override
    public boolean inTracingLoop() {
        return false;
    }

    @Override
    public void joinTracingLoop() {

    }

    @Override
    public void leaveTracingLoop() {

    }
}
