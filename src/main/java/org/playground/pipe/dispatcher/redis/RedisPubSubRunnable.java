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

import static org.playground.pipe.dispatcher.redis.RedisConstants.CHANNEL_PREFIX;

public class RedisPubSubRunnable implements Runnable {

    private static final Logger LOG = LogManager.getLogger();
    private final String loopback = "lo_" + UUID.randomUUID();
    /**
     * Map&lt;String, Subscriber&gt; about the subscribers registered in Redis.<br/>
     * The key is the channel value, such as 'geocall:dispatcher:ch:1_wm1'.
     */
    private final Map<String, Subscriber> subscribers = new ConcurrentHashMap<>();
    private final JedisPubSub pubSub = new JedisPubSub() {
        @Override
        public void onMessage(String channel, String message) {
            if (channel.startsWith(CHANNEL_PREFIX)) {
                LOG.trace("Incoming message {} for channel {} is interesting, so the subscriber associated to this channel will be notified about that", message, channel);
                Subscriber subscriber = subscribers.get(channel);
                if (subscriber != null) {
                    LOG.trace("Sending the message {} to the recipient subscriber {}", message, subscriber);
                    subscriber.onMessage();
                }
            } else
                LOG.trace("Incoming message {} for channel {} is not interesting", message, channel);
        }
    };

    @Override
    public void run() {
        try (Jedis client = RedisService.getClient()) {
            LOG.trace("Subscribing to the Redis {} loopback channel", loopback);
            if (client.isConnected()) LOG.debug("Connection established for node {}", InetAddress.getLocalHost());
            client.subscribe(pubSub, loopback);
        } catch (Exception e) {
            LOG.error("connection error", e);
        }
    }

    public void subscribe(Subscriber subscriber, String channel) {
        LOG.trace("subscribe(subscriber={}, channel={})", subscriber, channel);
        // subscription to Redis
        this.pubSub.subscribe(channel);
        // updating the internal subscribers map
        this.subscribers.put(channel, subscriber);
    }

    public void unsubscribe(String channel) {
        LOG.trace("unsubscribe(channel={})", channel);
        // unsubscription from Redis
        this.pubSub.unsubscribe(channel);
        // updating the internal subscribers map
        this.subscribers.remove(channel);
    }

    void unsubscribe() {
        LOG.trace("unsubscribe()");
        this.subscribers.keySet().forEach(channel -> {
            LOG.trace("Unsubscribing channel {}", channel);
            this.pubSub.unsubscribe(channel);
        });
        this.pubSub.unsubscribe();
        this.subscribers.clear();
    }
}
