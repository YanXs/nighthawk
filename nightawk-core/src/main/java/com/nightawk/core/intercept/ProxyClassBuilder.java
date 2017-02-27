package com.nightawk.core.intercept;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Xs.
 */
public class ProxyClassBuilder {

    private static final ConcurrentMap<Class<?>, Class<?>> classCache = new ConcurrentHashMap<>();

    public static <T> Class<?> build(Class<T> origin, MethodExclusion methodExclusion, Object interceptor) {
        DynamicType.Builder<T> builder = new ByteBuddy()
                .subclass(origin);
        Class<?> cachedClass = classCache.get(origin);
        if (cachedClass != null) {
            return cachedClass;
        }
        Class<? extends T> proxied = builder
                .method(ElementMatchers.not(methodExclusion.getExclusionMethod()))
                .intercept(MethodDelegation.to(interceptor))
                .make()
                .load(ProxyClassBuilder.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        classCache.putIfAbsent(origin, proxied);
        return proxied;
    }
}
