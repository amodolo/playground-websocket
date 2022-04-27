package org.playground.pipe.dispatcher;

import org.playground.pipe.utils.Pipe;

import javax.websocket.Session;

/**
 * Interface about all the operations that a subscriber can do.
 */
public interface Subscriber {

    boolean subscribe(Pipe pipe, Session session);

    boolean unsubscribe(Pipe pipe, Session session);
}
