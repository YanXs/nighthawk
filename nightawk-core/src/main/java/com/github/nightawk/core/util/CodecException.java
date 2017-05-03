package com.github.nightawk.core.util;

public class CodecException extends RuntimeException {

    public CodecException(Throwable cause) {
        super(cause);
    }

    public CodecException(String message, Throwable cause) {
        super(message, cause);
    }

    public CodecException(String message) {
        super(message);
    }
}
