package org.playground.pipe;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.assertj.core.groups.Tuple;
import org.jeasy.random.EasyRandom;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.playground.pipe.dispatcher.PipeDispatcher;
import org.playground.pipe.model.*;
import org.playground.pipe.utils.Pipe;

import javax.websocket.CloseReason;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.playground.pipe.PipeEndpoint.SUBSCRIPTION_ERROR;

@RunWith(MockitoJUnitRunner.class)
public class PipeEndpointTest {

    @InjectMocks
    private PipeEndpoint sut;
    @Mock
    private PipeDispatcher dispatcher;
    @Mock
    private Session session;
    @Mock
    private Map<String, Object> userProperties;
    @Mock
    private RemoteEndpoint.Async async;
    @Mock
    private Appender appender;
    @Captor
    private ArgumentCaptor<LogEvent> logEventCaptor;
    @Mock
    private Pipe pipe;
    @Captor
    private ArgumentCaptor<Message<?>> messageCaptor;
    @Captor
    private ArgumentCaptor<Pipe> pipeCaptor;
    @Captor
    private ArgumentCaptor<InitializedMessage> initializedMessageCaptor;
    @Captor
    private ArgumentCaptor<ErrorMessage> errorMessageCaptor;
    private EasyRandom easyRandom;
    private String name;
    private Logger logger;

    @Before
    public void setUp() {
        easyRandom = new EasyRandom();
        Long userId = easyRandom.nextObject(Long.class);
        name = easyRandom.nextObject(String.class);
        String sessionId = easyRandom.nextObject(String.class);
        when(pipe.getUserId()).thenReturn(userId);
        when(pipe.getName()).thenReturn(name);
        when(session.getUserProperties()).thenReturn(userProperties);
        when(session.getAsyncRemote()).thenReturn(async);
        when(session.getId()).thenReturn(sessionId);
        when(userProperties.get("user")).thenReturn(userId);

        // mocking log4j
        when(appender.getName()).thenReturn("appender");
        when(appender.isStarted()).thenReturn(true);
        logger = (Logger) LogManager.getLogger(PipeEndpoint.class);
        logger.addAppender(appender);
        logger.setLevel(Level.TRACE);
    }

    @After
    public void tearDown() {
        logger.removeAppender(appender);
    }

    @Test
    public void onOpen_SubscribeCalled_CorrectlySubscribed() throws UnknownHostException {
        // given
        when(dispatcher.subscribe(any(Pipe.class), eq(session))).thenReturn(true);
        // when
        sut.onOpen(session, name);
        // then
        verify(dispatcher).subscribe(pipeCaptor.capture(), eq(session));
        assertThat(pipeCaptor.getValue()).isNotNull().usingRecursiveComparison().isEqualTo(pipe);
    }

    @Test
    public void onOpen_SubscribeCalled_NotCorrectlySubscribed() {
        // given
        when(dispatcher.subscribe(any(Pipe.class), eq(session))).thenReturn(false);
        // when
        assertThatThrownBy(() -> sut.onOpen(session, name))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(SUBSCRIPTION_ERROR);
    }

    @Test
    public void onOpen_SubscribeCalled_SessionReceivesTheInitializedMessage() throws UnknownHostException {
        // given
        when(dispatcher.subscribe(any(Pipe.class), eq(session))).thenReturn(true);
        InitializedMessage expected = new InitializedMessage(PipeEndpoint.CONNECTED_ON_NODE + InetAddress.getLocalHost(), pipe);
        // when
        sut.onOpen(session, name);
        // then
        verify(async).sendObject(initializedMessageCaptor.capture());
        verify(dispatcher).subscribe(pipeCaptor.capture(), eq(session));
        assertThat(initializedMessageCaptor.getValue()).isNotNull().usingRecursiveComparison().isEqualTo(expected);
        assertThat(pipeCaptor.getValue()).isNotNull().usingRecursiveComparison().isEqualTo(pipe);
    }

    @Test
    public void onClose_UnsubscribeCalled_CorrectlyUnsubscribed() {
        // given
        when(dispatcher.unsubscribe(pipe, session)).thenReturn(true);
        CloseReason closeReason = new CloseReason(easyRandom.nextObject(CloseReason.CloseCodes.class), easyRandom.nextObject(String.class));
        // when
        sut.onClose(session, closeReason);
        // then
        verify(dispatcher).unsubscribe(pipe, session);
        verify(appender).append(logEventCaptor.capture());
        List<LogEvent> events = logEventCaptor.getAllValues();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).extracting(LogEvent::getLevel, LogEvent::getLoggerName)
                .containsExactly(Level.WARN, PipeEndpoint.class.getName());
    }

    @Test
    public void onClose_UnsubscribeCalled_NotCorrectlyUnsubscribed() {
        // given
        when(dispatcher.unsubscribe(pipe, session)).thenReturn(false);
        CloseReason closeReason = new CloseReason(easyRandom.nextObject(CloseReason.CloseCodes.class), easyRandom.nextObject(String.class));
        // when
        sut.onClose(session, closeReason);
        // then
        verify(dispatcher).unsubscribe(pipe, session);
        verify(appender, times(2)).append(logEventCaptor.capture());
        List<LogEvent> events = logEventCaptor.getAllValues();
        assertThat(events).hasSize(2);
        assertThat(events).extracting(LogEvent::getLevel, LogEvent::getLoggerName)
                .containsExactly(new Tuple(Level.WARN, PipeEndpoint.class.getName()),
                        new Tuple(Level.WARN, PipeEndpoint.class.getName()));
    }

    @Test
    public void onMessage_SendCalled_MessageCorrectlySent() {
        // given
        Message<String> message = easyRandom.nextObject(TextMessage.class);
        when(dispatcher.send(any(Message.class))).thenReturn(null);
        ProxyMessage<String> expected = new ProxyMessage<>(message, pipe);
        // when
        sut.onMessage(session, message);
        // then
        verify(dispatcher).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue()).isNotNull().isEqualTo(expected);
        verify(async, never()).sendObject(any(Object.class));
    }

    @Test
    public void onMessage_SendCalled_MessageNotCorrectlySent() {
        // given
        Message<String> message = easyRandom.nextObject(TextMessage.class);
        DispatchError dispatchError = easyRandom.nextObject(DispatchError.class);
        when(dispatcher.send(any(Message.class))).thenReturn(dispatchError);
        ErrorMessage expected = new ErrorMessage(dispatchError.getErrorCode(), pipe);
        // when
        sut.onMessage(session, message);
        // then
        verify(async).sendObject(errorMessageCaptor.capture());
        assertThat(errorMessageCaptor.getValue()).isNotNull().isEqualTo(expected);
    }

    @Test
    public void onError_OnceCalled_LogsTheErrorOccurred() {
        // given
        Throwable throwable = easyRandom.nextObject(Throwable.class);
        // when
        sut.onError(session, throwable);
        // then
        verify(appender).append(logEventCaptor.capture());
        List<LogEvent> events = logEventCaptor.getAllValues();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).extracting(LogEvent::getLevel, LogEvent::getLoggerName, e -> e.getMessage().getFormattedMessage())
                .containsExactly(Level.ERROR, PipeEndpoint.class.getName(), "new ERROR from session " + session.getId());
    }
}