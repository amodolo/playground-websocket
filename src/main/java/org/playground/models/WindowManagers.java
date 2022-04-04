package org.playground.models;

import org.playground.endpoint.PipeDispatcher;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WindowManagers {
    private static final Map<String, WindowManager> wms = new HashMap<>();

    public static void register(WindowManager wm) {
        wms.put(wm.getId(), wm);
        PipeDispatcher.touch(wm);
    }

    public static void unregister(WindowManager wm) {
        wms.remove(wm.getId());
        PipeDispatcher.untouch(wm);
    }

    public static Set<WindowManager> getAll() {
        return new HashSet<>(wms.values());
    }

    public static Set<WindowManager> getUsersWm(long userId) {
        return wms.values()
                .stream()
                .filter(wm -> wm.getUser().getId() == userId)
                .collect(Collectors.toSet());
    }
}
