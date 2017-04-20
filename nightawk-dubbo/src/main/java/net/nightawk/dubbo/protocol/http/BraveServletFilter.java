package net.nightawk.dubbo.protocol.http;

import com.alibaba.dubbo.tracker.*;
import com.alibaba.dubbo.tracker.http.ServletFilter;
import com.github.kristofa.brave.ServerRequestInterceptor;
import com.github.kristofa.brave.ServerResponseInterceptor;
import com.github.kristofa.brave.http.SpanNameProvider;
import net.nightawk.dubbo.protocol.CertainTraceId;
import net.nightawk.dubbo.protocol.Reportable;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class BraveServletFilter implements ServletFilter, Reportable {

    private final Filter delegate;

    private final RpcTrackerEngine trackerEngine;

    public BraveServletFilter(RpcTracker rpcTracker, SpanNameProvider spanNameProvider) {
        trackerEngine = rpcTracker.trackerEngine();
        delegate = new com.github.kristofa.brave.servlet.BraveServletFilter((ServerRequestInterceptor) trackerEngine.serverRequestInterceptor(),
                (ServerResponseInterceptor) trackerEngine.serverResponseInterceptor(), spanNameProvider);

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        delegate.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (isSampleNeeded(request)) {
            reportTraceIdIfSampled(trackerEngine.traceIdReporter(), new CertainTraceId(getTraceId(request)));
            delegate.doFilter(request, response, chain);
        } else {
            reportTraceIdIfSampled(trackerEngine.traceIdReporter(), TraceId.NOT_TRACE);
            chain.doFilter(request, response);
        }
    }

    private boolean isSampleNeeded(ServletRequest request) {
        return ((HttpServletRequest) request).getHeader(RpcAttachment.Sampled.getName()) != null;
    }

    private String getTraceId(ServletRequest request) {
        return ((HttpServletRequest) request).getHeader(RpcAttachment.TraceId.getName());
    }

    @Override
    public void destroy() {
        delegate.destroy();
    }

    @Override
    public void reportTraceIdIfSampled(TraceIdReporter reporter, TraceId traceId) {
        if (reporter != null) {
            reporter.report(traceId);
        }
    }
}
