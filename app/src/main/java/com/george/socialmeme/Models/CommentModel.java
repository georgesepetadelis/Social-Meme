package com.george.socialmeme.Models;

public class CommentModel {
    String author, postID, authorProfilePictureURL, commentText, commentID, authorUsername;

    public CommentModel(String author, String postID, String authorProfilePictureURL, String commentText, String commentID, String authorUsername) {
        this.author = author;
        this.postID = postID;
        this.authorProfilePictureURL = authorProfilePictureURL;
        this.commentText = commentText;
        this.commentID = commentID;
        this.authorUsername = authorUsername;
    }

    public CommentModel() {
    }

    public String getCommentID() {
        return commentID;
    }

    public void setCommentID(String commentID) {
        this.commentID = commentID;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getAuthorProfilePictureURL() {
        return authorProfilePictureURL;
    }

    public void setAuthorProfilePictureURL(String authorProfilePictureURL) {
        this.authorProfilePictureURL = authorProfilePictureURL;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }
}
