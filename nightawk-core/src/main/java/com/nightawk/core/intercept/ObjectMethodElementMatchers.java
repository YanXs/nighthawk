package com.nightawk.core.intercept;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

/**
 * @author Xs.
 */
public class ObjectMethodElementMatchers implements ElementMatcher<MethodDescription> {

    public final static ObjectMethodElementMatchers INSTANCE = new ObjectMethodElementMatchers();

    private final ElementMatcher.Junction<MethodDescription> matcher;

    private ObjectMethodElementMatchers() {
        matcher = ElementMatchers.named("getClass").and(takesArguments(0))
                .or(named("equals").and(takesArguments(Object.class)))
                .or(named("hashCode").and(takesArguments(0)))
                .or(named("wait").and(takesArguments(0)))
                .or(named("wait").and(takesArguments(long.class)))
                .or(named("wait").and(takesArguments(long.class, int.class)))
                .or(named("toString").and(takesArguments(0)))
                .or(named("clone").and(takesArguments(0)))
                .or(named("notify").and(takesArguments(0)))
                .or(named("notifyAll").and(takesArguments(0)))
                .or(named("finalize").and(takesArguments(0)));
    }
    @Override
    public boolean matches(MethodDescription target) {
        return matcher.matches(target);
    }
}
