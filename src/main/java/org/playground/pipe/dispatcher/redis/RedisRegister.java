package org.playground.pipe.dispatcher.redis;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playground.models.WindowManager;
import org.playground.pipe.dispatcher.Register;
import org.playground.services.RedisService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import static org.playground.pipe.dispatcher.redis.RedisConstants.KEY_PREFIX;

public class RedisRegister implements Register {

    private static final Logger LOG = LogManager.getLogger();
    protected static final String IMPOSSIBLE_TO_RENEW_THE_WINDOW_MANAGER_VALIDITY_IN_REDIS = "Max retry number reached: impossible to renew the windowManager ('{}') validity in Redis for user {}";
    protected static final long EXPIRATION_SECONDS = 60L * 60L; // 1 hour

    @Override
    public boolean touch(WindowManager windowManager) {
        LOG.trace("touch(windowManager={})", windowManager);
        try (Jedis client = RedisService.getClient()) {
            String key = KEY_PREFIX + windowManager.getUser().getId();
            LOG.trace("Renewal of registration {} for another hour", key);
            boolean done;
            int retry = 3;
            do {
                Transaction t = client.multi();
                t.sadd(key, windowManager.getId());
                t.expire(key, EXPIRATION_SECONDS); // 1h SESSION duration
                if (t.exec() != null) done = true;
                else done = --retry <= 0;
            } while (!done);
            if (retry == 0) {
                LOG.error(IMPOSSIBLE_TO_RENEW_THE_WINDOW_MANAGER_VALIDITY_IN_REDIS, windowManager.getId(), key);
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean deTouch(WindowManager windowManager) {
        LOG.trace("deTouch(windowManager={})", windowManager);
        try (Jedis client = RedisService.getClient()) {
            String key = KEY_PREFIX + windowManager.getUser().getId();
            LOG.trace("Removing the registration {}", key);
            client.srem(key, windowManager.getId());
        }
        return true;
    }
}
