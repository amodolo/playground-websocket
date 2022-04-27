package org.playground.pipe.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

public class Pipe implements Serializable {
    private long userId;
    private String name;

    @SuppressWarnings("unused")
    public Pipe() {
    }

    @SuppressWarnings("unused")
    @JsonCreator
    public Pipe(String value) {
        String[] values;
        if (value != null && !value.isEmpty()) {
            values = value.split("_", 2);
            this.userId = Long.parseLong(values[0]);
            if (values.length > 1) {
                name = values[1];
            }
        }
    }

    public Pipe(long userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    @JsonValue
    public String getId() {
        String id = Long.toString(userId);
        if (name != null) id += "_" + name;
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pipe pipe = (Pipe) o;

        if (userId != pipe.userId) return false;
        return name.equals(pipe.name);
    }

    @Override
    public int hashCode() {
        int result = (int) (userId ^ (userId >>> 32));
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Pipe{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                '}';
    }
}
