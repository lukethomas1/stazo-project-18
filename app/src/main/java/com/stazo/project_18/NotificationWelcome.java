package com.stazo.project_18;

import android.content.Context;
import android.util.Log;

import com.firebase.client.DataSnapshot;

import java.util.HashMap;

/**
 * Created by isaacwang on 7/7/16.
 */
public class NotificationWelcome extends Notification2{

    private String name;

    public NotificationWelcome(int type, String name) {
        super(type, "0");
        this.name = name;
    }

    public NotificationWelcome(HashMap<String, Object> notifMap) {
        super(((Long) notifMap.get("type")).intValue(), (String) notifMap.get("notifID"),
                (String) notifMap.get("pictureId"));
        this.name = (String) notifMap.get("name");
        setViewed((Boolean) notifMap.get("viewed"));
    }

    public void onNotificationClicked(Context context) {

    }

    public String generateMessage() {
        return filterMessageByLength("Welcome to Campus, " + name.split(" ")[0] +"!");
    }
    public SnapToBase hasConflict(DataSnapshot userNotifs) {
        // no conflicts for this app
        return null;
    }

    public Notification2 handleConflict(SnapToBase stb) {
        // if there was a prev follow from this user, just do nothing
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
