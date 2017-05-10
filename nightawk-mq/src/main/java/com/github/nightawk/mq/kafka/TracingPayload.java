package com.github.nightawk.mq.kafka;

public class TracingPayload {

    public static final int LENGTH = 49;

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

    public byte[] toBytes() {
        if (traceId == null ||
                spanId == null ||
                parentSpanId == null ||
                sampled == null) {
            throw new IllegalStateException("tracing span illegal");
        }

        StringBuilder stringBuilder = new StringBuilder(49);
        stringBuilder
                .append(appendBlankIfNeeded(traceId))
                .append(appendBlankIfNeeded(spanId))
                .append(appendBlankIfNeeded(parentSpanId))
                .append(sampled);
        return stringBuilder.toString().getBytes();
    }

    private String appendBlankIfNeeded(String input) {
        if (input.length() == 15) {
            input = input + " ";
        }
        return input;
    }

    public static TracingPayload fromBytes(byte[] bytes) {
        if (bytes.length != LENGTH) {
            throw new IllegalArgumentException("bytes illegal");
        }
        String tpString = new String(bytes);
        TracingPayload tp = new TracingPayload();
        tp.setTraceId(trimmedString(tpString.substring(0, 16)));
        tp.setSpanId(trimmedString(tpString.substring(17, 32)));
        tp.setParentSpanId(trimmedString(tpString.substring(33, 48)));
        tp.setSampled(tpString.substring(48, 49));
        return tp;
    }

    private static String trimmedString(String input) {
        return input.trim();
    }
}
