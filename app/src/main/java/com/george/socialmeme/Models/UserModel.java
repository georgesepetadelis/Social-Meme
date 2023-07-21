package com.george.socialmeme.Models;

public class UserModel {

    String userID, username, profilePictureURL, followers, following,
            totalLikes, goldTrophiesCounter, silverTrophiesCounter, bronzeTrophiesCounter;

    boolean userFollowingLoggedInUser, followingCurrentUser;

    public UserModel(String userID, String username, String profilePictureURL, String followers) {
        this.userID = userID;
        this.username = username;
        this.profilePictureURL = profilePictureURL;
        this.followers = followers;
    }

    public UserModel() {}

    public boolean isFollowingCurrentUser() {
        return followingCurrentUser;
    }

    public void setFollowingCurrentUser(boolean followingCurrentUser) {
        this.followingCurrentUser = followingCurrentUser;
    }

    public boolean currentUserFollowLoggedInUser() {
        return userFollowingLoggedInUser;
    }

    public void setUserFollowingLoggedInUser(boolean userFollowingLoggedInUser) {
        this.userFollowingLoggedInUser = userFollowingLoggedInUser;
    }

    public String getGoldTrophiesCounter() {
        return goldTrophiesCounter;
    }

    public void setGoldTrophiesCounter(String goldTrophiesCounter) {
        this.goldTrophiesCounter = goldTrophiesCounter;
    }

    public String getSilverTrophiesCounter() {
        return silverTrophiesCounter;
    }

    public void setSilverTrophiesCounter(String silverTrophiesCounter) {
        this.silverTrophiesCounter = silverTrophiesCounter;
    }

    public String getBronzeTrophiesCounter() {
        return bronzeTrophiesCounter;
    }

    public void setBronzeTrophiesCounter(String bronzeTrophiesCounter) {
        this.bronzeTrophiesCounter = bronzeTrophiesCounter;
    }

    public String getFollowing() {
        return following;
    }

    public void setFollowing(String following) {
        this.following = following;
    }

    public String getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(String totalLikes) {
        this.totalLikes = totalLikes;
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
