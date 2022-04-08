package org.playground.endpoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 */
@ServerEndpoint(
        value = "/w/old/pipe/{wm}",
        decoders = MessageDecoder.class,
        configurator = PipeConfigurator.class
)
public class PipeEndpoint {
    private static final Logger LOG = LogManager.getLogger();

    private long user;
    private String wm;
    private Session session;

    @OnOpen
    public void onOpen(Session session, @PathParam("wm") @NotNull String wm) {
        LOG.debug("session opened: {}", session.getId());
        LOG.trace("maxTextMessageBufferSize: {}", session.getMaxTextMessageBufferSize());
        LOG.trace("maxBinaryMessageBufferSize: {}", session.getMaxBinaryMessageBufferSize());
        LOG.trace("maxIdleTimeout: {}", session.getMaxIdleTimeout());


        this.user = (long) session.getUserProperties().get("user");
        this.wm = wm;
        this.session = session;
//        session.getUserProperties().put("wm", wm);
        session.setMaxIdleTimeout(0);

        PipeDispatcher.subscribe(this);

        try {
            synchronized (this.session) {
                session.getAsyncRemote().sendText("connected on node " + InetAddress.getLocalHost());
            }
        } catch (UnknownHostException e) {
            LOG.error(e);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        LOG.warn("session {} closed: {}", session.getId(), reason);
        PipeDispatcher.unsubscribe(this);
    }

    @OnMessage
    public void onMessage(Session session, Message message) {
        message.setSender(PipeDispatcher.getId(user, wm));
        LOG.debug("new MESSAGE from session {}: {}", session.getId(), message);
        PipeDispatcher.send(message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        LOG.error("new ERROR from session {}", session.getId(), error);
    }

    /**
     * @return
     */
    public long getUser() {
        return user;
    }

    /**
     * @return
     */
    public String getWM() {
        return wm;
    }

    /**
     * @return
     */
    public Session getSession() {
        return session;
    }
}
