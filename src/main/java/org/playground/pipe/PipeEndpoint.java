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
import org.playground.pipe.utils.Pipe;

import javax.validation.constraints.NotNull;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.net.InetAddress;
import java.net.UnknownHostException;

//TODO: cambiare {app} con {windowManager}?
@ServerEndpoint(
        value = "/w/pipe/{name}",
        decoders = MessageDecoder.class,
        encoders = MessageEncoder.class,
        configurator = PipeConfigurator.class
)
public class PipeEndpoint {
    private static final Logger LOG = LogManager.getLogger();

    private Pipe pipe;
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
    public void onOpen(Session session, @PathParam("name") @NotNull String name) throws UnknownHostException { //TODO: ma se sollevo un eccezione mi si chiude il canale?
        LOG.debug("session opened: {}", session.getId());
        LOG.trace("maxTextMessageBufferSize: {}", session.getMaxTextMessageBufferSize());
        LOG.trace("maxBinaryMessageBufferSize: {}", session.getMaxBinaryMessageBufferSize());
        LOG.trace("maxIdleTimeout: {}", session.getMaxIdleTimeout());

        // getting info about the user session
        long user = (long) session.getUserProperties().get("user");
        this.pipe = new Pipe(user, name);
        LOG.trace("pipe: {}", pipe);
        session.setMaxIdleTimeout(0);

        // subscribing this user ID + window manager ID to Redis
        boolean subscribed = dispatcher.subscribe(this.pipe, session);
        if (!subscribed) throw new IllegalStateException("subscription error");

        // notifying the client that the subscription has been successful
        Message<?> message = new TextMessage("connected on node " + InetAddress.getLocalHost(), this.pipe, this.pipe);
        DispatchError dispatchError = dispatcher.send(message);
        if (dispatchError != null) throw new IllegalStateException(dispatchError.getDescription(), dispatchError.getException());
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        LOG.warn("session {} closed: {}", session.getId(), reason);
        boolean unsubscribed = dispatcher.unsubscribe(this.pipe, session);
        if (!unsubscribed)
            throw new IllegalStateException("unSubscription error"); //TODO: questo come viene recepito lato client?
    }

    @OnMessage
    public void onMessage(Session session, Message<?> message) { //TODO: riesco a veicolare il codice ed il testo dell'errore
        LOG.debug("new MESSAGE from session {}: {}", session.getId(), message);
        ProxyMessage<?> proxyMessage = new ProxyMessage<>(message, this.pipe);
        DispatchError dispatchError = dispatcher.send(proxyMessage);
        if (dispatchError != null) throw new IllegalStateException(dispatchError.getDescription(), dispatchError.getException());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        LOG.error("new ERROR from session {}", session.getId(), error);
    }
}
