package org.playground.pipe.dispatcher.redis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RedisRemoteMessageBroker implements RemoteMessageBroker {

    private static final Logger LOG = LogManager.getLogger();
    private static volatile RedisRemoteMessageBroker INSTANCE;
    private final Thread redisThread;
    private final RedisPubSubRunnable redisPubSubRunnable;

    private RedisRemoteMessageBroker(RedisPubSubRunnable redisPubSubRunnable) {
        //TODO: CDI will inject this dependency
        this.redisPubSubRunnable = redisPubSubRunnable;
        this.redisThread = new Thread(redisPubSubRunnable);
    }

    public static RedisRemoteMessageBroker getInstance() {
        if (INSTANCE == null) {
            synchronized (RedisRemoteMessageBroker.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RedisRemoteMessageBroker(new RedisPubSubRunnable());
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void start() {
        LOG.trace("Starting the thread used to communicate to Redis");
        if (!this.redisThread.isAlive())
            this.redisThread.start();
        else
            LOG.warn("The thread used to communicate to Redis is already started, it's not required to start it another time");
    }

    @Override
    public void stop() {
        LOG.trace("Stopping the thread used to communicate to Redis");
        if (this.redisThread.isAlive()) {
            this.redisPubSubRunnable.unsubscribe();
            this.redisThread.interrupt();
        } else
            LOG.warn("The thread used to communicate to Redis is not running, it cannot be stopped");
    }

    public RedisPubSubRunnable getRedisPubSubRunnable() {
        return this.redisPubSubRunnable;
    }
}
