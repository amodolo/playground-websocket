package org.playground.pipe.dispatcher.redis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playground.pipe.dispatcher.MessageConsumer;
import org.playground.pipe.dispatcher.Subscriber;
import org.playground.pipe.utils.Pipe;

import javax.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.playground.pipe.dispatcher.redis.RedisConstants.CHANNEL_PREFIX;

/**
 * {@link Subscriber} implementation to deal with Redis message broker.
 */
public class RedisSubscriber implements Subscriber {

    private static final Logger LOG = LogManager.getLogger();
    private static final Map<Pipe, Session> registry = new ConcurrentHashMap<>();
    private final RedisSubscriberService service;
    private final MessageConsumer messageConsumer;

    public RedisSubscriber(RedisSubscriberService service, MessageConsumer messageConsumer) {
        //TODO: CDI will inject these dependencies
        this.service = service;
        this.messageConsumer = messageConsumer;
    }

    @Override
    public boolean subscribe(Pipe pipe, Session session) {
        LOG.trace("subscribe(pipe={}, session={})", pipe, session);
        registry.put(pipe, session);
        service.subscribe(this, CHANNEL_PREFIX + pipe.getId());
        LOG.trace("Trying to get possible available messages to dispatch to this subscriber {}", CHANNEL_PREFIX + pipe.getId());
        return messageConsumer.readAll(pipe.getId(), session);
    }

    @Override
    public boolean unsubscribe(Pipe pipe, Session session) {
        LOG.trace("unsubscribe(pipe={}, session={})", pipe, session);
        service.unsubscribe(CHANNEL_PREFIX + pipe.getId());
        registry.remove(pipe);
        return true;
    }

    /**
     * Notifies the subscriber that there is a new message available for him.
     *
     * @param pipe {@link Pipe} about the user to whom dispatch the message.
     */
    public void onMessage(Pipe pipe) {
        LOG.trace("New message from Redis for pipe {}", pipe);
        boolean read = messageConsumer.readAll(pipe.getId(), registry.get(pipe));
        if (!read) LOG.warn("Some message for pipe {} could not been read", pipe);
    }

    /**
     * Returns {@code true} in case {@link #registry} contains the provided {@code pipe}; {@code false} otherwise.
     *
     * @param pipe {@link Pipe} to be checked for presence in the {@link #registry} map.
     * @return {@code true} in case {@link #registry} contains the provided {@code pipe}; {@code false} otherwise.
     */
    static boolean containsKey(Pipe pipe) {
        return registry.containsKey(pipe);
    }

    /**
     * Returns the {@link #registry} map.
     *
     * @return the {@link #registry} map.
     */
    static Map<Pipe, Session> getRegistry() {
        return registry;
    }
}
