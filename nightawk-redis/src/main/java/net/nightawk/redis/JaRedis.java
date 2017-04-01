package net.nightawk.redis;

import net.nightawk.core.intercept.ProxyClassBuilder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;

/**
 * @author Xs.
 */
public class JaRedis {

    static Class<?> proxyClass = ProxyClassBuilder.build(Jedis.class, "Jedis", JaRedisMethodInclusion.INSTANCE, new JaRedisInterceptor());

    public static class Builder {
        private String host = Protocol.DEFAULT_HOST;
        private int port = Protocol.DEFAULT_PORT;
        private int connectionTimeout = Protocol.DEFAULT_TIMEOUT;
        private int soTimeout = Protocol.DEFAULT_TIMEOUT;

        public Builder() {
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder connectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder soTimeout(int soTimeout) {
            this.soTimeout = soTimeout;
            return this;
        }

        public Jedis build() {
            Jedis jedis = null;
            try {
                jedis = (Jedis) proxyClass.getConstructor(String.class, int.class, int.class, int.class)
                        .newInstance(host, port, connectionTimeout, soTimeout);
            } catch (Exception e) {
                throw new JaRedisBuildException(e);
            }
            return jedis;
        }
    }
}
