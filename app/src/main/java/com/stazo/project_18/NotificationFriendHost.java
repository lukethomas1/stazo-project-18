package com.stazo.project_18;

import android.content.Context;
import android.content.Intent;

import com.firebase.client.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by isaacwang on 6/30/16.
 */


public class NotificationFriendHost extends Notification2 {

    private String hostName;
    private String eventId;
    private String eventName;
    private String timeString;

    public NotificationFriendHost(int type, String hostName,
                                    String eventId,
                                    String eventName,
                                  String timeString,
                                  String pictureId) {
        super(type, pictureId);
        this.hostName = hostName;
        this.eventId = eventId;
        this.eventName = eventName;
        this.timeString = timeString;
    }

    public NotificationFriendHost(HashMap<String, Object> notifMap) {
        super(((Long) notifMap.get("type")).intValue(), (String) notifMap.get("notifID"),
                (String) notifMap.get("pictureId"));
        this.hostName = (String) notifMap.get("hostName");
        this.eventId = (String) notifMap.get("eventId");
        this.eventName = (String) notifMap.get("eventName");
        this.timeString = (String) notifMap.get("timeString");
        setViewed((Boolean) notifMap.get("viewed"));
    }

    public void onNotificationClicked(Context context) {
        ((MainAct) context).goToEventInfo(eventId, true);
        setViewed(true);
    }

    public String generateMessage(){
        return filterMessageByLength(hostName + " is hosting " + eventName + " " + timeString + ".");
    }

    public SnapToBase hasConflict(DataSnapshot userNotifs) {
        // no conflict is possible
        return null;
    }

    public Notification2 handleConflict(SnapToBase stb) {
        return null;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getTimeString() {
        return timeString;
    }

    public void setTimeString(String timeString) {
        this.timeString = timeString;
    }

    public String getEventId() {

        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
