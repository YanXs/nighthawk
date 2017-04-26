package net.xmoshi.nightawk.core.brave;

/**
 * @author Xs.
 */
public enum SampleType {

    Always("always"),

    Never("never"),

    CountingSample("counting"),

    BoundarySample("boundary");

    private final String value;

    SampleType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
