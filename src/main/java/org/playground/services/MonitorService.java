package org.playground.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playground.models.WindowManagers;
import redis.clients.jedis.Transaction;

import java.util.*;

public class MonitorService {
    private static final Logger LOG = LogManager.getLogger();
    private static Timer timer;
    private static final TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            RedisService.execute(client -> {
                Transaction t = client.multi();
                WindowManagers.getAll().forEach(wm -> {
                    String key = "user:" + wm.getUser().getId();
                    t.zadd(key, System.currentTimeMillis(), wm.getId());
                    t.expire(key, 10);
                });
                t.exec();
                return null;
            });
        }
    };

    public static void start() {
        timer = new Timer("monitor");
        timer.scheduleAtFixedRate(timerTask, 0, 5000);
    }

    public static void stop() {
        timer.cancel();
    }

    public static Map<Long, List<String>> getWMReferences(String userId) {
        Map<Long, List<String>> ret = new HashMap<>();
        try {
            String key = userId == null ? "user:*" : "user:"+userId;
            Set<String> usersKey = RedisService.keys(key, null);
            for (String userKey : usersKey) {
                Long user = Long.parseLong(userKey.substring(5));
                RedisService.execute(client -> {
                    ret.put(user, client.zrange(userKey, 0, -1));
                    return null;
                });
            }
        } catch (Exception e) {
            LOG.error(e);
        }
        return ret;
    }

}
