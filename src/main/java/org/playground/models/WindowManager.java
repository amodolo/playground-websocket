package org.playground.models;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class WindowManager {
    private static AtomicInteger generator = new AtomicInteger(0);
    private final String id;
    private final User user;

    public WindowManager(User user) {
        this.id = "wm" + generator.incrementAndGet();
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WindowManager that = (WindowManager) o;
        return getId().equals(that.getId()) && getUser().equals(that.getUser());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUser());
    }

    @Override
    public String toString() {
        return "WindowManager{" +
                "id='" + id + '\'' +
                ", user=" + user +
                '}';
    }
}
