package com.george.socialmeme.Models;

public class UserModel {

    String userID, username, profilePictureURL, followers;

    public UserModel(String userID, String username, String profilePictureURL, String followers) {
        this.userID = userID;
        this.username = username;
        this.profilePictureURL = profilePictureURL;
        this.followers = followers;
    }

    public UserModel() {
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePictureURL() {
        return profilePictureURL;
    }

    public void setProfilePictureURL(String profilePictureURL) {
        this.profilePictureURL = profilePictureURL;
    }

    public String getFollowers() {
        return followers;
    }

    public void setFollowers(String followers) {
        this.followers = followers;
    }
}
