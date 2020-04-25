package com.fzu.recommend.entity;

import java.util.Date;

public class ActionLog {

    private int id;

    private int userId;

    private int newsId;

    private int actionType;

    private Date actionTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getNewsId() {
        return newsId;
    }

    public void setNewsId(int newsId) {
        this.newsId = newsId;
    }

    public int getActionType() {
        return actionType;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }

    public Date getActionTime() {
        return actionTime;
    }

    public void setActionTime(Date actionTime) {
        this.actionTime = actionTime;
    }

    @Override
    public String toString() {
        return "Action{" +
                "id=" + id +
                ", userId=" + userId +
                ", newsId=" + newsId +
                ", actionType=" + actionType +
                ", actionTime=" + actionTime +
                '}';
    }
}
