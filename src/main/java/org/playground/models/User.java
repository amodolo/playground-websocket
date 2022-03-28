package org.playground.models;

import java.io.Serializable;

public class User implements Serializable {
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
}
