package org.playground.pipe.dispatcher.redis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playground.pipe.dispatcher.Subscriber;
import org.playground.services.RedisService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RedisPubSubRunnable implements Runnable {
    private static final Logger LOG = LogManager.getLogger();

    static final String KEY_PREFIX = "geocall:dispatcher:";
    static final String CHANNEL_PREFIX = KEY_PREFIX + "ch:";

    private final String loopback = "lo_" + UUID.randomUUID();

    private final Map<String, Subscriber> subscribers = new ConcurrentHashMap<>();

    private final JedisPubSub pubSub = new JedisPubSub() {
        @Override
        public void onMessage(String channel, String message) {
            if (channel.startsWith(CHANNEL_PREFIX)) subscribers.get(channel).onMessage();
        }
    };

    @Override
    public void run() {
        try (Jedis client = RedisService.getClient()) {
            if (client.isConnected()) LOG.debug("Connection established for node {}", InetAddress.getLocalHost());
            client.subscribe(pubSub, loopback);
        } catch (Exception e) {
            LOG.error("connection error", e);
        }
    }

    public void subscribe(Subscriber subscriber, String channel) {
        pubSub.subscribe(channel);
        this.subscribers.put(channel, subscriber);
    }

    public void unsubscribe(String channel) {
        pubSub.unsubscribe(channel);
        this.subscribers.remove(channel);
    }
}
