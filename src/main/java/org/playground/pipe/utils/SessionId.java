package org.playground.pipe.utils;

public class SessionId {
    private final long userId;
    private final String appId;

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
}
