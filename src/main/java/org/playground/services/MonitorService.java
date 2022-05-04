package org.playground.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playground.models.WindowManagers;

import java.util.Timer;
import java.util.TimerTask;

public class MonitorService {
    private static final Logger LOG = LogManager.getLogger();
    private static Timer timer;
    private static final TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            WindowManagers.getInstance().touchAll();
        }
    };

    private MonitorService() {
        // this is a utility class containing only static methods
    }

    public static void start() {
        LOG.trace("Starting monitor service");
        timer = new Timer("monitor");
        timer.scheduleAtFixedRate(timerTask, 0, 5000);
    }

    public static void stop() {
        LOG.trace("Stopping monitor service");
        WindowManagers.getInstance().unregisterAll();
        timer.cancel();
    }

}
