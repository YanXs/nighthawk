package com.github.nightawk.mq.kafka;

public class TracingPayload {

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
        String tp = traceId + "%" + spanId + "%" + parentSpanId + "%" + sampled;
        return tp.getBytes();
    }

    public static TracingPayload fromBytes(byte[] bytes) {
        String tpString = new String(bytes);
        String[] strings = tpString.split("%");
        if (strings.length != 4){
            throw new IllegalArgumentException("bytes illegal, tpString: " + tpString);
        }
        TracingPayload tp = new TracingPayload();
        tp.setTraceId(strings[0]);
        tp.setSpanId(strings[1]);
        tp.setParentSpanId(strings[2]);
        tp.setSampled(strings[3]);
        return tp;
    }
}
