package org.playground.pipe.dispatcher;

import org.playground.pipe.utils.SessionId;

import javax.websocket.Session;

public interface Subscriber {
    boolean subscribe(SessionId sessionId, Session session);

    boolean unsubscribe(SessionId sessionId, Session session);

    void onMessage();
}
