package net.nightawk.dubbo.protocol;

import com.alibaba.dubbo.tracker.TraceId;

public class CertainTraceId extends TraceId {

    public CertainTraceId(String traceId) {
        super(Boolean.TRUE, traceId);
    }

}
