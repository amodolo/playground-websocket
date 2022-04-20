package org.playground.pipe.dispatcher.redis;

/**
 * Interface about operations needed to communicate to a remote message broker.
 */
public interface RemoteMessageBroker {

    /**
     * Method useful to start the communication with a remote message broker.
     */
    void start();

    /**
     * Method useful to stop the communication with a remote message broker.
     */
    void stop();
}
