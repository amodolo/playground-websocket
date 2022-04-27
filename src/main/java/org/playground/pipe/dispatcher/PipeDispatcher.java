package org.playground.pipe.dispatcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playground.models.WindowManager;
import org.playground.pipe.model.DispatchError;
import org.playground.pipe.model.Message;
import org.playground.pipe.utils.Pipe;

import javax.websocket.Session;

public class PipeDispatcher {

    private static final Logger LOG = LogManager.getLogger();
    private final Publisher publisher;
    private final Subscriber subscriber;
    private final Register register;

    public PipeDispatcher(PipeDispatcherFactory factory) {
        this.publisher = factory.createPublisher();
        this.subscriber = factory.createSubscriber();
        this.register = factory.createRegister();
    }

    public DispatchError send(Message<?> message) {
        LOG.trace("send(message={})", message);
        return publisher.send(message);
    }

    public boolean subscribe(Pipe pipe, Session session) {
        LOG.trace("subscribe(pipe={}, session={})", pipe, session);
        return subscriber.subscribe(pipe, session);
    }

    public boolean unsubscribe(Pipe pipe, Session session) {
        LOG.trace("unsubscribe(pipe={}, session={})", pipe, session);
        return subscriber.unsubscribe(pipe, session);
    }

    public boolean touch(WindowManager wm) {
        LOG.trace("touch(windowManager={})", wm.getId());
        return this.register.touch(wm);
    }

    public boolean deTouch(WindowManager wm) {
        LOG.trace("deTouch(windowManager={})", wm.getId());
        return this.register.deTouch(wm);
    }
}
