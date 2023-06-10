package com.example.diploma.model;

public class Group {
    private long id;
    private String name;
    private User userId;

    public Group() {
    }

    public Group(long id, String name, User userId) {
        this.id = id;
        this.name = name;
        this.userId = userId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }
}
