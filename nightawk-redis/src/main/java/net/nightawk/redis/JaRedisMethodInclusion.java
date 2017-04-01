package net.nightawk.redis;

import net.nightawk.core.intercept.MethodInclusion;
import net.nightawk.core.intercept.ObjectMethodElementMatchers;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;

/**
 * @author Xs.
 */
public class JaRedisMethodInclusion implements MethodInclusion {

    public static final MethodInclusion INSTANCE = new JaRedisMethodInclusion();

    private JaRedisMethodInclusion(){
    }

    @Override
    @SuppressWarnings("unchecked")
    public ElementMatcher<MethodDescription> getIncludes() {
        return not(ElementMatchers.<MethodDescription>isPrivate()
                .or(ObjectMethodElementMatchers.INSTANCE)
                .or(named("close"))
                .or(named("getDB"))
                .or(named("getClient"))
                .or(named("connect"))
                .or(named("setDataSource"))
                .or(named("resetState"))
                .or(named("clusterSlots"))
                .or(named("isConnected"))
                .or(named("ping"))
                .or(named("checkIsInMultiOrPipeline")));
    }
}
