package net.nightawk.dubbo.protocol;

import com.alibaba.dubbo.tracker.TraceId;
import com.alibaba.dubbo.tracker.TraceIdReporter;

public interface Reportable {

    void reportTraceIdIfSampled(TraceIdReporter reporter, TraceId traceId);

}
