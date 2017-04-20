package net.nightawk.dubbo;

import com.alibaba.dubbo.tracker.TraceId;

public class ThreadLocalTraceIdWatcher implements TraceIdWatcher {

    private final ThreadLocal<TraceId> traceId = new ThreadLocal<TraceId>() {
        @Override
        protected TraceId initialValue() {
            return TraceId.NOT_TRACE;
        }
    };

    @Override
    public void report(TraceId traceId) {
        this.traceId.set(traceId);
    }

    @Override
    public TraceId getTraceId() {
        return traceId.get();
    }
}
