package org.playground.pipe.dispatcher;

import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.playground.models.WindowManager;
import org.playground.pipe.model.DispatchError;
import org.playground.pipe.model.Message;
import org.playground.pipe.model.TextMessage;
import org.playground.pipe.utils.Pipe;

import javax.websocket.Session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PipeDispatcherTest {

    private PipeDispatcher sut;
    @Mock
    private PipeDispatcherFactory pipeDispatcherFactory;
    @Mock
    private Publisher publisher;
    @Mock
    private Subscriber subscriber;
    @Mock
    private Register register;
    private Pipe pipe;
    private WindowManager windowManager;
    @Mock
    private Session session;
    private EasyRandom easyRandom;

    @Before
    public void setUp() {
        easyRandom = new EasyRandom();
        pipe = easyRandom.nextObject(Pipe.class);
        windowManager = easyRandom.nextObject(WindowManager.class);
        when(pipeDispatcherFactory.createPublisher()).thenReturn(publisher);
        when(pipeDispatcherFactory.createSubscriber()).thenReturn(subscriber);
        when(pipeDispatcherFactory.createRegister()).thenReturn(register);
        sut = new PipeDispatcher(pipeDispatcherFactory);
    }

    @Test
    public void send_PublisherCalled_MessageCorrectlySent() {
        // given
        Message<String> message = easyRandom.nextObject(TextMessage.class);
        when(publisher.send(message)).thenReturn(null);
        // when
        DispatchError actual = sut.send(message);
        // then
        verify(publisher).send(message);
        assertThat(actual).isNull();
    }

    @Test
    public void send_PublisherCalled_MessageNotCorrectlySent() {
        // given
        Message<String> message = easyRandom.nextObject(TextMessage.class);
        DispatchError dispatchError = easyRandom.nextObject(DispatchError.class);
        when(publisher.send(message)).thenReturn(dispatchError);
        // when
        DispatchError actual = sut.send(message);
        // then
        assertThat(actual).isNotNull().isEqualTo(dispatchError);
    }

    @Test
    public void subscribe_SubscriberCalled_CorrectlySubscribed() {
        // given
        when(subscriber.subscribe(pipe, session)).thenReturn(true);
        // when
        boolean actual = sut.subscribe(pipe, session);
        // then
        verify(subscriber).subscribe(pipe, session);
        assertThat(actual).isTrue();
    }

    @Test
    public void subscribe_SubscriberCalled_NotCorrectlySubscribed() {
        // given
        when(subscriber.subscribe(pipe, session)).thenReturn(false);
        // when
        boolean actual = sut.subscribe(pipe, session);
        // then
        assertThat(actual).isFalse();
    }

    @Test
    public void unsubscribe_SubscriberCalled_CorrectlyUnsubscribed() {
        // given
        when(subscriber.unsubscribe(pipe, session)).thenReturn(true);
        // when
        boolean actual = sut.unsubscribe(pipe, session);
        // then
        verify(subscriber).unsubscribe(pipe, session);
        assertThat(actual).isTrue();
    }

    @Test
    public void unsubscribe_SubscriberCalled_NotCorrectlyUnsubscribed() {
        // given
        when(subscriber.unsubscribe(pipe, session)).thenReturn(false);
        // when
        boolean actual = sut.unsubscribe(pipe, session);
        // then
        assertThat(actual).isFalse();
    }

    @Test
    public void touch_RegisterCalled_CorrectlyTouched() {
        // given
        when(register.touch(windowManager)).thenReturn(true);
        // when
        boolean actual = sut.touch(windowManager);
        // then
        verify(register).touch(windowManager);
        assertThat(actual).isTrue();
    }

    @Test
    public void touch_RegisterCalled_NotCorrectlyTouched() {
        // given
        when(register.touch(windowManager)).thenReturn(false);
        // when
        boolean actual = sut.touch(windowManager);
        // then
        assertThat(actual).isFalse();
    }

    @Test
    public void deTouch_RegisterCalled_CorrectlyDeTouched() {
        // given
        when(register.deTouch(windowManager)).thenReturn(true);
        // when
        boolean actual = sut.deTouch(windowManager);
        // then
        verify(register).deTouch(windowManager);
        assertThat(actual).isTrue();
    }

    @Test
    public void deTouch_RegisterCalled_NotCorrectlyDeTouched() {
        // given
        when(register.deTouch(windowManager)).thenReturn(false);
        // when
        boolean actual = sut.deTouch(windowManager);
        // then
        assertThat(actual).isFalse();
    }
}