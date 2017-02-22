package com.nightawk.dubbo.spring;

import com.github.kristofa.brave.Brave;
import com.nightawk.dubbo.BraveRpcTrackerEngine;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * @author Xs.
 */
public class BraveRpcTrackerEngineFactoryBean implements FactoryBean<BraveRpcTrackerEngine>, InitializingBean {

    private BraveRpcTrackerEngine braveRpcTrackerEngine;

    private Brave brave;

    public void setBrave(Brave brave) {
        this.brave = brave;
    }

    @Override
    public BraveRpcTrackerEngine getObject() throws Exception {
        if (braveRpcTrackerEngine == null) {
            afterPropertiesSet();
        }
        return null;
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
    }
}
