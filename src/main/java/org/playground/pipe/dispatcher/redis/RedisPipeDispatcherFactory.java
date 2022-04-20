package org.playground.pipe.dispatcher.redis;

import org.playground.pipe.dispatcher.PipeDispatcherFactory;
import org.playground.pipe.dispatcher.Publisher;
import org.playground.pipe.dispatcher.Subscriber;
import org.playground.pipe.utils.MessageEncoder;

public class RedisPipeDispatcherFactory implements PipeDispatcherFactory {

    @Override
    public Publisher createPublisher() {
        //TODO: CDI will inject this dependency
        return new RedisPublisher(new MessageEncoder());
    }

    @Override
    public Subscriber createSubscriber() {
        //TODO: CDI will inject this dependency
        return new RedisSubscriber(
                RedisRemoteMessageBroker.getInstance().getRedisPubSubRunnable(),
                new RedisMessageConsumer());
    }
}
