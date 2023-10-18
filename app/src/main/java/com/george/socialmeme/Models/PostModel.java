package com.george.socialmeme.Models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class PostModel implements Parcelable {
    private String name, imgUrl, likes, id, authorProfilePictureURL, AuthorID, postType, commentsCount, audioName, joke_title, joke_content;
    private HashMap<String, CommentModel> comments;

    public PostModel(String name, String imgUrl, String postType, String commentsCount,
                     String audioName, String joke_title, String joke_content, HashMap<String, CommentModel> comments) {
        this.name = name;
        this.imgUrl = imgUrl;
        this.postType = postType;
        this.commentsCount = commentsCount;
        this.audioName = audioName;
        this.joke_title = joke_title;
        this.joke_content = joke_content;
        this.comments = comments;
    }

    public PostModel() {

    }

    protected PostModel(Parcel in) {
        name = in.readString();
        imgUrl = in.readString();
        likes = in.readString();
        id = in.readString();
        authorProfilePictureURL = in.readString();
        AuthorID = in.readString();
        postType = in.readString();
        commentsCount = in.readString();
        audioName = in.readString();
        joke_title = in.readString();
        joke_content = in.readString();
    }


    public HashMap<String, CommentModel> getComments() {
        return comments;
    }

    public void setComments(HashMap<String, CommentModel> comments) {
        this.comments = comments;
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

    public String getJoke_title() {
        return joke_title;
    }

    public void setJoke_title(String joke_title) {
        this.joke_title = joke_title;
    }

    public String getJoke_content() {
        return joke_content;
    }

    public void setJoke_content(String joke_content) {
        this.joke_content = joke_content;
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
        return AuthorID;
    }

    public void setAuthorID(String authorID) {
        this.AuthorID = authorID;
    }

    public String getAuthorProfilePictureURL() {
        return authorProfilePictureURL;
    }

    public void setAuthorProfilePictureURL(String authorProfilePictureURL) {
        this.authorProfilePictureURL = authorProfilePictureURL;
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
        dest.writeString(authorProfilePictureURL);
        dest.writeString(AuthorID);
        dest.writeString(postType);
        dest.writeString(commentsCount);
        dest.writeString(audioName);
        dest.writeString(joke_title);
        dest.writeString(joke_content);
        //dest.writeList(comments);
    }
}