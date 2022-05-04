package org.playground.pipe.dispatcher.redis;

import org.junit.Before;
import org.junit.Test;
import org.playground.pipe.dispatcher.Publisher;
import org.playground.pipe.dispatcher.Register;
import org.playground.pipe.dispatcher.Subscriber;

import static org.assertj.core.api.Assertions.assertThat;

public class RedisPipeDispatcherFactoryTest {

    private RedisPipeDispatcherFactory sut;

    @Before
    public void setUp() {
        sut = new RedisPipeDispatcherFactory();
    }

    @Test
    public void createPublisher_WhenCalled_ReturnsRedisPublisher() {
        // when
        Publisher actual = sut.createPublisher();
        // then
        assertThat(actual).isNotNull().isInstanceOf(RedisPublisher.class);
    }

    @Test
    public void createSubscriber_WhenCalled_ReturnsRedisSubscriber() {
        // when
        Subscriber actual = sut.createSubscriber();
        // then
        assertThat(actual).isNotNull().isInstanceOf(RedisSubscriber.class);
    }

    @Test
    public void createSubscriber_WhenCalled_ReturnsRedisRegister() {
        // when
        Register actual = sut.createRegister();
        // then
        assertThat(actual).isNotNull().isInstanceOf(RedisRegister.class);
    }
}