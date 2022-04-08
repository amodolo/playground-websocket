package org.playground.pipe.dispatcher.redis;

import org.playground.pipe.dispatcher.Subscriber;
import org.playground.pipe.utils.SessionId;
import org.playground.services.RedisService;
import redis.clients.jedis.Jedis;

import javax.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RedisSubscriber implements Subscriber {
    private static Map<SessionId, Session> registry = new ConcurrentHashMap<>();
    private static final String KEY_PREFIX = RedisPubSubRunnable.KEY_PREFIX;
    private static final String CHANNEL_PREFIX = RedisPubSubRunnable.CHANNEL_PREFIX;

    private RedisPubSubRunnable runnable; //TODO: devo linkare questa...chi me lo istanzia?
    private SessionId sessionId;


    @Override
    public boolean subscribe(SessionId sessionId, Session session) {
        this.sessionId = sessionId;
        registry.put(sessionId, session);
        runnable.subscribe(this, CHANNEL_PREFIX + sessionId.getId());
        return read(sessionId);
    }

    @Override
    public boolean unsubscribe(SessionId sessionId, Session session) {
        this.sessionId = null;
        runnable.unsubscribe(CHANNEL_PREFIX + sessionId.getId());
        registry.remove(sessionId);
        return true;
    }

    @Override
    public void onMessage() {
        read(this.sessionId);
    }

    private boolean read(SessionId sessionId) {
        Session session = registry.get(sessionId);
        if (session == null || !session.isOpen()) return false;

        try (Jedis client = RedisService.getClient()) {
            String value;
            while ((value = client.lpop(KEY_PREFIX + sessionId.getId())) != null) {
                synchronized (session) {
                    session.getAsyncRemote().sendText(value);
                }
            }
        }
        return true;
    }

}
