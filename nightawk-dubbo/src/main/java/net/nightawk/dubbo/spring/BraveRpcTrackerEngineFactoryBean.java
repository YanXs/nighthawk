package net.nightawk.dubbo.spring;

import com.alibaba.dubbo.tracker.TraceIdReporter;
import com.github.kristofa.brave.Brave;
import net.nightawk.dubbo.BraveRpcTrackerEngine;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * @author Xs.
 */
public class BraveRpcTrackerEngineFactoryBean implements FactoryBean<BraveRpcTrackerEngine>, InitializingBean {

    private BraveRpcTrackerEngine braveRpcTrackerEngine;

    private Brave brave;

    private TraceIdReporter traceIdReporter;

    public void setBrave(Brave brave) {
        this.brave = brave;
    }

    public void setTraceIdReporter(TraceIdReporter traceIdReporter) {
        this.traceIdReporter = traceIdReporter;
    }

    @Override
    public BraveRpcTrackerEngine getObject() throws Exception {
        if (braveRpcTrackerEngine == null) {
            afterPropertiesSet();
        }
        return braveRpcTrackerEngine;
    }

    @Override
    public Class<?> getObjectType() {
        return braveRpcTrackerEngine == null ? BraveRpcTrackerEngine.class : braveRpcTrackerEngine.getClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(brave, "brave must not be null");
        braveRpcTrackerEngine = BraveRpcTrackerEngine.create(brave);
        braveRpcTrackerEngine.setTraceIdReporter(traceIdReporter);
    }
}
