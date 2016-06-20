package com.stazo.project_18;

import com.firebase.client.Firebase;

import java.util.ArrayList;

/**
 * Created by ericzhang on 6/17/16.
 */
public class Comment {

    private String event_ID;
    private ArrayList<String> comments;

    public Comment() {
    }

    public Comment(Firebase fb, String event_ID) {
        this.event_ID = event_ID;

    }

    public Comment(String event_ID) {
        this.event_ID = event_ID;
        this.comments = new ArrayList<String>();
    }

    public Comment(String event_ID, ArrayList<String> comments) {
        this.event_ID = event_ID;
        this.comments = comments;
    }

    public void setEvent_ID(String event_ID) {
        this.event_ID = event_ID;
    }

    public String getEvent_ID() {
        return this.event_ID;
    }

    public void setComments(ArrayList<String> comments) {
        this.comments = comments;
    }

    public ArrayList<String> getComments() {
        return this.comments;
    }

    public void addComment(String comment) {
        this.comments.add(comment);
    }
}