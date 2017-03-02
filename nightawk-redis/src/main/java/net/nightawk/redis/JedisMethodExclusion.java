package net.nightawk.redis;

import net.nightawk.core.intercept.MethodExclusion;
import net.nightawk.core.intercept.ObjectMethodElementMatchers;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.not;

/**
 * @author Xs.
 */
public class JedisMethodExclusion implements MethodExclusion {

    public static final JedisMethodExclusion INSTANCE = new JedisMethodExclusion();

    @Override
    @SuppressWarnings("unchecked")
    public ElementMatcher<MethodDescription> getExclusionMethod() {
        return not(ElementMatchers.<MethodDescription>isPrivate()
                .or(ObjectMethodElementMatchers.INSTANCE)
                .or(named("close"))
                .or(named("getDB"))
                .or(named("getClient"))
                .or(named("connect"))
                .or(named("setDataSource"))
                .or(named("resetState"))
                .or(named("clusterSlots"))
                .or(named("checkIsInMultiOrPipeline")));
    }
}
