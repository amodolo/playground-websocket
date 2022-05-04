package org.playground.pipe.dispatcher.redis;

import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.playground.pipe.dispatcher.MessageConsumer;
import org.playground.pipe.utils.Pipe;

import javax.websocket.Session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.playground.pipe.dispatcher.redis.RedisConstants.CHANNEL_PREFIX;

@RunWith(MockitoJUnitRunner.class)
public class RedisSubscriberTest {

    @InjectMocks
    private RedisSubscriber sut;
    @Mock
    private RedisSubscriberService service;
    @Mock
    private Pipe pipe;
    @Mock
    private MessageConsumer messageConsumer;
    @Mock
    private Session session;
    private String pipeId;

    @Before
    public void setUp() {
        EasyRandom easyRandom = new EasyRandom();
        pipeId = easyRandom.nextObject(String.class);
        when(pipe.getId()).thenReturn(pipeId);
    }

    @Test
    public void subscribe_WhenCalled_AllDelegatedServicesAreCalledAndTheSubscriberIsInsertedIntoTheSubscriberMapAndTrueIsReturned() {
        // given
        when(messageConsumer.readAll(pipeId, session)).thenReturn(true);
        // when
        boolean actual = sut.subscribe(pipe, session);
        // then
        assertThat(actual).isTrue();
        assertThat(RedisSubscriber.containsKey(pipe)).isTrue();
        verify(service).subscribe(sut, CHANNEL_PREFIX + pipeId);
        verify(messageConsumer).readAll(pipeId, session);
    }

    @Test
    public void unsubscribe_WhenCalled_AllDelegatedServicesAreCalledAndTheSubscriberIsRemovedFromTheSubscriberMapAndTrueIsReturned() {
        // when
        boolean actual = sut.unsubscribe(pipe, session);
        // then
        assertThat(actual).isTrue();
        assertThat(RedisSubscriber.containsKey(pipe)).isFalse();
        verify(service).unsubscribe(CHANNEL_PREFIX + pipeId);
    }

    @Test
    public void onMessage_WhenCalled_MessageConsumerReturnsMessages() {
        // given
        when(messageConsumer.readAll(pipeId, session)).thenReturn(true);
        // to register the subscriber into the registry map
        RedisSubscriber.getRegistry().put(pipe, session);
        // when
        sut.onMessage(pipe);
        // then
        verify(messageConsumer).readAll(pipe.getId(), session);
    }
}