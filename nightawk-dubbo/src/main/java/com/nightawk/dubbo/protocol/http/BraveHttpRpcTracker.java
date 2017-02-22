package com.nightawk.dubbo.protocol.http;

import com.alibaba.dubbo.tracker.RpcTrackerEngine;

/**
 * @author Xs
 */
public class BraveHttpRpcTracker extends FacadeHttpRpcTracker {

    private final RpcTrackerEngine trackerEngine;

    public BraveHttpRpcTracker(RpcTrackerEngine trackerEngine) {
        this.trackerEngine = trackerEngine;
    }

    @Override
    public RpcTrackerEngine trackerEngine() {
        return trackerEngine;
    }
}
