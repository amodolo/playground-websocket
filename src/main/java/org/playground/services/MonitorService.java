package org.playground.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playground.models.WindowManager;
import org.playground.models.WindowManagers;
import redis.clients.jedis.Transaction;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class MonitorService {
    private static final Logger LOG = LogManager.getLogger();
    private static Timer timer;
    private static final TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            RedisService.execute(client -> {
                Transaction t = client.multi();

                WebSessions.getSessions().forEach((session, user) -> {
                    String key = "session:" + user.getId() + ":" + session.getId();
                    Map<String, Double> values = WindowManagers.getUsersWm(user)
                            .stream()
                            .collect(Collectors.toMap(WindowManager::getId, wm -> (double) System.currentTimeMillis()));

                    t.zadd(key, values);
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

}
