package com.stazo.project_18;

import android.content.Context;
import android.content.Intent;

import com.firebase.client.DataSnapshot;

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
                                   String eventName,
                                   String pictureId) {
        super(type, pictureId);
        this.joinedUserName = joinedUserName;
        this.eventId = eventId;
        this.eventName = eventName;
    }

    public NotificationJoinedEvent(HashMap<String, Object> notifMap) {
        super(((Long) notifMap.get("type")).intValue(), (String) notifMap.get("notifID"),
                (String) notifMap.get("pictureId"));
        this.joinedUserName = (String) notifMap.get("joinedUserName");
        this.eventId = (String) notifMap.get("eventId");
        this.eventName = (String) notifMap.get("eventName");
        setViewed((Boolean) notifMap.get("viewed"));
    }

    public void onNotificationClicked(Context context) {
        ((MainAct) context).goToEventInfo(eventId, true);
        setViewed(true);
    }

    public String generateMessage() {
        return filterMessageByLength(joinedUserName + " joined your event: \"" + eventName + "\".");
    }

    public SnapToBase hasConflict(DataSnapshot userNotifs) {
        for (DataSnapshot notif: userNotifs.getChildren()) {
            HashMap<String, Object> notifMap = (HashMap<String, Object>) notif.getValue();
            if (((Long) notifMap.get("type")).intValue() != Notification2.TYPE_JOINED_EVENT) {
                continue;
            }
            NotificationJoinedEvent nje = new NotificationJoinedEvent(notifMap);
            if (nje.getEventId().equals(eventId) &&
                    nje.getJoinedUserName().equals(joinedUserName)) {
                return new SnapToBase(notif, notif.getRef());
            }
        }
        return null;
    }

    public Notification2 handleConflict(SnapToBase stb) {

        // if the same user joined the event twice, who cares
        return null;
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
