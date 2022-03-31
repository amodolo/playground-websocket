package org.playground.endpoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playground.services.MonitorService;
import org.playground.services.RedisService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Transaction;

import javax.websocket.Session;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class WebChannelDispatcher {

    private static final String KEY_PREFIX = "web-channel:";
    private static final String CHANNEL_DISPATCH = WebChannelDispatcher.class.getSimpleName() + ":CHANNEL_DISPATCH";
    private static final Logger LOG = LogManager.getLogger();
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
        subscriberThread = null;
    }

    static void subscribe(WebChannelEndpoint endpoint) {
        String key = getId(endpoint);
        registry.put(key, endpoint.getSession());
        read(key);
    }

    static void unsubscribe(WebChannelEndpoint endpoint) {
        registry.remove(getId(endpoint));
    }

    static String getId(long userId, String wmId) {
        return String.format("%d|%s", userId, wmId);
    }

    static String getId(WebChannelEndpoint endpoint) {
        return getId(endpoint.getUser(), endpoint.getWM());
    }

    private static boolean hasSingleTarget(WebMessage message) {
        return message.getTarget().matches("\\d+\\|.+"); // userId|wmId
    }

    /**
     *
     * @param webMessage
     */
    public static void send(WebMessage webMessage) {
        if (hasSingleTarget(webMessage)) {
            // send to single user's window
            write(KEY_PREFIX + webMessage.getTarget(), webMessage);
        } else {
            // send to all user's windows or all windows of every connected depending on the message target
            Map<Long, List<String>> users = MonitorService.getWMReferences(webMessage.getTarget());
            for (Map.Entry<Long, List<String>> entry: users.entrySet()) {
                Long user = entry.getKey();
                List<String> wms = entry.getValue();
                for (String wm: wms) {
                    String target = getId(user, wm);
                    write(KEY_PREFIX + target, webMessage.newTarget(target));
                }
            }
        }
    }

    private static void write(String key, WebMessage webMessage) {
        RedisService.execute(client -> {
            try {
                Transaction t = client.multi();
                t.lpush(key, webMessage.toJson());
                t.expire(key, 10 * 60L); // 10 min
                t.publish(CHANNEL_DISPATCH, webMessage.getTarget());
                t.exec();
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

    private static class Subscriber extends JedisPubSub implements Runnable {

        @Override
        public void run() {
            try (Jedis client = RedisService.getClient()) {
                if (client.isConnected()) LOG.debug("Connection established for node {}", InetAddress.getLocalHost());
                client.subscribe(this, CHANNEL_DISPATCH);
            } catch (Exception e) {
                LOG.error("connection error", e);
            }
        }

        @Override
        public void onMessage(String channel, String msg) {
            read(msg);
        }
    }
}
