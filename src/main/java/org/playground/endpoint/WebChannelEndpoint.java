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
        value = "/web-channel/{user}/{wm}",
        decoders = WebMessageDecoder.class
)
public class WebChannelEndpoint {
    private static final Logger LOG = LogManager.getLogger();

    private long user;
    private String wm;
    private Session session;

    @OnOpen
    public void onOpen(Session session, @PathParam("user") @NotNull String user, @PathParam("wm") @NotNull String wm) {
        LOG.debug("session opened: {}", session.getId());
        LOG.trace("maxTextMessageBufferSize: {}", session.getMaxTextMessageBufferSize());
        LOG.trace("maxBinaryMessageBufferSize: {}", session.getMaxBinaryMessageBufferSize());
        LOG.trace("maxIdleTimeout: {}", session.getMaxIdleTimeout());


        this.user = Long.parseLong(user);
        this.wm = wm;
        this.session = session;
        session.getUserProperties().put("user", user);
        session.getUserProperties().put("wm", wm);
        session.setMaxIdleTimeout(0);

        WebChannelDispatcher.subscribe(this);

        try {
            session.getAsyncRemote().sendText("connected on node " + InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            LOG.error(e);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        LOG.warn("session {} closed: {}", session.getId(), reason);
//        session.getAsyncRemote().sendText("disconnected. "+reason);
        WebChannelDispatcher.unsubscribe(this);
    }

    @OnMessage
    public void onMessage(Session session, WebMessage message) {
        message.setSender(WebChannelDispatcher.getId(user, wm));
        LOG.debug("new MESSAGE from session {}: {}", session.getId(), message);
        WebChannelDispatcher.send(message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        LOG.error("new ERROR from session {}", session.getId(), error);
    }

    /**
     *
     * @return
     */
    public long getUser() {
        return user;
    }

    /**
     *
     * @return
     */
    public String getWM() {
        return wm;
    }

    /**
     *
     * @return
     */
    public Session getSession() {
        return session;
    }
}
