package org.playground.pipe.dispatcher.redis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RedisSubscriberServiceManager {

    private static final Logger LOG = LogManager.getLogger();
    private static volatile RedisSubscriberServiceManager INSTANCE;
    private Thread thread;
    private final RedisSubscriberService service;

    private RedisSubscriberServiceManager(RedisSubscriberService service) {
        //TODO: CDI will inject this dependency
        this.service = service;
    }

    public static RedisSubscriberServiceManager getInstance() {
        if (INSTANCE == null) {
            synchronized (RedisSubscriberServiceManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RedisSubscriberServiceManager(new RedisSubscriberService());
                }
            }
        }
        return INSTANCE;
    }

    public synchronized void start() {
        LOG.trace("Starting the thread used to communicate to Redis");
        if (this.thread == null || !this.thread.isAlive()) {
            this.thread = new Thread(service);
            this.thread.start();
        } else
            LOG.warn("The thread used to communicate to Redis is already started, it's not required to start it another time");
    }

    public synchronized void stop() {
        LOG.trace("Stopping the thread used to communicate to Redis");
        if (this.thread.isAlive()) {
            this.service.unsubscribe();
            this.thread.interrupt();
        } else
            LOG.warn("The thread used to communicate to Redis is not running, it cannot be stopped");
    }

    public RedisSubscriberService getService() {
        return this.service;
    }
}
