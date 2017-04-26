package net.xmoshi.nightawk.jdbc.mybatis;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;

/**
 * @author Xs.
 */
public class MetaObjectUtils {

    private static final ObjectFactory OBJECT_FACTORY = new DefaultObjectFactory();
    private static final ObjectWrapperFactory OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();
    private static final String HANDLER_FIELD = "h";
    private static final String TARGET_FIELD = "target";
    private static final String DELEGATE_BOUND_SQL = "delegate.boundSql";
    private static final String DELEGATE_CONFIGURATION = "delegate.configuration";
    private static final String DELEGATE_MAPPED_STATEMENT = "delegate.mappedStatement";
    private static final String DELEGATE_ROW_BOUNDS = " delegate.rowBounds";

    public static MetaObject findTargetObject(Invocation invocation) {
        MetaObject metaObject = MetaObject.forObject(invocation.getTarget(), OBJECT_FACTORY, OBJECT_WRAPPER_FACTORY);
        while (metaObject.hasGetter(HANDLER_FIELD)) {
            Object o = metaObject.getValue(HANDLER_FIELD);
            metaObject = MetaObject.forObject(o, OBJECT_FACTORY, OBJECT_WRAPPER_FACTORY);
            if (metaObject.hasGetter(TARGET_FIELD)) {
                o = metaObject.getValue(TARGET_FIELD);
                metaObject = MetaObject.forObject(o, OBJECT_FACTORY, OBJECT_WRAPPER_FACTORY);
            }
        }
        return metaObject;
    }

    public static BoundSql getBoundSql(MetaObject metaObject) {
        return (BoundSql) metaObject.getValue(DELEGATE_BOUND_SQL);
    }

    public static Configuration getConfiguration(MetaObject metaObject) {
        return (Configuration) metaObject.getValue(DELEGATE_CONFIGURATION);
    }

    public static MappedStatement getMappedStatement(MetaObject metaObject) {
        return (MappedStatement) metaObject.getValue(DELEGATE_MAPPED_STATEMENT);
    }

    public static RowBounds getRowBounds(MetaObject metaObject) {
        return (RowBounds) metaObject.getValue(DELEGATE_ROW_BOUNDS);
    }
}
