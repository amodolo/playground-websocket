package org.playground.services;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.function.Function;

public class RedisService {
    private static final JedisPool pool;

    static {
        String host = System.getenv("REDIS_HOST");
        if (host == null) host = "localhost";

        String port = System.getenv("REDIS_PORT");
        if (port == null) port = "6379";

        pool = new JedisPool(host, Integer.parseInt(port));
    }


    public static <T> T execute(Function<Jedis, T> handler) {
        try (Jedis client = pool.getResource()) {
            return handler.apply(client);
        }
    }

    public static Jedis getClient() {
        return pool.getResource();
    }

}
