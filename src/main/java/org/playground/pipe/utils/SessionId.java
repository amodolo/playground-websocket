package org.playground.pipe.utils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class SessionId {
    private long userId;
    private String appId;

    @SuppressWarnings("unused")
    public SessionId() {
    }

    @SuppressWarnings("unused")
    @JsonCreator
    public SessionId(String value) {
        String[] values;
        if (value != null && !value.isEmpty()) {
            values = value.split("_", 2);// 1_2_3
            this.userId = Long.parseLong(values[0]);
            if (values.length > 1) {
                appId = values[1];
            }
        }
    }

    public SessionId(long userId, String appId) {
        this.userId = userId;
        this.appId = appId;
    }

    public long getUserId() {
        return userId;
    }

    public String getAppId() {
        return appId;
    }

    @JsonValue
    public String getId() {
        String id = Long.toString(userId);
        if (appId != null) id += "_" + appId;
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SessionId sessionId = (SessionId) o;

        if (userId != sessionId.userId) return false;
        return appId.equals(sessionId.appId);
    }

    @Override
    public int hashCode() {
        int result = (int) (userId ^ (userId >>> 32));
        result = 31 * result + appId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SessionId{" +
                "userId=" + userId +
                ", appId='" + appId + '\'' +
                '}';
    }
}
