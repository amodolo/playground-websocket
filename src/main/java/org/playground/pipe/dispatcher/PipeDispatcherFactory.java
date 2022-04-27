package org.playground.pipe.dispatcher;

public interface PipeDispatcherFactory {
    Publisher createPublisher();

    Subscriber createSubscriber();

    Register createRegister();
}
