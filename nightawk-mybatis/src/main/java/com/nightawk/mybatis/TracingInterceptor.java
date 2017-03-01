package com.nightawk.mybatis;

import com.github.kristofa.brave.ClientTracer;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;

import javax.sql.DataSource;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * TracingInterceptor is a component to trace sql execution latency using mybatis Interceptor.
 * It is recommended that you use spring-mybatis
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class})})
public class TracingInterceptor implements Interceptor {

    private static final ObjectFactory OBJECT_FACTORY = new DefaultObjectFactory();
    private static final ObjectWrapperFactory OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
    private static final String HANDLER_FIELD = "h";
    private static final String TARGET_FIELD = "target";

    private ClientTracer clientTracer;

    private Map<String, DBUrlParser> parsers;


    public TracingInterceptor() {
        parsers = new HashMap<>();
        parsers.put("mysql", new MysqlUrlParser());
        parsers.put("oracle", new OracleUrlParser());
    }

    public void setClientTracer(final ClientTracer clientTracer) {
        this.clientTracer = clientTracer;
    }

    public void setParsers(Map<String, DBUrlParser> parsers) {
        if (!parsers.isEmpty()) {
            mergeParsers(parsers);
        }
    }

    private void mergeParsers(Map<String, DBUrlParser> parsers) {
        for (Map.Entry<String, DBUrlParser> entry : parsers.entrySet()) {
            parsers.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (clientTracer == null) {
            return invocation.proceed();
        }
        MetaObject metaObject = MetaObject.forObject(invocation.getTarget(), OBJECT_FACTORY, OBJECT_WRAPPER_FACTORY);
        while (metaObject.hasGetter(HANDLER_FIELD)) {
            Object o = metaObject.getValue(HANDLER_FIELD);
            metaObject = MetaObject.forObject(o, OBJECT_FACTORY, OBJECT_WRAPPER_FACTORY);
            if (metaObject.hasGetter(TARGET_FIELD)) {
                o = metaObject.getValue(TARGET_FIELD);
                metaObject = MetaObject.forObject(o, OBJECT_FACTORY, OBJECT_WRAPPER_FACTORY);
            }
        }
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        Configuration configuration = (Configuration) metaObject.getValue("delegate.configuration");
        Exception queryException = null;
        try {
            beginTrace(boundSql.getSql(), configuration.getEnvironment());
            return invocation.proceed();
        } catch (Exception ex) {
            queryException = ex;
            throw ex;
        } finally {
            endTrace(queryException);
        }
    }

    private void beginTrace(String sql, Environment environment) throws Exception {
        clientTracer.startNewSpan("query");
        clientTracer.submitBinaryAnnotation("executed.query", sql);
        DataSource dataSource = environment.getDataSource();
        try {
            setClientSent(dataSource.getConnection());
        } catch (Exception e) {
            clientTracer.setClientSent();
        }
    }

    private void setClientSent(Connection connection) throws Exception {
        DatabaseMetaData metaData = connection.getMetaData();
        String url = metaData.getURL();
        String afterJDBC = url.substring(5); // jdbc:
        String scheme = afterJDBC.substring(0, afterJDBC.indexOf(":"));
        DBUrlParser parser = parsers.get(scheme);
        if (parser == null) {
            throw new IllegalStateException("unknown db scheme: " + scheme);
        }
        parser.parse(url);
        String serviceName = scheme + "-" + parser.getDataBase();
        InetAddress address = Inet4Address.getByName(parser.getHost());
        int ipv4 = ByteBuffer.wrap(address.getAddress()).getInt();
        clientTracer.setClientSent(ipv4, parser.getPort(), serviceName);
    }

    private void endTrace(Exception queryException) {
        try {
            if (queryException != null) {
                String errorMessage = queryException.getMessage();
                if (StringUtils.isEmpty(errorMessage)) {
                    errorMessage = "check error message in log file";
                }
                clientTracer.submitBinaryAnnotation("sql.exception", errorMessage);
            }
        } finally {
            clientTracer.setClientReceived();
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // NOP
    }
}
