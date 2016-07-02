package com.stazo.project_18;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.firebase.client.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by isaacwang on 6/30/16.
 */


public class NotificationNewFollow extends Notification2 {

    private String followerName;
    private String followerId;

    public NotificationNewFollow(int type, String followerName,
                                  String followerId) {
        super(type);
        this.followerName = followerName;
        this.followerId = followerId;
    }

    public NotificationNewFollow(HashMap<String, Object> notifMap) {
        super(((Long) notifMap.get("type")).intValue());
        this.followerName = (String) notifMap.get("followerName");
        this.followerId = (String) notifMap.get("followerId");
    }

    public void onNotificationClicked(Context context) {
        ((MainAct) context).goToOtherProfile(followerId);
    }

    public String generateMessage() {
        return followerName + " is now following you.";
    }

    public SnapToBase hasConflict(DataSnapshot userNotifs) {
        for (DataSnapshot notif: userNotifs.getChildren()) {
            HashMap<String, Object> notifMap = (HashMap<String, Object>) notif.getValue();
            if (((Long) notifMap.get("type")).intValue() != Notification2.TYPE_NEW_FOLLOW) {
                continue;
            }
            NotificationNewFollow nnf = new NotificationNewFollow(notifMap);
            Log.d("wtf", "nnf id is " + nnf.getFollowerId() + ", my id is " + followerId);
            if (nnf.getFollowerId().equals(followerId) ) {
                Log.d("wtf", "CONFLICT");
                return new SnapToBase(notif, notif.getRef());
            }
        }
        Log.d("wtf", "NO CONFLICT");
        return null;
    }

    public Notification2 handleConflict(SnapToBase stb) {

        // if there was a prev follow from this user, just do nothing
        return null;
    }


    public String getFollowerName() {
        return followerName;
    }

    public void setFollowerName(String followerName) {
        this.followerName = followerName;
    }

    public String getFollowerId() {
        return followerId;
    }

    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }
}
