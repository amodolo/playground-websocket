package org.playground.models;

import org.playground.services.WebSessions;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.io.Serializable;

public class User implements Serializable, HttpSessionBindingListener {
    private final Long id;
    private final String username;
    private final String password;
    private final String name;
    private final String surname;

    public User(Long id, String username, String password, String name, String surname) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.surname = surname;
    }

    public User(User user) {
        this.id = user.id;
        this.username = user.username;
        this.password = user.password;
        this.name = user.name;
        this.surname = user.surname;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    @Override
    public void valueBound(HttpSessionBindingEvent event) {
        WebSessions.register(event.getSession(), this);
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
        WebSessions.unregister(event.getSession());
    }
}
