package org.playground.services;

import org.playground.models.User;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSessions {
    private static final Map<HttpSession, User> sessions = new ConcurrentHashMap<>();

    public static void register(HttpSession session, User user) {
        sessions.put(session, user);
    }

    public static void unregister(HttpSession session) {
        sessions.remove(session);
    }

    public static Map<HttpSession, User> getSessions() {
        return sessions;
    }
}
