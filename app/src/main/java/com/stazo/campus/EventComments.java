package com.stazo.campus;

import java.util.ArrayList;

/**
 * Created by ericzhang on 6/24/16.
 */
public class EventComments {

    private String event_ID;
    private ArrayList<Comment> comments;

    public EventComments() {

    }

    public void setEvent_ID(String event_ID) {
        this.event_ID = event_ID;
    }

    public String getEvent_ID() {
        return this.event_ID;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    public ArrayList<Comment> getComments() {
        return this.comments;
    }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }
}
