package com.george.socialmeme.Models;

public class UploadPostModel {
    private String imgUrl;

    public UploadPostModel() {
    }

    public UploadPostModel(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}