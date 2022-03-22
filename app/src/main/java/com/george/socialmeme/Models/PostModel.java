package com.george.socialmeme.Models;

public class PostModel {
    private String name, imgUrl, likes, id, profileImgUrl, authorID, postType, commentsCount, audioName, postTitle, postContentText;

    public PostModel(String name, String imgUrl, String postType, String commentsCount, String audioName, String postTitle, String postContentText) {
        this.name = name;
        this.imgUrl = imgUrl;
        this.postType = postType;
        this.commentsCount = commentsCount;
        this.audioName = audioName;
        this.postTitle = postTitle;
        this.postContentText = postContentText;
    }

    public PostModel() {

    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getPostContentText() {
        return postContentText;
    }

    public void setPostContentText(String postContentText) {
        this.postContentText = postContentText;
    }

    public String getAudioName() {
        return audioName;
    }

    public void setAudioName(String audioName) {
        this.audioName = audioName;
    }

    public String getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(String commentsCount) {
        this.commentsCount = commentsCount;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }

    public String getAuthorID() {
        return authorID;
    }

    public void setAuthorID(String authorID) {
        this.authorID = authorID;
    }

    public String getProfileImgUrl() {
        return profileImgUrl;
    }

    public void setProfileImgUrl(String profileImgUrl) {
        this.profileImgUrl = profileImgUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}