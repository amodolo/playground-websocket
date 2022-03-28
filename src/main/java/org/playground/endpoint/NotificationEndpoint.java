package org.playground.endpoint;

import org.playground.models.Notification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.validation.constraints.NotNull;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(
        value = "/notification/{user}/{wm}",
        encoders = NotificationEncoder.class,
        decoders = NotificationDecoder.class
)
public class NotificationEndpoint {
    private static final Logger LOG = LogManager.getLogger();

    private long user;
    private String wm;
    private Session session;

    @OnOpen
    public void onOpen(Session session, @PathParam("user") @NotNull Long user, @PathParam("wm") @NotNull String wm) {
        LOG.debug("Session opened: {}", session.getId());
        LOG.trace("MaxTextMessageBufferSize: {}", session.getMaxTextMessageBufferSize());
        LOG.trace("MaxBinaryMessageBufferSize: {}", session.getMaxBinaryMessageBufferSize());
        LOG.trace("MaxIdleTimeout: {}", session.getMaxIdleTimeout());

        session.setMaxIdleTimeout(0);

        this.user = user;
        this.wm = wm;
        this.session = session;

        NotificationService.subscribe(this);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        LOG.warn("Session {} closed: {}", session.getId(), reason);
        NotificationService.unsubscribe(this);
    }

    @OnMessage
    public void onMessage(Session session, Notification notification) {
        NotificationService.send(notification);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        LOG.error("new ERROR from session {}", session.getId(), error);
    }

    public long getUser() {
        return user;
    }

    public String getWM() {
        return wm;
    }

    public Session getSession() {
        return session;
    }
}
