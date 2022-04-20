package org.playground.pipe.dispatcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playground.pipe.model.DispatchError;
import org.playground.pipe.model.Message;
import org.playground.pipe.utils.SessionId;

import javax.websocket.Session;

public class PipeDispatcher implements Publisher, Subscriber {

    private static final Logger LOG = LogManager.getLogger();
    private final Publisher publisher;
    private final Subscriber subscriber;

    public PipeDispatcher(PipeDispatcherFactory factory) {
        this.publisher = factory.createPublisher();
        this.subscriber = factory.createSubscriber();
    }

    @Override
    public DispatchError send(Message message) {
        LOG.trace("send(message={})", message);
        return publisher.send(message);
    }

    @Override
    public boolean subscribe(SessionId sessionId, Session session) {
        LOG.trace("subscribe(sessionId={}, session={})", sessionId, session);
        return subscriber.subscribe(sessionId, session);
    }

    @Override
    public boolean unsubscribe(SessionId sessionId, Session session) {
        LOG.trace("unsubscribe(sessionId={}, session={})", sessionId, session);
        return subscriber.unsubscribe(sessionId, session);
    }

    @Override
    public boolean onMessage() {
        LOG.trace("onMessage()");
        return subscriber.onMessage();
    }
}
