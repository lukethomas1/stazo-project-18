package com.stazo.project_18;

/**
 * Created by luket on 6/20/2016.
 */
public class Notification {
    private String message;
    private String creatorName;
    private String eventID;
    private boolean viewed = false;

    public Notification() {
        message = "";
        creatorName = "";
        eventID = "";
    }

    public Notification(String text, boolean viewd, String ID) {
        message = text;
        viewed = viewd;
        eventID = ID;
    }
}
