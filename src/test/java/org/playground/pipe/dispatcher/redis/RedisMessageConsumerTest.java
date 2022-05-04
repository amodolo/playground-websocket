package org.playground.pipe.dispatcher.redis;

import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.playground.services.RedisService;
import redis.clients.jedis.Jedis;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.playground.pipe.dispatcher.redis.RedisConstants.KEY_PREFIX;

@RunWith(MockitoJUnitRunner.class)
public class RedisMessageConsumerTest {

    private RedisMessageConsumer sut;
    @Mock
    private Session session;
    @Mock
    private Jedis jedis;
    @Mock
    private RemoteEndpoint.Async async;
    @Captor
    private ArgumentCaptor<String> messageCaptor;
    private EasyRandom easyRandom;

    @Before
    public void setUp() {
        easyRandom = new EasyRandom();
        sut = new RedisMessageConsumer();
        when(session.getAsyncRemote()).thenReturn(async);
    }

    @Test
    public void readAll_RecipientSessionIsNull_FalseIsReturned() {
        // when
        Boolean actual = sut.readAll(easyRandom.nextObject(String.class), null);
        // then
        assertThat(actual).isNotNull().isFalse();
    }

    @Test
    public void readAll_RecipientSessionIsNotOpen_FalseIsReturned() {
        // given
        when(session.isOpen()).thenReturn(false);
        // when
        Boolean actual = sut.readAll(easyRandom.nextObject(String.class), session);
        // then
        assertThat(actual).isNotNull().isFalse();
    }

    @Test
    public void readAll_RecipientSessionIsOpen_RedisServiceIsCalledAndTrueIsReturned() {
        // given
        String recipientKey = easyRandom.nextObject(String.class);
        when(session.isOpen()).thenReturn(true);
        String expected1 = easyRandom.nextObject(String.class);
        String expected2 = easyRandom.nextObject(String.class);
        when(jedis.lpop(KEY_PREFIX + recipientKey)).thenReturn(expected1, expected2, null);
        try (MockedStatic<RedisService> mockedRedisService = mockStatic(RedisService.class)) {
            mockedRedisService.when(() -> RedisService.execute(ArgumentMatchers.<Function<Jedis, Boolean>>any())).thenAnswer(invocation -> {
                Function<Jedis, Boolean> function = invocation.getArgument(0);
                return function.apply(jedis);
            });
            // when
            Boolean actual = sut.readAll(recipientKey, session);
            // then
            assertThat(actual).isNotNull().isTrue();
            mockedRedisService.verify(() -> RedisService.execute(ArgumentMatchers.<Function<Jedis, Boolean>>any()));
            verify(jedis, times(3)).lpop(KEY_PREFIX + recipientKey);
            verify(async, times(2)).sendText(messageCaptor.capture());
            assertThat(messageCaptor.getAllValues()).isNotNull().hasSize(2).containsExactly(expected1, expected2);
        }
    }
}