package net.xmoshi.nightawk.dubbo;

import com.alibaba.dubbo.tracker.TraceId;
import com.alibaba.dubbo.tracker.TraceIdReporter;

public interface Reportable {

    void reportTraceIdIfSampled(TraceIdReporter reporter, TraceId traceId);

}
