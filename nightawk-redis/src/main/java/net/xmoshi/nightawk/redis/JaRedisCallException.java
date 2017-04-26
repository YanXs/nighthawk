package net.xmoshi.nightawk.redis;

/**
 * @author Xs.
 */
public class JaRedisCallException extends RuntimeException {

    public JaRedisCallException(String msg) {
        super(msg);
    }

    public JaRedisCallException(String message, Throwable cause) {
        super(message, cause);
    }

    public JaRedisCallException(Throwable cause) {
        super(cause);
    }
}
