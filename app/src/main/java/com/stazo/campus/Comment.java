package com.stazo.campus;

/**
 * Created by ericzhang on 6/17/16.
 */
public class Comment {

    private String event_ID;
    private String comment;
    private String user_ID;

    public Comment() {
    }

    public Comment(String event_ID, String comment, String user_ID) {
        this.event_ID = event_ID;
        this.comment = comment;
        this.user_ID = user_ID;
    }

    public void setEvent_ID(String event_ID) {
        this.event_ID = event_ID;
    }

    public String getEvent_ID() {
        return this.event_ID;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return this.comment;
    }

    public void setUser_ID(String user_ID) {
        this.user_ID = user_ID;
    }

    public String getUser_ID() {
        return this.user_ID;
    }
}