package org.playground.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playground.endpoint.PipeDispatcher;
import org.playground.models.WindowManagers;

import java.util.Timer;
import java.util.TimerTask;

public class MonitorService {
    private static final Logger LOG = LogManager.getLogger();
    private static Timer timer;
    private static final TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
        WindowManagers.getAll().forEach(PipeDispatcher::touch);
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
