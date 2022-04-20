package org.playground.pipe.dispatcher.redis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playground.pipe.dispatcher.MessageConsumer;
import org.playground.pipe.dispatcher.Subscriber;
import org.playground.pipe.utils.SessionId;

import javax.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.playground.pipe.dispatcher.redis.RedisConstants.CHANNEL_PREFIX;

/**
 * {@link Subscriber} implementation to deal with Redis message broker.
 */
public class RedisSubscriber implements Subscriber {

    private static final Logger LOG = LogManager.getLogger();
    //TODO: spostare registry in RedisRemoteMessageBroker.getInstance() e renderla non static???
    private static final Map<SessionId, Session> registry = new ConcurrentHashMap<>();
    private final RedisPubSubRunnable runnable;
    private SessionId sessionId;
    private final MessageConsumer messageConsumer;

    public RedisSubscriber(RedisPubSubRunnable runnable, MessageConsumer messageConsumer) {
        //TODO: CDI will inject this dependencies
        this.runnable = runnable;
        this.messageConsumer = messageConsumer;
    }

    @Override
    public boolean subscribe(SessionId sessionId, Session session) {
        LOG.trace("subscribe(sessionId={}, session={})", sessionId, session);
        this.sessionId = sessionId;
        registry.put(sessionId, session);
        runnable.subscribe(this, CHANNEL_PREFIX + sessionId.getId());
        LOG.trace("Trying to get possible available messages to dispatch to this subscriber {}", CHANNEL_PREFIX + sessionId.getId());
        return messageConsumer.apply(sessionId.getId(), session);
    }

    @Override
    public boolean unsubscribe(SessionId sessionId, Session session) {
        LOG.trace("unsubscribe(sessionId={}, session={})", sessionId, session);
        this.sessionId = null;
        runnable.unsubscribe(CHANNEL_PREFIX + sessionId.getId());
        registry.remove(sessionId);
        return true;
    }

    @Override
    public boolean onMessage() {
        LOG.trace("New message from Redis for sessionId {}", this.sessionId);
        return messageConsumer.apply(sessionId.getId(), registry.get(sessionId));
    }
}
