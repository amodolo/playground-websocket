package org.playground.models;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.playground.pipe.dispatcher.Register;
import org.playground.pipe.dispatcher.redis.RedisRegister;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class WindowManagers implements Register {

    private static final Logger LOG = LogManager.getLogger();
    private static volatile WindowManagers INSTANCE;
    private final Map<String, WindowManager> windowManagers;
    private final Register register;

    private WindowManagers(Register register) {
        //TODO: CDI will inject this dependency
        this.register = register;
        this.windowManagers = new ConcurrentHashMap<>();
    }

    public static WindowManagers getInstance() {
        if (INSTANCE == null) {
            synchronized (WindowManagers.class) {
                if (INSTANCE == null) {
                    INSTANCE = new WindowManagers(new RedisRegister());
                }
            }
        }
        return INSTANCE;
    }

    public boolean register(WindowManager windowManager) {
        LOG.trace("register(windowManager={})", windowManager);
        windowManagers.put(windowManager.getId(), windowManager);
        return register.register(windowManager);
        //TODO: lo gestiremo con gli eventi e relativi BusinessTask quando rientreremo in Geocall!
    }

    public boolean unregister(WindowManager windowManager) {
        LOG.trace("unregister(windowManager={})", windowManager);
        windowManagers.remove(windowManager.getId());
        return register.unregister(windowManager);
    }

    @Override
    public boolean touch(WindowManager windowManager) {
        LOG.trace("touch(windowManager={})", windowManager);
        return register.touch(windowManager);
    }

    @Override
    public boolean detouch(WindowManager windowManager) {
        LOG.trace("detouch(windowManager={})", windowManager);
        return register.detouch(windowManager);
    }

    public boolean touchAll() {
        LOG.trace("touchAll()");
        boolean[] result = new boolean[]{true};
        windowManagers.values().forEach(windowManager -> result[0] &= this.touch(windowManager));
        return result[0];
    }

    public boolean detouchAll() {
        LOG.trace("detouchAll()");
        boolean[] result = new boolean[]{true};
        windowManagers.values().forEach(windowManager -> result[0] &= this.detouch(windowManager));
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
