package org.playground.endpoint;

import org.playground.models.Notification;
import org.playground.services.RedisService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Transaction;

import javax.websocket.Session;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationService {

    private static final String CHANNEL = "notification_channel_1";
    private static final Logger LOG = LogManager.getLogger();
    private static final Map<Long, Map<String, Session>> registry = new ConcurrentHashMap<>();

    private static Thread subscriberThread;
    private static Subscriber subscriber;

    public static void start() {
        subscriber = new Subscriber();
        subscriberThread = new Thread(subscriber);
        subscriberThread.start();
    }

    public static void stop() {
        subscriber.unsubscribe();
        subscriber = null;
        subscriberThread = null;
    }

    public static void send(Notification notification) {
        RedisService.execute(client -> {
            try {
                long user = notification.getReceiver();
                String key = getKey(user);
                Transaction t = client.multi();
                t.lpush(key, notification.toJson());
                t.expire(key, 10 * 60L); // 10 min
                t.publish(CHANNEL, String.valueOf(user));
                t.exec();
            } catch (Exception e) {
                LOG.error("notification write error", e);
            }
            return null;
        });
    }

    static void subscribe(NotificationEndpoint endpoint) {
        registry.computeIfAbsent(endpoint.getUser(), u -> new HashMap<>()).put(endpoint.getWM(), endpoint.getSession());
        process(endpoint.getUser());
    }

    static void unsubscribe(NotificationEndpoint endpoint) {
        registry.getOrDefault(endpoint.getUser(), new HashMap<>()).remove(endpoint.getWM());
    }


    private static String getKey(long user) {
        return "notification:user:" + user;
    }

    private static void process(long user) {
        if (!registry.containsKey(user)) return;

        RedisService.execute(client -> {
            String key = getKey(user);
            String value;
            while ((value = client.lpop(key)) != null) {
                try {
                    process(user, Notification.fromJson(value));
                } catch (Exception e) {
                    LOG.error("notification read error", e);
                }
            }
            return null;
        });
    }

    private static boolean process(long user, Notification notification) {
        String wm = notification.getReceiverWM();
        if (wm == null) {
            boolean any = false;
            for (Session session: registry.get(user).values()) {
                if (session.isOpen()) {
                    session.getAsyncRemote().sendObject(notification);
                    any = true;
                }
            }
            return any;
        } else {
            Session session = registry.get(user).get(wm);
            if (session != null && session.isOpen()) {
                session.getAsyncRemote().sendObject(notification);
                return true;
            }
            else return false;
        }
    }

    private static class Subscriber extends JedisPubSub implements Runnable {

        @Override
        public void run() {
            try (Jedis client = RedisService.getClient()) {
                if (client.isConnected()) LOG.debug("Connection established for node {}", InetAddress.getLocalHost());
                client.subscribe(this, CHANNEL);
            } catch (Exception e) {
                LOG.error("connection error", e);
            }
        }

        @Override
        public void onMessage(String channel, String message) {
            long user = Long.parseLong(message);
            process(user);
        }
    }
}
