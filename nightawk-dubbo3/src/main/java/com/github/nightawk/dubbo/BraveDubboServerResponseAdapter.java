package com.github.nightawk.dubbo;

import com.alibaba.dubbo.tracker.DubboResponse;
import com.github.kristofa.brave.KeyValueAnnotation;
import com.github.kristofa.brave.ServerResponseAdapter;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Xs
 */
public class BraveDubboServerResponseAdapter implements ServerResponseAdapter, com.alibaba.dubbo.tracker.ServerResponseAdapter {

    private final DubboResponse response;

    public BraveDubboServerResponseAdapter(DubboResponse response) {
        this.response = response;
    }

    public boolean isTraceable() {
        return response.isTraceable();
    }

    @Override
    public Collection<KeyValueAnnotation> responseAnnotations() {
        return Collections.emptyList();
    }

}
