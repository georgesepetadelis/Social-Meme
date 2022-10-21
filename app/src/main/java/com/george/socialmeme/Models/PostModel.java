package com.george.socialmeme.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class PostModel implements Parcelable {
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

    protected PostModel(Parcel in) {
        name = in.readString();
        imgUrl = in.readString();
        likes = in.readString();
        id = in.readString();
        profileImgUrl = in.readString();
        authorID = in.readString();
        postType = in.readString();
        commentsCount = in.readString();
        audioName = in.readString();
        postTitle = in.readString();
        postContentText = in.readString();
    }

    public static final Creator<PostModel> CREATOR = new Creator<PostModel>() {
        @Override
        public PostModel createFromParcel(Parcel in) {
            return new PostModel(in);
        }

        @Override
        public PostModel[] newArray(int size) {
            return new PostModel[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(imgUrl);
        dest.writeString(likes);
        dest.writeString(id);
        dest.writeString(profileImgUrl);
        dest.writeString(authorID);
        dest.writeString(postType);
        dest.writeString(commentsCount);
        dest.writeString(audioName);
        dest.writeString(postTitle);
        dest.writeString(postContentText);
    }
}