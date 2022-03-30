package org.playground.services;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.HashSet;
import java.util.Set;
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
    public static Set<String> keys(String pattern, String type) throws Exception {

        return execute(j -> {
            HashSet<String> hs = new HashSet<>();
            String cursor = ScanParams.SCAN_POINTER_START;
            do {
                ScanParams sp = new ScanParams();
                sp.match(pattern);

                ScanResult<String> sr;
                if (type != null) sr = j.scan(cursor, sp, type);
                else sr = j.scan(cursor, sp);

                cursor = sr.getCursor();
                hs.addAll(sr.getResult());
            } while (!cursor.equals(ScanParams.SCAN_POINTER_START));

            return hs;
        });

    }

    public static Jedis getClient() {
        return pool.getResource();
    }

}
