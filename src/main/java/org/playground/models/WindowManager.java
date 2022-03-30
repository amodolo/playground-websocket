package org.playground.models;

import java.util.concurrent.atomic.AtomicInteger;

public class WindowManager {
    private static AtomicInteger generator = new AtomicInteger(0);
    private final String id;
    private final User user;

    public WindowManager(User user) {
        this.id = "wm_"+generator.incrementAndGet();
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public User getUser() {
        return user;
    }
}
