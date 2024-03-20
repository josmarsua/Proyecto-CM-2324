package com.example.myapplication;

public class Comment {

    private int userID;
    private int eventID;
    private int commentID;
    private String username;
    private String date;
    private int rating;
    private String body;

    public Comment(int userID, int eventID, int commentID, String username, String date, int rating, String body) {
        this.userID = userID;
        this.eventID = eventID;
        this.commentID = commentID;
        this.username = username;
        this.date = date;
        this.rating = rating;
        this.body = body;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public int getCommentID() {
        return commentID;
    }

    public void setCommentID(int commentID) {
        this.commentID = commentID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
