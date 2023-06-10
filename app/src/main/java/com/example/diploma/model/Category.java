package com.example.diploma.model;

public class Category {
    private long id;
    private User userId;
    private String name;
    private String desc;
    private String colour;

    public Category() {
    }

    public Category(long id, User userId, String name, String desc, String colour) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.desc = desc;
        this.colour = colour;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }
}
