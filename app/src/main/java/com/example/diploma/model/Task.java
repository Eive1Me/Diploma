package com.example.diploma.model;

import androidx.annotation.Nullable;

import java.util.Date;

public class Task {
    private long id;
    private User userId;
    private String name;
    private Category categoryId;
    private Priority priorityId;
    private Date plannedTime;
    private Date deadlineTime;
    private String desc;
    private Group groupId;
    private Status statusId;
    private Date completeTime;

    @Override
    public boolean equals(@Nullable Object obj) {
        Task task = (Task) obj;
        if (obj == null) {
            return false;
        } else {
            return this.id == task.getId();
        }
    }

    public Task() {
    }

    public Task(long id, User userId, String name, Category categoryId, Priority priorityId, Date plannedTime, Date deadlineTime, String desc, Group groupId, Status statusId, Date completeTime) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.categoryId = categoryId;
        this.priorityId = priorityId;
        this.plannedTime = plannedTime;
        this.deadlineTime = deadlineTime;
        this.desc = desc;
        this.groupId = groupId;
        this.statusId = statusId;
        this.completeTime = completeTime;
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

    public Category getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Category categoryId) {
        this.categoryId = categoryId;
    }

    public Priority getPriorityId() {
        return priorityId;
    }

    public void setPriorityId(Priority priorityId) {
        this.priorityId = priorityId;
    }

    public Date getPlannedTime() {
        return plannedTime;
    }

    public void setPlannedTime(Date plannedTime) {
        this.plannedTime = plannedTime;
    }

    public Date getDeadlineTime() {
        return deadlineTime;
    }

    public void setDeadlineTime(Date deadlineTime) {
        this.deadlineTime = deadlineTime;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Group getGroupId() {
        return groupId;
    }

    public void setGroupId(Group groupId) {
        this.groupId = groupId;
    }

    public Status getStatusId() {
        return statusId;
    }

    public void setStatusId(Status statusId) {
        this.statusId = statusId;
    }

    public Date getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }
}
