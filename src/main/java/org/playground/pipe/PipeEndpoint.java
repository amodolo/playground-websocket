package org.playground.pipe;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playground.pipe.dispatcher.PipeDispatcher;
import org.playground.pipe.dispatcher.redis.RedisPipeDispatcherFactory;
import org.playground.pipe.model.Message;
import org.playground.pipe.model.TextMessage;
import org.playground.pipe.utils.MessageDecoder;
import org.playground.pipe.utils.MessageEncoder;
import org.playground.pipe.utils.PipeConfigurator;
import org.playground.pipe.utils.SessionId;

import javax.validation.constraints.NotNull;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.net.InetAddress;
import java.net.UnknownHostException;

@ServerEndpoint(
        value = "/w/pipe/{app}",
        decoders = MessageDecoder.class,
        encoders = MessageEncoder.class,
        configurator = PipeConfigurator.class
)
public class PipeEndpoint {
    private static final Logger LOG = LogManager.getLogger();

    private SessionId sessionId;
    private Session session;
    private final PipeDispatcher dispatcher;

    public PipeEndpoint() {
        dispatcher = new PipeDispatcher(new RedisPipeDispatcherFactory());
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("app") @NotNull String app) throws UnknownHostException { //TODO: ma se sollevo un eccezione mi si chiude il canale?
        LOG.debug("session opened: {}", session.getId());
        LOG.trace("maxTextMessageBufferSize: {}", session.getMaxTextMessageBufferSize());
        LOG.trace("maxBinaryMessageBufferSize: {}", session.getMaxBinaryMessageBufferSize());
        LOG.trace("maxIdleTimeout: {}", session.getMaxIdleTimeout());


        long user = (long) session.getUserProperties().get("user");
        this.sessionId = new SessionId(user, app);
        this.session = session;
        session.setMaxIdleTimeout(0);

        boolean subscribed = dispatcher.getSubscriber().subscribe(this.sessionId, session);
        if (!subscribed) throw new IllegalStateException("subscription error");

        Message message = new TextMessage("connected on node " + InetAddress.getLocalHost(), this.sessionId, this.sessionId);
        boolean sent = dispatcher.getPublisher().send(message);
        if (!sent) throw new IllegalStateException("sent error");
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        LOG.warn("session {} closed: {}", session.getId(), reason);
        boolean unsubscribed = dispatcher.getSubscriber().unsubscribe(this.sessionId, session);
        if (!unsubscribed)
            throw new IllegalStateException("unSubscription error"); //TODO: questo come viene recepito lato client?
    }

    @OnMessage
    public void onMessage(Session session, Message message) { //TODO: riesco a veicolare il codice ed il testo dell'errore
        message.setSender(this.sessionId);
        LOG.debug("new MESSAGE from session {}: {}", session.getId(), message);
        boolean sent = dispatcher.getPublisher().send(message);
        if (!sent) throw new IllegalStateException("sent error");
    }

    @OnError
    public void onError(Session session, Throwable error) {
        LOG.error("new ERROR from session {}", session.getId(), error);
    }
}
