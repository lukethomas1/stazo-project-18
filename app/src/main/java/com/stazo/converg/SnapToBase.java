package com.stazo.converg;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;

/**
 * Created by isaacwang on 7/2/16.
 */
public class SnapToBase {

    private DataSnapshot snap;
    private Firebase base;

    public SnapToBase(DataSnapshot s, Firebase b) {
        snap = s;
        base = b;
    }

    public DataSnapshot getSnap() {
        return snap;
    }

    public Firebase getBase() {
        return base;
    }
}
