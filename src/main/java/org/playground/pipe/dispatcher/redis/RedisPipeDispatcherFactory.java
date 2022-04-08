package org.playground.pipe.dispatcher.redis;

import org.playground.pipe.dispatcher.PipeDispatcherFactory;
import org.playground.pipe.dispatcher.Publisher;
import org.playground.pipe.dispatcher.Subscriber;

public class RedisPipeDispatcherFactory implements PipeDispatcherFactory {
    @Override
    public Publisher createPublisher() {
        return new RedisPublisher();
    }

    @Override
    public Subscriber createSubscriber() {
        return new RedisSubscriber();
    }
}
