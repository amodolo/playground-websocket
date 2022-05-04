package org.playground.pipe.utils;

import org.playground.models.User;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Collections;

public class PipeConfigurator extends ServerEndpointConfig.Configurator {

    protected static final String USER = "user";

    @Override
    public void modifyHandshake(ServerEndpointConfig config, HandshakeRequest request, HandshakeResponse response) {
        // TODO bearer auth
        HttpSession session = (HttpSession) request.getHttpSession();
        User user = (User) session.getAttribute(USER);
        if (user != null) {
            config.getUserProperties().put(USER, user.getId());
        } else {
            response.getHeaders().put(HandshakeResponse.SEC_WEBSOCKET_ACCEPT, Collections.emptyList()); // TODO can be done in a better way
        }
    }
}
