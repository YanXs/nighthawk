package net.xmoshi.nightawk.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.tracker.RpcTrackerEngine;
import com.alibaba.dubbo.tracker.TraceIdReporter;
import com.github.kristofa.brave.*;
import com.github.kristofa.brave.http.HttpSpanCollector;
import com.github.kristofa.brave.kafka.KafkaSpanCollector;
import com.github.kristofa.brave.scribe.ScribeSpanCollector;
import com.github.kristofa.brave.scribe.ScribeSpanCollectorParams;
import net.xmoshi.nightawk.core.brave.LoggingSpanCollectorMetricsHandler;

/**
 * @author Xs
 */
public class BraveRpcTrackerEngine implements RpcTrackerEngine {

    private static final SpanCollectorMetricsHandler DEFAULT_HANDLER = new LoggingSpanCollectorMetricsHandler();

    private final Brave brave;

    private TraceIdReporter traceIdReporter;

    BraveRpcTrackerEngine(URL url) {
        Brave.Builder builder = new Brave.Builder(url.getParameter("application"));
        builder.spanCollector(createSpanCollector(url));
        builder.traceSampler(createSampler(url));
        brave = builder.build();
    }

    BraveRpcTrackerEngine(Brave brave) {
        this.brave = brave;
    }

    public void setTraceIdReporter(TraceIdReporter traceIdReporter) {
        this.traceIdReporter = traceIdReporter;
    }

    private SpanCollector createSpanCollector(URL url) {
        String transport = url.getParameter("transport", "http");
        Integer flushInterval = url.getParameter("flushinterval", 1);
        switch (transport) {
            case "http": {
                HttpSpanCollector.Config.Builder builder = HttpSpanCollector.Config.builder();
                builder.flushInterval(flushInterval);
                String baseUrl = "http://" + url.getHost() + ":" + url.getPort();
                return HttpSpanCollector.create(baseUrl, builder.build(), DEFAULT_HANDLER);
            }
            case "kafka": {
                String address = url.getParameter("address");
                if (StringUtils.isEmpty(address)) {
                    throw new IllegalArgumentException("transport address must not be null");
                }
                KafkaSpanCollector.Config.Builder builder = KafkaSpanCollector.Config.builder(address);
                builder.flushInterval(flushInterval);
                return KafkaSpanCollector.create(builder.build(), DEFAULT_HANDLER);
            }
            case "scribe":
                ScribeSpanCollectorParams params = new ScribeSpanCollectorParams();
                params.setMetricsHandler(DEFAULT_HANDLER);
                return new ScribeSpanCollector(url.getHost(), url.getPort(), params);
            default:
                throw new IllegalArgumentException("unknown transport type, transport: " + transport +
                        ", supported transport: { http, kafka, scribe }");
        }
    }

    private Sampler createSampler(URL url) {
        String sampler = url.getParameter("sampler");
        if (StringUtils.isEmpty(sampler)) {
            return Sampler.ALWAYS_SAMPLE;
        } else {
            String rate = url.getParameter("rate");
            if (StringUtils.isEmpty(rate)) {
                return Sampler.ALWAYS_SAMPLE;
            }
            switch (sampler) {
                case "counting":
                    return CountingSampler.create(Float.valueOf(rate));
                case "boundary":
                    return BoundarySampler.create(Float.valueOf(rate));
                default:
                    throw new IllegalArgumentException("unknown sampler type, sampler: " + sampler +
                            ", supported sampler: { counting, boundary }");
            }
        }
    }

    public static BraveRpcTrackerEngine create(URL url) {
        return new BraveRpcTrackerEngine(url);
    }

    public static BraveRpcTrackerEngine create(Brave brave) {
        return new BraveRpcTrackerEngine(brave);
    }

    @Override
    public ClientRequestInterceptor clientRequestInterceptor() {
        return brave.clientRequestInterceptor();
    }

    @Override
    public ClientResponseInterceptor clientResponseInterceptor() {
        return brave.clientResponseInterceptor();
    }

    @Override
    public ServerRequestInterceptor serverRequestInterceptor() {
        return brave.serverRequestInterceptor();
    }

    @Override
    public ServerResponseInterceptor serverResponseInterceptor() {
        return brave.serverResponseInterceptor();
    }

    @Override
    public TraceIdReporter traceIdReporter() {
        return traceIdReporter;
    }
}
