package com.stazo.project_18;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by isaacwang on 6/30/16.
 */


public class NotificationJoinedEvent extends Notification2 {

    private String joinedUserName;
    private String eventId;
    private String eventName;

    public NotificationJoinedEvent(int type, String joinedUserName,
                                   String eventId,
                                   String eventName) {
        super(type);
        this.joinedUserName = joinedUserName;
        this.eventId = eventId;
        this.eventName = eventName;
    }

    public NotificationJoinedEvent(HashMap<String, Object> notifMap) {
        super(((Long) notifMap.get("type")).intValue());
        this.joinedUserName = (String) notifMap.get("joinedUserName");
        this.eventId = (String) notifMap.get("eventId");
        this.eventName = (String) notifMap.get("eventName");
    }

    public void onNotificationClicked(Context context) {
        ((MainAct) context).goToEventInfo(eventId, true);
    }

    public String generateMessage() {
        return joinedUserName + " joined your event: \"" + eventName + "\".";
    }

    public String getJoinedUserName() {
        return joinedUserName;
    }

    public void setJoinedUserName(String joinedUserName) {
        this.joinedUserName = joinedUserName;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
}
