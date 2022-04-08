package org.playground.pipe.dispatcher.redis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playground.pipe.dispatcher.Publisher;
import org.playground.pipe.model.DispatchError;
import org.playground.pipe.model.Message;
import org.playground.pipe.utils.MessageEncoder;
import org.playground.pipe.utils.SessionId;
import org.playground.services.RedisService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import javax.websocket.EncodeException;
import java.util.HashSet;
import java.util.Set;

public class RedisPublisher implements Publisher {

    private static final Logger LOG = LogManager.getLogger();
    private static final String KEY_PREFIX = RedisPubSubRunnable.KEY_PREFIX;
    private static final String CHANNEL_PREFIX = RedisPubSubRunnable.CHANNEL_PREFIX;
    private final MessageEncoder encoder;

    public RedisPublisher() {
        this.encoder = new MessageEncoder();
    }

    @Override
    public Set<DispatchError> send(Message message) {
        // TODO: cosa devo fare se non riesco a spedire il messaggio? il client deve tenere aperto il socket o viene chiuso?
        Set<DispatchError> errors = new HashSet<>();

        try (Jedis client = RedisService.getClient()) {
            String key = KEY_PREFIX + message.getTarget().getUserId();
            Set<String> apps = client.smembers(key);
            if (message.getTarget().getAppId() == null) {
                apps.forEach(app -> {
                    DispatchError error = write(new SessionId(message.getTarget().getUserId(), app), message, client);
                    if (error != null) errors.add(error);
                });
            } else if (apps.contains(message.getTarget().getAppId())) {
                DispatchError error = write(message.getTarget(), message, client);
                if (error != null) errors.add(error);
            } else {
                String description = "unknown target "+message.getTarget().getAppId();
                LOG.warn(description);
                errors.add(new DispatchError(message.getTarget(), description));
            }
        }

        return errors;
    }

    private DispatchError write(SessionId target, Message message, Jedis client) {
        try {
            boolean done = false;
            do {
                Transaction t = client.multi();
                t.lpush(KEY_PREFIX + target.getId(), encoder.encode(message));
                t.expire(KEY_PREFIX + target.getId(), 60L); // 1min
                t.publish(CHANNEL_PREFIX + target.getId(), target.getId());
                if (t.exec() != null) done = true;
            } while (!done);
            //TODO: e se il server non risponde perchè giù? questo cicla all'infinito (magari ha senso avere un max retry)
        } catch (EncodeException e) {
            LOG.error("notification write error", e);
            return new DispatchError(target, "notification write error", e);
        }

        return null;
    }
}
