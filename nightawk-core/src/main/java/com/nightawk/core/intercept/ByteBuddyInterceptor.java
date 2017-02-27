package com.nightawk.core.intercept;

import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * @author Xs.
 */
public interface ByteBuddyInterceptor {

    @RuntimeType
    Object intercept(@SuperCall Callable<?> superMethod, @Origin Method method,
                     @AllArguments Object[] args, @This Object me) throws Exception;

}
