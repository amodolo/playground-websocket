package org.playground.pipe;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playground.pipe.dispatcher.PipeDispatcher;
import org.playground.pipe.dispatcher.redis.RedisPipeDispatcherFactory;
import org.playground.pipe.model.DispatchError;
import org.playground.pipe.model.Message;
import org.playground.pipe.model.ProxyMessage;
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

//TODO: cambiare {app} con {windowManager}?
@ServerEndpoint(
        value = "/w/pipe/{app}",
        decoders = MessageDecoder.class,
        encoders = MessageEncoder.class,
        configurator = PipeConfigurator.class
)
public class PipeEndpoint {
    private static final Logger LOG = LogManager.getLogger();

    private SessionId sessionId;
    private final PipeDispatcher dispatcher;

    @SuppressWarnings("unused")
    public PipeEndpoint() {
        //TODO: CDI will inject this dependency
        this(new PipeDispatcher(new RedisPipeDispatcherFactory()));
    }

    public PipeEndpoint(PipeDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("app") @NotNull String app) throws UnknownHostException { //TODO: ma se sollevo un eccezione mi si chiude il canale?
        LOG.debug("session opened: {}", session.getId());
        LOG.trace("maxTextMessageBufferSize: {}", session.getMaxTextMessageBufferSize());
        LOG.trace("maxBinaryMessageBufferSize: {}", session.getMaxBinaryMessageBufferSize());
        LOG.trace("maxIdleTimeout: {}", session.getMaxIdleTimeout());

        // getting info about the user session
        long userId = (long) session.getUserProperties().get("user");
        this.sessionId = new SessionId(userId, app);
        LOG.trace("sessionId: {}", sessionId);
        session.setMaxIdleTimeout(0);

        // subscribing this user ID + window manager ID to Redis
        boolean subscribed = dispatcher.subscribe(this.sessionId, session);
        if (!subscribed) throw new IllegalStateException("subscription error");

        // notifying the client that the subscription has been successful
        Message message = new TextMessage("connected on node " + InetAddress.getLocalHost(), this.sessionId, this.sessionId);
        DispatchError dispatchError = dispatcher.send(message);
        if (dispatchError != null) throw new IllegalStateException("sent error:");
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        LOG.warn("session {} closed: {}", session.getId(), reason);
        boolean unsubscribed = dispatcher.unsubscribe(this.sessionId, session);
        if (!unsubscribed)
            throw new IllegalStateException("unSubscription error"); //TODO: questo come viene recepito lato client?
    }

    @OnMessage
    public void onMessage(Session session, Message message) { //TODO: riesco a veicolare il codice ed il testo dell'errore
        LOG.debug("new MESSAGE from session {}: {}", session.getId(), message);
        ProxyMessage proxyMessage = new ProxyMessage(message, this.sessionId);
        DispatchError dispatchError = dispatcher.send(proxyMessage);
        // FIXME: forse meglio sollevare una PipeException, in questo modo tengo traccia anche dei DispatchErrors???
        if (dispatchError != null)
            throw new IllegalStateException(String.format("sent error: %s", dispatchError));
    }

    @OnError
    public void onError(Session session, Throwable error) {
        LOG.error(String.format("new ERROR from session %s", session.getId()), error);
    }
}