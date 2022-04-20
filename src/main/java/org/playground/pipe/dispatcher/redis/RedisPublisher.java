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
import java.util.Set;

import static org.playground.pipe.dispatcher.redis.RedisConstants.CHANNEL_PREFIX;
import static org.playground.pipe.dispatcher.redis.RedisConstants.KEY_PREFIX;

public class RedisPublisher implements Publisher {

    private static final Logger LOG = LogManager.getLogger();
    private final MessageEncoder encoder;

    public RedisPublisher(MessageEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public DispatchError send(Message message) {
        LOG.trace("send(message={})", message);
        // TODO: cosa devo fare se non riesco a spedire il messaggio? il client deve tenere aperto il socket o viene chiuso?
        try (Jedis client = RedisService.getClient()) {
            String key = KEY_PREFIX + message.getTarget().getUserId();
            Set<String> apps = client.smembers(key); // when no member of the set is found, an empty set is returned
            if (apps.isEmpty()) {
                String errorMessage = "There are no windows managers registered at the moment for the recipient " + message.getTarget().getUserId();
                LOG.warn(errorMessage);
                return new DispatchError(message.getTarget(), errorMessage);
            } else {
                LOG.trace("Registered window managers for the user key {}: {}", key, apps);
                if (message.getTarget().getAppId() == null) {
                    LOG.trace("Sending a message to all registered window managers for the recipient " + message.getTarget().getUserId());
                    DispatchError dispatchError = null;
                    boolean atLeastOneMessageCorrectlySent = false;
                    for (String app : apps) {
                        DispatchError error = write(new SessionId(message.getTarget().getUserId(), app), message, client);
                        // TODO: solo l'ultimo DispatchError viene tornato? E' corretto?
                        if (error != null)
                            dispatchError = error;
                        else
                            atLeastOneMessageCorrectlySent = true;
                    }

                    if (!atLeastOneMessageCorrectlySent) {
                        LOG.warn("No message has been sent to any window manager of the recipient");
                        return dispatchError;
                    } else {
                        LOG.trace("The message has been sent to at least one window manager of the recipient");
                        return null;
                    }
                } else if (apps.contains(message.getTarget().getAppId())) {
                    LOG.trace("Sending a message to the registered window manager " + message.getTarget().getAppId() + " for the recipient " + message.getTarget().getUserId());
                    return write(message.getTarget(), message, client);
                } else {
                    String description = "Unknown target " + message.getTarget().getAppId();
                    LOG.warn(description);
                    return new DispatchError(message.getTarget(), description);
                }
            }
        }
    }

    //TODO: spostare in una classe dedicata come fatto con il RedisMessageConsumer?
    private DispatchError write(SessionId target, Message message, Jedis client) {
        LOG.trace("write(target={}, message={}, client={})", target, message, client);
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
            //TODO: capire come gestire gli errori nel contesto web-socket
            return new DispatchError(target, "notification write error", e);
        }

        return null;
    }
}
