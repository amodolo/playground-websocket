package org.playground.pipe.dispatcher.redis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playground.pipe.dispatcher.Publisher;
import org.playground.pipe.model.DispatchError;
import org.playground.pipe.model.Message;
import org.playground.pipe.utils.MessageEncoder;
import org.playground.pipe.utils.Pipe;
import org.playground.services.RedisService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import javax.websocket.EncodeException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.playground.pipe.dispatcher.redis.RedisConstants.CHANNEL_PREFIX;
import static org.playground.pipe.dispatcher.redis.RedisConstants.KEY_PREFIX;

public class RedisPublisher implements Publisher {

    private static final Logger LOG = LogManager.getLogger();
    protected static final String IMPOSSIBLE_TO_SEND_MESSAGE_TO_REDIS = "Max retry number reached: impossible to send the message ('%s') to target ('%s') in Redis";
    protected static final long EXPIRATION_SECONDS = 60L;
    private final MessageEncoder encoder;

    public RedisPublisher(MessageEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public DispatchError send(Message<?> message) {
        LOG.trace("send(message={})", message);

        try (Jedis client = RedisService.getClient()) {
            String key = KEY_PREFIX + message.getTarget().getUserId();
            Set<String> targets = client.smembers(key); // when no member of the set is found, an empty set is returned
            if (targets.isEmpty()) {
                LOG.warn("There are no windows managers registered at the moment for the recipient {}", message.getTarget().getUserId());
                return new DispatchError(DispatchError.ErrorCode.NO_TARGET_AVAILABLE);
            } else {
                LOG.trace("Registered window managers for the user key '{}': {}", key, targets);
                if (message.getTarget().getName() == null) {
                    LOG.trace("Sending a message to all registered window managers for the recipient {}", message.getTarget().getUserId());
                    return write(targets, message, client);
                } else if (targets.contains(message.getTarget().getName())) {
                    LOG.trace("Sending a message to the registered window manager {} for the recipient {}", message.getTarget().getName(), message.getTarget().getUserId());
                    return write(message.getTarget(), message, client);
                } else {
                    LOG.warn("Unknown target {}", message.getTarget().getName());
                    return new DispatchError(DispatchError.ErrorCode.UNKNOWN_TARGET);
                }
            }
        }
    }

    final DispatchError write(Set<String> targets, Message<?> message, Jedis client) {
        AtomicBoolean anyCorrectlySent = new AtomicBoolean(false);
        AtomicReference<DispatchError> dispatchError = new AtomicReference<>();
        targets.stream()
                .map(name -> new Pipe(message.getTarget().getUserId(), name))
                .map(pipe -> write(pipe, message, client))
                .forEach(error -> {
                    if (error == null) anyCorrectlySent.set(true);
                    else dispatchError.set(error);
                });

        if (anyCorrectlySent.get()) return null;
        else return dispatchError.get();
    }

    final DispatchError write(Pipe target, Message<?> message, Jedis client) {
        LOG.trace("write(target={}, message={}, client={})", target, message, client);
        try {
            boolean done;
            int retry = 3;
            String key = KEY_PREFIX + target.getId();
            do {
                Transaction t = client.multi();
                t.lpush(key, encoder.encode(message));
                t.expire(key, EXPIRATION_SECONDS); // 1min
                t.publish(CHANNEL_PREFIX + target.getId(), target.getId());
                if (t.exec() != null) done = true;
                else done = --retry <= 0;
            } while (!done);
            if (retry == 0)
                throw new IllegalStateException(String.format(IMPOSSIBLE_TO_SEND_MESSAGE_TO_REDIS, message, key));
        } catch (EncodeException e) {
            LOG.error("notification write error", e);
            return new DispatchError(DispatchError.ErrorCode.INVALID_MESSAGE);
        }

        return null;
    }
}
