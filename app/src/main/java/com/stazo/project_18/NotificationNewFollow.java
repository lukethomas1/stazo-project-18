package com.stazo.project_18;

import android.content.Context;
import android.content.Intent;

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
