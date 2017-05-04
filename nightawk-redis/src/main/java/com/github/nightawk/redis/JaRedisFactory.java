package com.github.nightawk.redis;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.InvalidURIException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.JedisURIHelper;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Xs.
 */
public class JaRedisFactory implements PooledObjectFactory<Jedis> {

    private final AtomicReference<HostAndPort> hostAndPort = new AtomicReference<HostAndPort>();
    private final int connectionTimeout;
    private final int soTimeout;
    private final String password;
    private final int database;
    private final String clientName;

    public JaRedisFactory(final String host, final int port, final int connectionTimeout,
                          final int soTimeout, final String password, final int database, final String clientName) {
        this.hostAndPort.set(new HostAndPort(host, port));
        this.connectionTimeout = connectionTimeout;
        this.soTimeout = soTimeout;
        this.password = password;
        this.database = database;
        this.clientName = clientName;
    }

    public JaRedisFactory(final URI uri, final int connectionTimeout, final int soTimeout,
                          final String clientName) {
        if (!JedisURIHelper.isValid(uri)) {
            throw new InvalidURIException(String.format(
                    "Cannot open Redis connection due invalid URI. %s", uri.toString()));
        }
        this.hostAndPort.set(new HostAndPort(uri.getHost(), uri.getPort()));
        this.connectionTimeout = connectionTimeout;
        this.soTimeout = soTimeout;
        this.password = JedisURIHelper.getPassword(uri);
        this.database = JedisURIHelper.getDBIndex(uri);
        this.clientName = clientName;
    }

    public void setHostAndPort(final HostAndPort hostAndPort) {
        this.hostAndPort.set(hostAndPort);
    }

    @Override
    public PooledObject<Jedis> makeObject() throws Exception {
        final HostAndPort hostAndPort = this.hostAndPort.get();
        JaRedis.Builder builder = new JaRedis.Builder();
        builder
                .host(hostAndPort.getHost())
                .port(hostAndPort.getPort())
                .connectionTimeout(connectionTimeout)
                .soTimeout(soTimeout);
        Jedis jedis = builder.build();
        try {
            jedis.connect();
            if (null != this.password) {
                jedis.auth(this.password);
            }
            if (database != 0) {
                jedis.select(database);
            }
            if (clientName != null) {
                jedis.clientSetname(clientName);
            }
        } catch (JedisException je) {
            jedis.close();
            throw je;
        }
        return new DefaultPooledObject<>(jedis);
    }

    @Override
    public void destroyObject(PooledObject<Jedis> pooledJedis) throws Exception {
        final BinaryJedis jedis = pooledJedis.getObject();
        if (jedis.isConnected()) {
            try {
                try {
                    jedis.quit();
                } catch (Exception ignored) {
                }
                jedis.disconnect();
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public boolean validateObject(PooledObject<Jedis> pooledJedis) {
        final BinaryJedis jedis = pooledJedis.getObject();
        try {
            HostAndPort hostAndPort = this.hostAndPort.get();
            String connectionHost = jedis.getClient().getHost();
            int connectionPort = jedis.getClient().getPort();

            return hostAndPort.getHost().equals(connectionHost)
                    && hostAndPort.getPort() == connectionPort && jedis.isConnected()
                    && jedis.ping().equals("PONG");
        } catch (final Exception e) {
            return false;
        }
    }

    @Override
    public void activateObject(PooledObject<Jedis> pooledJedis) throws Exception {
        final BinaryJedis jedis = pooledJedis.getObject();
        if (jedis.getDB() != database) {
            jedis.select(database);
        }
    }

    @Override
    public void passivateObject(PooledObject<Jedis> pooledJedis) throws Exception {
        // NOP
    }
}
