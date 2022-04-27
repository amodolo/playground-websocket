package org.playground.pipe.dispatcher.redis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playground.pipe.dispatcher.MessageConsumer;
import org.playground.services.RedisService;

import javax.websocket.Session;

import static org.playground.pipe.dispatcher.redis.RedisConstants.KEY_PREFIX;

/**
 * {@link MessageConsumer} implementation to read a message from Redis message broker and dispatch it to the recipient.
 */
class RedisMessageConsumer implements MessageConsumer {

    private static final Logger LOG = LogManager.getLogger();

    @Override
    public Boolean readAll(String recipientKey, Session recipientSession) {
        LOG.trace("apply(recipientKey={}, recipientSession={})", recipientKey, recipientSession);
        if (recipientSession == null || !recipientSession.isOpen()) {
            LOG.debug("the session {} is no longer open, impossible to read messages and send them to it", recipientSession);
            return false;
        }

        return RedisService.execute(client -> {
            String key = KEY_PREFIX + recipientKey;
            String value;
            while ((value = client.lpop(key)) != null) {
                synchronized (recipientSession) {
                    LOG.trace("Sending the message {} to the recipient session {}", value, recipientSession);
                    recipientSession.getAsyncRemote().sendText(value);
                }
            }
            return true;
        });
    }
}
