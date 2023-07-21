package com.george.socialmeme.Models;

public class NotificationModel {

    String id, type, date, title, message;

    public NotificationModel(String id, String type, String date, String message) {
        this.id = id;
        this.type = type;
        this.date = date;
        this.message = message;
    }

    public NotificationModel() {
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
