package org.playground.services;

import org.playground.models.User;

import java.util.HashMap;
import java.util.Map;

public class UserService {
    private static final Map<Long, User> registry = new HashMap<>();

    static {
        registry.put(1L, new User(1L, "user1", "user1", "User", "1"));
        registry.put(2L, new User(2L, "user2", "user2", "User", "2"));
        registry.put(3L, new User(3L, "user3", "user3", "User", "3"));
        registry.put(4L, new User(4L, "user4", "user4", "User", "4"));
        registry.put(5L, new User(5L, "user5", "user5", "User", "5"));
        registry.put(6L, new User(6L, "user6", "user6", "User", "6"));
    }

    public static boolean validate(String username, String password) {
        return registry.values().stream().anyMatch(user -> user.getUsername().equals(username) && user.getPassword().equals(password));
    }

    public static User get(String username) {
        return registry.values().stream()
                .filter(user -> user.getUsername().equals(username) )
                .findFirst()
                .orElseThrow();
    }
}
