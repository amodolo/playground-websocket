package org.playground.pipe.dispatcher;

import org.playground.utils.Lazy;

public class PipeDispatcher {
    private final Lazy<Publisher> publisher;
    private final Lazy<Subscriber> subscriber;

    public PipeDispatcher(PipeDispatcherFactory factory) {
        this.publisher = new Lazy<>(factory::createPublisher);
        this.subscriber = new Lazy<>(factory::createSubscriber);
    }

    public Publisher getPublisher() {
        return publisher.get();
    }

    public Subscriber getSubscriber() {
        return subscriber.get();
    }
}
