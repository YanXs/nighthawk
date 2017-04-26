package net.xmoshi.nightawk.redis;

/**
 * @author Xs.
 */
public class JaRedisBuildException extends RuntimeException{

    public JaRedisBuildException(String message) {
        super(message);
    }

    public JaRedisBuildException(String message, Throwable cause) {
        super(message, cause);
    }

    public JaRedisBuildException(Throwable cause) {
        super(cause);
    }
}
