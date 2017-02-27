package com.nightawk.mysql;

import com.github.kristofa.brave.ClientTracer;
import com.mysql.jdbc.*;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Properties;

/**
 * copy from brave-mysql , whereas just care about PreparedStatement
 *
 * @author Xs
 */
public class PreparedStatementInterceptor implements StatementInterceptorV2 {

    private final static String SERVICE_NAME_KEY = "zipkinServiceName";

    private final ThreadLocal<Long> startTimeMs = new ThreadLocal<>();

    private static volatile ClientTracer clientTracer;

    private static volatile Long longQueryMs;

    public static void setLongQueryMs(Long longQueryMs) {
        PreparedStatementInterceptor.longQueryMs = longQueryMs;
    }

    public static void setClientTracer(final ClientTracer tracer) {
        clientTracer = tracer;
    }


    @Override
    public ResultSetInternalMethods preProcess(final String sql, final Statement interceptedStatement, final Connection connection) throws SQLException {
        ClientTracer clientTracer = PreparedStatementInterceptor.clientTracer;
        if (clientTracer != null) {
            final String sqlToLog;
            // When running a prepared statement, sql will be null and we must fetch the sql from the statement itself
            if (interceptedStatement instanceof PreparedStatement) {
                sqlToLog = ((PreparedStatement) interceptedStatement).getPreparedSql();
                beginTrace(clientTracer, sqlToLog, connection);
                startTimeMs.set(System.currentTimeMillis());
            }
        }
        return null;
    }

    @Override
    public ResultSetInternalMethods postProcess(final String sql, final Statement interceptedStatement, final ResultSetInternalMethods originalResultSet,
                                                final Connection connection, final int warningCount, final boolean noIndexUsed, final boolean noGoodIndexUsed,
                                                final SQLException statementException) throws SQLException {
        ClientTracer clientTracer = PreparedStatementInterceptor.clientTracer;
        if (clientTracer != null && overtime()) {
            endTrace(clientTracer, warningCount, statementException);
        }
        return null;
    }

    private boolean overtime() {
        Long now = System.currentTimeMillis();
        return (now - startTimeMs.get()) > longQueryMs;
    }

    private void beginTrace(final ClientTracer tracer, final String sql, final Connection connection) throws SQLException {
        tracer.startNewSpan("query");
        tracer.submitBinaryAnnotation("executed.query", sql);
        try {
            setClientSent(tracer, connection);
        } catch (Exception e) {
            tracer.setClientSent();
        }
    }

    /**
     * MySQL exposes the host connecting to, but not the port. This attempts to get the port from the
     * JDBC URL. Ex. 5555 from {@code jdbc:mysql://localhost:5555/isSampled}, or 3306 if absent.
     */
    private void setClientSent(ClientTracer tracer, Connection connection) throws Exception {
        InetAddress address = Inet4Address.getByName(connection.getHost());
        int ipv4 = ByteBuffer.wrap(address.getAddress()).getInt();

        URI url = URI.create(connection.getMetaData().getURL().substring(5)); // strip "jdbc:"
        int port = url.getPort() == -1 ? 3306 : url.getPort();

        Properties props = connection.getProperties();
        String serviceName = props.getProperty(SERVICE_NAME_KEY);
        if (serviceName == null || "".equals(serviceName)) {
            serviceName = "mysql";
            String databaseName = connection.getCatalog();
            if (databaseName != null && !"".equals(databaseName)) {
                serviceName += "-" + databaseName;
            }
        }
        tracer.setClientSent(ipv4, port, serviceName);
    }

    private void endTrace(final ClientTracer tracer, final int warningCount, final SQLException statementException) {
        try {
            if (warningCount > 0) {
                tracer.submitBinaryAnnotation("warning.count", warningCount);
            }
            if (statementException != null) {
                tracer.submitBinaryAnnotation("error.code", statementException.getErrorCode());
            }
        } finally {
            tracer.setClientReceived();
        }
    }

    @Override
    public boolean executeTopLevelOnly() {
        // True means that we don't get notified about queries that other interceptors issue
        return true;
    }

    @Override
    public void init(final Connection connection, final Properties properties) throws SQLException {
        // Don't care
    }

    @Override
    public void destroy() {
        // Don't care
    }
}
