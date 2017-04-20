package net.nightawk.sphex.tomcat.jdbc;

import net.nightawk.sphex.StatementTracer;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.JdbcInterceptor;
import org.apache.tomcat.jdbc.pool.ProxyConnection;
import org.apache.tomcat.jdbc.pool.interceptor.AbstractCreateStatementInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Xs
 */
public class TomcatDataSourceTracingInterceptor extends AbstractCreateStatementInterceptor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Constructor<?>[] constructors =
            new Constructor[AbstractCreateStatementInterceptor.STATEMENT_TYPE_COUNT];

    private volatile String url;

    private static final AtomicReference<StatementTracer> reference = new AtomicReference<>(null);

    public static void setStatementTracer(final StatementTracer tracer) {
        reference.set(tracer);
    }

    private static StatementTracer getStatementTracer() {
        return reference.get();
    }

    @Override
    public Object createStatement(Object proxy, Method method, Object[] args, Object statement, long time) {
        try {
            Object result;
            String name = method.getName();
            String sql = null;
            Constructor<?> constructor;
            if (compare(CREATE_STATEMENT, name)) {
                constructor = getConstructor(CREATE_STATEMENT_IDX, Statement.class);
            } else if (compare(PREPARE_STATEMENT, name)) {
                sql = (String) args[0];
                constructor = getConstructor(PREPARE_STATEMENT_IDX, PreparedStatement.class);
            } else if (compare(PREPARE_CALL, name)) {
                sql = (String) args[0];
                constructor = getConstructor(PREPARE_CALL_IDX, CallableStatement.class);
            } else {
                return statement;
            }
            result = constructor.newInstance(new StatementProxy(statement, sql));
            return result;
        } catch (Exception x) {
            logger.warn("Unable to create statement proxy for tracing.", x);
        }
        return statement;
    }

    private class StatementProxy implements InvocationHandler {

        protected boolean closed = false;
        protected Object delegate;
        protected final String sql;

        public StatementProxy(Object parent, String sql) {
            this.delegate = parent;
            this.sql = sql;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            final String name = method.getName();
            boolean close = compare(JdbcInterceptor.CLOSE_VAL, name);
            if (close && closed) return null;
            if (compare(JdbcInterceptor.ISCLOSED_VAL, name)) {
                return closed;
            }
            if (closed) throw new SQLException("Statement closed.");
            boolean process = isExecute(method, false);
            StatementTracer tracer = getStatementTracer();
            Object result = null;
            Throwable error = null;
            try {
                if (process && tracer != null) {
                    tracer.beginTrace(sql, lookupUrl());
                }
                result = method.invoke(delegate, args);
            } catch (Throwable t) {
                error = t;
                throw t;
            } finally {
                if (tracer != null) {
                    tracer.endTrace(0, error);
                }
            }
            if (close) {
                closed = true;
                delegate = null;
            }
            return result;
        }
    }

    protected Constructor<?> getConstructor(int idx, Class<?> clazz) throws NoSuchMethodException {
        if (constructors[idx] == null) {
            Class<?> proxyClass = Proxy.getProxyClass(TomcatDataSourceTracingInterceptor.class.getClassLoader(), clazz);
            constructors[idx] = proxyClass.getConstructor(InvocationHandler.class);
        }
        return constructors[idx];
    }


    private String lookupUrl() {
        if (url == null) {
            JdbcInterceptor interceptor = getNext();
            while (interceptor != null) {
                if (interceptor instanceof ProxyConnection) {
                    url = ((ProxyConnection) interceptor).getPool().getPoolProperties().getUrl();
                    break;
                }
                interceptor = getNext();
            }
            if (url == null) {
                throw new IllegalStateException("url must not be null after looking up the chain");
            }
        }
        return url;
    }

    public void poolStarted(ConnectionPool pool) {
        url = pool.getPoolProperties().getUrl();
    }

    @Override
    public void closeInvoked() {
        // NOP
    }
}
