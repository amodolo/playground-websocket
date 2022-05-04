package org.playground.pipe.dispatcher.redis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RedisSubscriberServiceManagerTest {

    @InjectMocks
    private RedisSubscriberServiceManager sut;
    @Mock
    private RedisSubscriberService service;

    @Test
    public void getInstance_WhenCalled_ReturnsARedisSubscriberServiceManagerWrappingARedisSubscriberService() {
        // when
        RedisSubscriberServiceManager instance = RedisSubscriberServiceManager.getInstance();
        // then
        assertThat(instance).isNotNull()
                .isInstanceOf(RedisSubscriberServiceManager.class)
                .extracting(RedisSubscriberServiceManager::getService)
                .isNotNull()
                .isInstanceOf(RedisSubscriberService.class);
    }

    @Test
    public void getInstance_WhenCalledMoreThanOnce_TheSameInstanceIsReturned() {
        // when
        RedisSubscriberServiceManager instance1 = RedisSubscriberServiceManager.getInstance();
        RedisSubscriberServiceManager instance2 = RedisSubscriberServiceManager.getInstance();
        // then
        assertThat(instance1).isSameAs(instance2);
    }

    @Test
    public void start_WhenCalled_AThreadCallWrappedServiceRun() {
        // when
        sut.start();
        // then
        verify(service, Mockito.timeout(200)).run();
    }

    @Test
    public void stop_WhenCalled_TheDelegatedServiceUnsubscribeAllSubscribersAndStopsHimself() {
        // given
        sut.start(); // need to start the internal thread (integration test in progress... :-()
        // when
        sut.stop();
        // then
        verify(service, Mockito.timeout(200)).unsubscribe();
    }
}
