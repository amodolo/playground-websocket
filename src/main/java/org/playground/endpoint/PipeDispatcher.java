package org.playground.endpoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playground.models.WindowManager;
import org.playground.services.RedisService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Transaction;

import javax.websocket.Session;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 */
public class PipeDispatcher {

    private static final Logger LOG = LogManager.getLogger();
    private static final String KEY_PREFIX = "geocall:dispatcher:";
    private static final String CHANNEL_PREFIX = KEY_PREFIX + "ch:";
    private static final Map<String, Session> registry = new HashMap<>();

    private static Thread subscriberThread;
    private static Subscriber subscriber;

    /**
     *
     */
    public static void start() {
        subscriber = new Subscriber();
        subscriberThread = new Thread(subscriber);
        subscriberThread.start();
    }

    /**
     *
     */
    public static void stop() {
        subscriber.unsubscribe();
        subscriber = null;
        subscriberThread.interrupt();
        subscriberThread = null;
    }

    /**
     * @param wm
     */
    public static void touch(WindowManager wm) {

        try (Jedis client = RedisService.getClient()) {
            String key = KEY_PREFIX + wm.getUser().getId();
            boolean done = false;
            do {
                Transaction t = client.multi();
                t.sadd(key, wm.getId());
                t.expire(key, 60 * 60L); // 1h SESSION duration
                if (t.exec() != null) done = true;
            } while (!done);
        }
    }

    /**
     * @param wm
     */
    public static void untouch(WindowManager wm) {
        try (Jedis client = RedisService.getClient()) {
            String key = KEY_PREFIX + wm.getUser().getId();
            client.srem(key, wm.getId());
        }
    }

    static void subscribe(PipeEndpoint endpoint) {
        String key = getId(endpoint);
        registry.put(key, endpoint.getSession());
        read(key);
        subscriber.subscribe(CHANNEL_PREFIX + getId(endpoint));
    }

    static void unsubscribe(PipeEndpoint endpoint) {
        subscriber.unsubscribe(CHANNEL_PREFIX + getId(endpoint));
        registry.remove(getId(endpoint));
    }

    static String getId(long userId, String wmId) {
        return String.format("%d_%s", userId, wmId);
    }

    static String getId(PipeEndpoint endpoint) {
        return getId(endpoint.getUser(), endpoint.getWM());
    }

    /**
     * @param message
     */
    public static void send(Message message) {
        try {
            try (Jedis client = RedisService.getClient()) {
                String key = KEY_PREFIX + message.getTargetUser();
                Set<String> wms = client.smembers(key);
                if (message.getTargetWm() == null) {
                    wms.forEach(wm -> write(getId(message.getTargetUser(), wm), message));
                } else if (wms.contains(message.getTargetWm())) {
                    write(message.getTarget(), message);
                }
            }
        } catch (Exception e) {
            LOG.error("error retrieving sessions", e);
        }
    }

    private static void write(String key, Message message) {
        RedisService.execute(client -> {
            try {
                boolean done = false;
                do {
                    Transaction t = client.multi();
                    t.lpush(KEY_PREFIX + key, message.toJson());
                    t.expire(KEY_PREFIX + key, 60L); // 1min
                    t.publish(CHANNEL_PREFIX + key, key);
                    if (t.exec() != null) done = true;
                } while (!done);
            } catch (Exception e) {
                LOG.error("notification write error", e);
            }
            return null;
        });
    }

    private static void read(String key) {
        Session session = registry.get(key);
        if (session == null || !session.isOpen()) return;

        RedisService.execute(client -> {
            String value;
            while ((value = client.lpop(KEY_PREFIX + key)) != null) {
                synchronized (session) {
                    session.getAsyncRemote().sendText(value);
                }
            }
            return null;
        });
    }

    private PipeDispatcher() {
        // this class contains only static methods
    }

    private static class Subscriber extends JedisPubSub implements Runnable {
        private final String loopback = "lo_" + UUID.randomUUID();

        @Override
        public void run() {
            try (Jedis client = RedisService.getClient()) {
                if (client.isConnected()) LOG.debug("Connection established for node {}", InetAddress.getLocalHost());
                client.subscribe(this, loopback);
            } catch (Exception e) {
                LOG.error("connection error", e);
            }
        }

        @Override
        public void onMessage(String channel, String msg) {
            if (channel.startsWith(CHANNEL_PREFIX)) {
                read(msg);
            }
        }
    }
}
