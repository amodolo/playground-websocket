package org.playground.pipe;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playground.pipe.dispatcher.PipeDispatcher;
import org.playground.pipe.dispatcher.redis.RedisPipeDispatcherFactory;
import org.playground.pipe.model.*;
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

@ServerEndpoint(
        value = "/w/pipe/{name}",
        decoders = MessageDecoder.class,
        encoders = MessageEncoder.class,
        configurator = PipeConfigurator.class
)
public class PipeEndpoint {
    private static final Logger LOG = LogManager.getLogger();
    protected static final String CONNECTED_ON_NODE = "connected on node ";
    protected static final String SUBSCRIPTION_ERROR = "subscription error";

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
        if (!subscribed) throw new IllegalStateException(SUBSCRIPTION_ERROR);

        // notifying the client that the subscription has been successful
        InitializedMessage message = new InitializedMessage(CONNECTED_ON_NODE + InetAddress.getLocalHost(), this.pipe);
        session.getAsyncRemote().sendObject(message);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        LOG.warn("session {} closed: {}", session.getId(), reason);
        boolean unsubscribed = dispatcher.unsubscribe(this.pipe, session);
        if (!unsubscribed) LOG.warn("unable to unsubscribe pipe {}", pipe);
    }

    @OnMessage
    public void onMessage(Session session, Message<?> message) {
        LOG.debug("new MESSAGE from session {}: {}", session.getId(), message);
        ProxyMessage<?> proxyMessage = new ProxyMessage<>(message, this.pipe);
        DispatchError dispatchError;
        dispatchError = dispatcher.send(proxyMessage);

        if (dispatchError != null) {
            ErrorMessage errorMessage = new ErrorMessage(dispatchError.getErrorCode(), this.pipe);
            session.getAsyncRemote().sendObject(errorMessage);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        LOG.error("new ERROR from session {}", session.getId(), error);
    }
}
