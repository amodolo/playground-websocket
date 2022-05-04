package org.playground.pipe.utils;

import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.playground.models.User;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.playground.pipe.utils.PipeConfigurator.USER;

@RunWith(MockitoJUnitRunner.class)
public class PipeConfiguratorTest {

    private PipeConfigurator sut;
    @Mock
    private ServerEndpointConfig serverEndpointConfig;
    @Mock
    private HandshakeRequest handshakeRequest;
    @Mock
    private HandshakeResponse handshakeResponse;
    @Mock
    private HttpSession httpSession;
    @Mock
    private Map<String, Object> userProperties;
    @Mock
    private Map<String, List<String>> httpHeaders;
    private EasyRandom easyRandom;

    @Before
    public void setUp() {
        sut = new PipeConfigurator();
        easyRandom = new EasyRandom();
        when(handshakeRequest.getHttpSession()).thenReturn(httpSession);
        when(handshakeResponse.getHeaders()).thenReturn(httpHeaders);
        when(serverEndpointConfig.getUserProperties()).thenReturn(userProperties);
    }

    @Test
    public void modifyHandshake_UserIsPresentInSession_UserIdPutIntoUserProperties() {
        // given
        User user = easyRandom.nextObject(User.class);
        when(httpSession.getAttribute(USER)).thenReturn(user);
        // when
        sut.modifyHandshake(serverEndpointConfig, handshakeRequest, handshakeResponse);
        // then
        verify(userProperties).put(USER, user.getId());
    }

    @Test
    public void modifyHandshake_UserIsNotPresentInSession_UserIdNotPutIntoUserProperties() {
        // when
        sut.modifyHandshake(serverEndpointConfig, handshakeRequest, handshakeResponse);
        // then
        verify(httpHeaders).put(HandshakeResponse.SEC_WEBSOCKET_ACCEPT, Collections.emptyList());
    }
}