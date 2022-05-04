package org.playground.models;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playground.pipe.dispatcher.PipeDispatcher;
import org.playground.pipe.dispatcher.redis.RedisPipeDispatcherFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class WindowManagers {

    private static final Logger LOG = LogManager.getLogger();
    private static WindowManagers INSTANCE;
    private final Map<String, WindowManager> windowManagers;
    private final PipeDispatcher dispatcher;

    private WindowManagers() {
        this(new ConcurrentHashMap<>(), new PipeDispatcher(new RedisPipeDispatcherFactory()));
    }

    /**
     * For test purposes only.
     *
     * @param windowManagers {@link Map}&lt;String, WindowManager&gt; to initialize the window managers map.
     * @param dispatcher {@link PipeDispatcher} to initialize the dispatcher.
     */
    @SuppressWarnings("unused")
    WindowManagers(Map<String, WindowManager> windowManagers, PipeDispatcher dispatcher) {
        this.dispatcher = dispatcher;
        this.windowManagers = windowManagers;
    }

    public static WindowManagers getInstance() {
        if (INSTANCE == null) {
            synchronized (WindowManagers.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WindowManagers();
                }
            }
        }
        return INSTANCE;
    }

    public boolean register(WindowManager windowManager) {
        LOG.trace("register(windowManager={})", windowManager);
        windowManagers.put(windowManager.getId(), windowManager);
        return dispatcher.touch(windowManager);
        //TODO: lo gestiremo con gli eventi e relativi BusinessTask quando rientreremo in Geocall!
    }

    public boolean unregister(WindowManager windowManager) {
        LOG.trace("unregister(windowManager={})", windowManager);
        windowManagers.remove(windowManager.getId());
        return dispatcher.deTouch(windowManager);
    }

    public boolean touchAll() {
        LOG.trace("touchAll()");
        boolean[] result = new boolean[]{true};
        windowManagers.values().forEach(windowManager -> result[0] &= dispatcher.touch(windowManager));
        return result[0];
    }

    public boolean unregisterAll() {
        LOG.trace("unregisterAll()");
        boolean[] result = new boolean[]{true};
        windowManagers.values().forEach(windowManager -> result[0] &= dispatcher.deTouch(windowManager));
        windowManagers.clear();
        return result[0];
    }

    public Set<WindowManager> getUsersWindowManager(long userId) {
        LOG.trace("getUsersWindowManager(userId={})", userId);
        return windowManagers.values()
                .stream()
                .filter(windowManager -> Objects.equals(windowManager.getUser().getId(), userId))
                .collect(Collectors.toSet());
    }
}
