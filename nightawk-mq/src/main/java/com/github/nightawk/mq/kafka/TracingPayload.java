package com.github.nightawk.mq.kafka;

public class TracingPayload {

    public static final int TP_LENGTH = 2;

    public static final int TRACE_ID_LENGTH = "traceId".length();

    public static final int SPAN_ID_LENGTH = "spanId".length();

    public static final int PARENT_SPAN_ID_LENGTH = "parentSpanId".length();

    public static final int SAMPLED_LENGTH = "sampled".length();

    public static final int HEADER_LENGTH = TP_LENGTH + TRACE_ID_LENGTH + SPAN_ID_LENGTH + PARENT_SPAN_ID_LENGTH + SAMPLED_LENGTH;

    private String traceId;

    private String spanId;

    private String parentSpanId;

    private String sampled;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(String parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    public String getSampled() {
        return sampled;
    }

    public void setSampled(String sampled) {
        this.sampled = sampled;
    }
}
