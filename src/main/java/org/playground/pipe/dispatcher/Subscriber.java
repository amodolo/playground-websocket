package org.playground.pipe.dispatcher;

import org.playground.pipe.utils.SessionId;

import javax.websocket.Session;

/**
 * Interface about all the operations that a subscriber can do.
 */
public interface Subscriber {

    boolean subscribe(SessionId sessionId, Session session);

    boolean unsubscribe(SessionId sessionId, Session session);

    /**
     * Notifies the subscriber that there is a new message available for him.<br/>
     * Returns {@code true} in case of successful result; {@code false} otherwise.
     *
     * @return {@code true} in case of successful result; {@code false} otherwise.
     */
    boolean onMessage();
}
