package com.nightawk.core.brave;

import com.github.kristofa.brave.*;
import com.github.kristofa.brave.http.HttpSpanCollector;
import com.github.kristofa.brave.kafka.KafkaSpanCollector;
import com.github.kristofa.brave.scribe.ScribeSpanCollector;
import com.github.kristofa.brave.scribe.ScribeSpanCollectorParams;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BraveFactoryBean implements FactoryBean<Brave>, InitializingBean {

    private static final Integer DEFAULT_FLUSH_INTERVAL = 1;
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("(\\d{1,3}(\\.\\d{1,3}){3}:\\d{1,5},?)+");
    private String serviceName;
    private String transport;
    private String transportAddress;
    private String sampler;
    private String sampleRate;
    private Integer flushInterval = DEFAULT_FLUSH_INTERVAL;

    private Brave brave;

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public void setTransportAddress(String transportAddress) {
        this.transportAddress = transportAddress;
    }

    public void setSampler(String sampler) {
        this.sampler = sampler;
    }

    public void setSampleRate(String sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void setFlushInterval(Integer flushInterval) {
        this.flushInterval = flushInterval;
    }

    @Override
    public Brave getObject() throws Exception {
        if (brave == null) {
            afterPropertiesSet();
        }
        return brave;
    }

    @Override
    public Class<?> getObjectType() {
        return brave == null ? Brave.class : brave.getClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(serviceName, "serviceName must not be null");
        Assert.notNull(transport, "transport must not be null");
        Assert.notNull(transportAddress, "transportAddress must not be null");
        Matcher matcher = ADDRESS_PATTERN.matcher(transportAddress);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("transportAddress format is wrong");
        }
        Brave.Builder builder = new Brave.Builder(serviceName);
        builder.spanCollector(createSpanCollector());
        builder.traceSampler(createSampler());
        brave = builder.build();
    }

    private static final SpanCollectorMetricsHandler DEFAULT_HANDLER = new LoggingSpanCollectorMetricsHandler();


    private SpanCollector createSpanCollector() {
        String lowerTransport = transport.toLowerCase();
        switch (lowerTransport) {
            case "http": {
                HttpSpanCollector.Config.Builder builder = HttpSpanCollector.Config.builder();
                builder.flushInterval(flushInterval);
                String[] addresses = transportAddress.split(",");
                String baseUrl = "http://" + addresses[0];
                return HttpSpanCollector.create(baseUrl, builder.build(), DEFAULT_HANDLER);
            }
            case "kafka": {
                KafkaSpanCollector.Config.Builder builder = KafkaSpanCollector.Config.builder(transportAddress);
                builder.flushInterval(flushInterval);
                return KafkaSpanCollector.create(builder.build(), DEFAULT_HANDLER);
            }
            case "scribe": {
                ScribeSpanCollectorParams params = new ScribeSpanCollectorParams();
                params.setMetricsHandler(DEFAULT_HANDLER);
                String[] addresses = transportAddress.split(",");
                String scribeUrl = addresses[0];
                String host = scribeUrl.substring(0, scribeUrl.indexOf(":"));
                String port = scribeUrl.substring(scribeUrl.indexOf(":") + 1);
                return new ScribeSpanCollector(host, Integer.valueOf(port), params);
            }
            default:
                throw new IllegalArgumentException("unknown transport type, transport: " + transport);
        }
    }

    private Sampler createSampler() {
        if (StringUtils.isEmpty(sampler)) {
            return Sampler.ALWAYS_SAMPLE;
        } else {
            if (StringUtils.isEmpty(sampleRate)) {
                return Sampler.ALWAYS_SAMPLE;
            }
            String lowerSampler = sampler.toLowerCase();
            switch (lowerSampler) {
                case "counting":
                    return CountingSampler.create(Float.valueOf(sampleRate));
                case "boundary":
                    return BoundarySampler.create(Float.valueOf(sampleRate));
                default:
                    throw new IllegalArgumentException("unknown sampler type, sampler: " + sampler);
            }
        }
    }
}
