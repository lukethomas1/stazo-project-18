package com.stazo.converg;

import android.content.Context;

import com.firebase.client.DataSnapshot;

import java.util.HashMap;

/**
 * Created by isaacwang on 9/8/16.
 */
public class NotificationEventToday extends Notification2 {

    private String eventId;
    private String eventName;
    private String timeString;

    public NotificationEventToday(int type, String timeString,
                                   String eventId,
                                   String eventName,
                                   String pictureId) {
        super(type, pictureId);
        this.timeString = timeString;
        this.eventId = eventId;
        this.eventName = eventName;
    }

    public NotificationEventToday(HashMap<String, Object> notifMap) {
        super(((Long) notifMap.get("type")).intValue(), (String) notifMap.get("notifID"),
                (String) notifMap.get("pictureId"));
        this.timeString = (String) notifMap.get("timeString");
        this.eventId = (String) notifMap.get("eventId");
        this.eventName = (String) notifMap.get("eventName");
        setViewed((Boolean) notifMap.get("viewed"));
    }

    public void onNotificationClicked(Context context) {
        ((MainAct) context).goToEventInfo(eventId, true);
        setViewed(true);
    }

    public String generateMessage() {
        return filterMessageByLength(eventName + " is happening " + timeString);
    }

    public SnapToBase hasConflict(DataSnapshot userNotifs) {
        for (DataSnapshot notif: userNotifs.getChildren()) {
            HashMap<String, Object> notifMap = (HashMap<String, Object>) notif.getValue();
            if (((Long) notifMap.get("type")).intValue() != Notification2.TYPE_EVENT_TODAY) {
                continue;
            }
            NotificationEventToday net = new NotificationEventToday(notifMap);
            if (net.getEventId().equals(eventId)) {
                return new SnapToBase(notif, notif.getRef());
            }
        }
        return null;
    }

    public Notification2 handleConflict(SnapToBase stb) {

        // if the same user joined the event twice, who cares
        return null;
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

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
}
