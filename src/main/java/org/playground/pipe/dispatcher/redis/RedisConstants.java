package org.playground.pipe.dispatcher.redis;

interface RedisConstants {

    String KEY_PREFIX = "geocall:dispatcher:";
    String CHANNEL_PREFIX = KEY_PREFIX + "ch:";
}
