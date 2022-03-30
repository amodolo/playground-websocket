package org.playground.models;

import java.util.*;

public class WindowManagers {
    private static Map<String, WindowManager> wms = new HashMap<>();

    public static void register(WindowManager wm) {
        wms.put(wm.getId(), wm);
    }

    public static void unregister(WindowManager wm) {
        wms.remove(wm.getId());
    }

    public static Set<WindowManager> getAll() {
        return new HashSet<>(wms.values());
    }
}
