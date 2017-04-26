package net.xmoshi.nightawk.jdbc.mybatis;

import net.xmoshi.nightawk.jdbc.StatementTracer;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;

import java.sql.Statement;
import java.util.Properties;

/**
 * MybatisTracingInterceptor is a component to trace sql execution latency using mybatis Interceptor.
 * It is recommended that you use spring-mybatis,and this interceptor is better to be the first in the interceptorChain
 * so that it could intercept at last
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class})})
public class MybatisTracingInterceptor implements Interceptor {

    private StatementTracer statementTracer;

    public void setStatementTracer(StatementTracer statementTracer) {
        this.statementTracer = statementTracer;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (!statementTracer.isTraceEnabled()) {
            return invocation.proceed();
        }
        MetaObject metaObject = MetaObjectUtils.findTargetObject(invocation);
        BoundSql boundSql = MetaObjectUtils.getBoundSql(metaObject);
        Configuration configuration = MetaObjectUtils.getConfiguration(metaObject);
        Exception queryException = null;
        try {
            beginTrace(boundSql.getSql(), configuration.getEnvironment());
            return invocation.proceed();
        } catch (Exception ex) {
            queryException = ex;
            throw ex;
        } finally {
            statementTracer.endTrace(0, queryException);
        }
    }

    private void beginTrace(String sql, Environment environment) throws Exception {
        if (!(environment.getDataSource() instanceof DataSourceAdapter)) {
            throw new IllegalDataSourceException("datasource must be DataSourceAdapter");
        }
        DataSourceAdapter dataSource = (DataSourceAdapter) environment.getDataSource();
        statementTracer.beginTrace(sql, dataSource.getUrl());
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
