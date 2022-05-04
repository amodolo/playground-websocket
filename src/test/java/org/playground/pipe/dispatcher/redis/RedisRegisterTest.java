package org.playground.pipe.dispatcher.redis;

import org.jeasy.random.EasyRandom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.playground.models.WindowManager;
import org.playground.services.RedisService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.playground.pipe.dispatcher.redis.RedisConstants.KEY_PREFIX;

@RunWith(MockitoJUnitRunner.class)
public class RedisRegisterTest {

    @Mock
    private Jedis client;
    @Mock
    private Transaction transaction;
    private RedisRegister sut;
    private EasyRandom easyRandom;

    @Before
    public void setUp() {
        easyRandom = new EasyRandom();
        sut = new RedisRegister();
    }

    @Test
    public void touch_TouchIsRetried3Times_LastTimeEndsWithSuccessSoTrueIsReturned() {
        // given
        WindowManager windowManager = easyRandom.nextObject(WindowManager.class);
        when(client.multi()).thenReturn(transaction);
        when(transaction.exec()).thenReturn(null)
                .thenReturn(null)
                .thenReturn(easyRandom.objects(Object.class, 3).collect(Collectors.toList()));
        try (MockedStatic<RedisService> mockedRedisService = mockStatic(RedisService.class)) {
            mockedRedisService.when(RedisService::getClient).thenReturn(client);
            // when
            Boolean actual = sut.touch(windowManager);
            // then
            assertThat(actual).isTrue();
            verify(transaction, times(3)).sadd(KEY_PREFIX + windowManager.getUser().getId(), windowManager.getId());
            verify(transaction, times(3)).expire(KEY_PREFIX + windowManager.getUser().getId(), RedisRegister.EXPIRATION_SECONDS);
            verify(transaction, times(3)).exec();
        }
    }

    @Test
    public void touch_TouchIsRetried3TimesWithoutSuccess_IllegalStateExceptionIsThrown() {
        // given
        WindowManager windowManager = easyRandom.nextObject(WindowManager.class);
        when(client.multi()).thenReturn(transaction);
        when(transaction.exec()).thenReturn(null)
                .thenReturn(null);
        // when
        try (MockedStatic<RedisService> mockedRedisService = mockStatic(RedisService.class)) {
            mockedRedisService.when(RedisService::getClient).thenReturn(client);
            boolean actual = sut.touch(windowManager);
            // then
            assertThat(actual).isFalse();
            verify(transaction, times(3)).sadd(KEY_PREFIX + windowManager.getUser().getId(), windowManager.getId());
            verify(transaction, times(3)).expire(KEY_PREFIX + windowManager.getUser().getId(), RedisRegister.EXPIRATION_SECONDS);
            verify(transaction, times(3)).exec();
        }
    }

    @Test
    public void deTouch_DeTouchIsCalledWithSuccess_SoTrueIsReturned() {
        // given
        WindowManager windowManager = easyRandom.nextObject(WindowManager.class);
        try (MockedStatic<RedisService> mockedRedisService = mockStatic(RedisService.class)) {
            mockedRedisService.when(RedisService::getClient).thenReturn(client);
            // when
            Boolean actual = sut.deTouch(windowManager);
            // then
            assertThat(actual).isTrue();
            verify(client).srem(KEY_PREFIX + windowManager.getUser().getId(), windowManager.getId());
        }
    }
}