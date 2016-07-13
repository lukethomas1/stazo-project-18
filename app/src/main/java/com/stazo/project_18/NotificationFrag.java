package com.stazo.project_18;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class NotificationFrag extends android.support.v4.app.Fragment {

    private View v;
    private Firebase fb;
    private User currentUser;
    private ArrayList<Notification2> notifs = new ArrayList<>();
    private LinearLayout LL1;
    private InteractiveScrollView notifScrollView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_notification, container, false);

        // Initialize the Firebase reference
        fb = ((Project_18) this.getActivity().getApplication()).getFB();

        currentUser = ((Project_18) this.getActivity().getApplication()).getMe();

        // Set the layout
        LL1 = (LinearLayout)v.findViewById(R.id.LL1);

        // Clear notifs to prevent duplicates
        notifs.clear();

        // Update the notifications and then display them
        pullNotifications(currentUser.getID());

        return v;
    }

    private void displayNotifications() {
        for (final Notification2 not2 : notifs) {
            // Make a final copy of not2 so that it can be used inside the onclick setter
            //final Notification2 not2Copy = not2;

            // Container for button
            LinearLayout container = new LinearLayout(getActivity());

            makeNotificationButton(not2, container);

            LL1.addView(container);
        }
    }

    private void makeNotificationButton(Notification2 notif, final LinearLayout container) {

        // Format container
        container.setMinimumHeight(200);
        container.setOrientation(LinearLayout.HORIZONTAL);
        if (notif.isViewed()) {
            container.setBackground(getResources().getDrawable(R.drawable.border_notif_button_viewed));
        }
        // Not viewed specific
        else {
            container.setBackground(getResources().getDrawable(R.drawable.border_notif_button_unviewed));
        }

        // Create a new button to add to the view
        Button button = new Button(getActivity());

        // Set the text of the button
        button.setText(notif.generateMessage());

        // Format button
        button.setAllCaps(false);
        button.setBackground(null);
        button.setTextColor(getResources().getColor(R.color.colorTextPrimary));
        button.setTypeface(null, Typeface.NORMAL);
        button.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams buttonLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        buttonLP.gravity = Gravity.CENTER_VERTICAL;
        button.setLayoutParams(buttonLP);


        // Format image
        ImageView iv = new ImageView(getActivity());
        (new SetPictureTask(iv, notif.getPictureId())).execute();
        iv.setPadding(20, 20, 20, 20);


        // Add listeners
        NotificationOnTouchListener listener =
                new NotificationOnTouchListener(notif, container);
        button.setOnTouchListener(listener);
        container.setOnTouchListener(listener);
        iv.setOnTouchListener(listener);

        container.addView(iv);
        container.addView(button);
    }

    private class SetPictureTask extends AsyncTask<Void, Void, Void> {

        private ImageView iv;
        private String id;
        private Bitmap bitmap;
        private LinearLayout container;

        public SetPictureTask(ImageView iv, String userId){
            this.iv = iv;
            this.id = userId;
        }

        @Override
        protected Void doInBackground(Void... v) {

            if (!id.equals("0")) {

                if (((Project_18) getActivity().getApplication()).
                        getBitmapFromMemCache(id) != null) {
                    bitmap = ((Project_18) getActivity().getApplication()).
                            getBitmapFromMemCache(id);
                } else {
                    try {
                        bitmap = BitmapFactory.decodeStream((new URL("https://graph.facebook.com/" +
                                id + "/picture?width=" +
                                Project_18.pictureSize)).openConnection().getInputStream());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            else {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.app_logo_icon);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {

            // put the same thing in cached
            Bitmap unscaled = Bitmap.createBitmap(bitmap);

            // cache it
            ((Project_18) getActivity().getApplication()).addBitmapToMemoryCache(id, unscaled);

            // scale it
            bitmap = Project_18.BITMAP_RESIZER(unscaled, 150, 150);

            setBitmap(iv, bitmap);
        }
    }

    private void setBitmap(ImageView iv, Bitmap bitmap) {
        iv.setImageBitmap(bitmap);
    }

    private void goToEventInfo(String event_id) {
        // Delegate Activity switching to encapsulating activity
        ((MainAct)this.getActivity()).goToEventInfo(event_id, true);
    }

    public void pullNotifications(String userToPullFrom) {
        Project_18.getFB().child("NotifDatabase").
                child(userToPullFrom).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot notifSnap : dataSnapshot.getChildren()) {
                    HashMap<String, Object> notifMap = (HashMap<String, Object>) notifSnap.getValue();
                    if (((Long) notifMap.get("type")).intValue() == Notification2.TYPE_COMMENT_EVENT) {
                        notifs.add(new NotificationCommentEvent(notifMap));
                    } else if (((Long) notifMap.get("type")).intValue() == Notification2.TYPE_FRIEND_HOST) {
                        notifs.add(new NotificationFriendHost(notifMap));
                    } else if (((Long) notifMap.get("type")).intValue() == Notification2.TYPE_NEW_FOLLOW) {
                        notifs.add(new NotificationNewFollow(notifMap));
                    } else if (((Long) notifMap.get("type")).intValue() == Notification2.TYPE_JOINED_EVENT) {
                        notifs.add(new NotificationJoinedEvent(notifMap));
                    } else if (((Long) notifMap.get("type")).intValue() == Notification2.TYPE_INVITE_EVENT) {
                        notifs.add(new NotificationInviteEvent(notifMap));
                    } else if (((Long) notifMap.get("type")).intValue() == Notification2.TYPE_WELCOME) {
                        notifs.add(new NotificationWelcome(notifMap));
                    }
                }

                // There are no notifications to show
                if(notifs.isEmpty()) {
                    v.findViewById(R.id.noNotificationsText).setVisibility(View.VISIBLE);
                }

                // Make newest notifs show at the top
                Collections.reverse(notifs);

                // There are notifications to show, show them
                // Show them on the screen
                displayNotifications();

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    // Sets the current notif to "viewed" for user Me
    public void setToViewed(final Notification2 notif) {
        fb.child("NotifDatabase").child(Project_18.me.getID()).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot notifSnap : dataSnapshot.getChildren()) {
                            if (notifSnap.child("notifID").getValue().equals(notif.getNotifID())) {
                                notifSnap.getRef().child("viewed").setValue(true);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }
    public class NotificationOnTouchListener implements View.OnTouchListener {

        private Notification2 n;
        private LinearLayout container;

        public NotificationOnTouchListener(Notification2 notif, LinearLayout container) {
            this.n = notif;
            this.container = container;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_UP) {
                n.onNotificationClicked(getActivity());
                setToViewed(n);
                container.setBackground(getResources().getDrawable(R.drawable.border_notif_button_viewed));
            }
            if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (n.isViewed()) {
                    container.setBackground(getResources().getDrawable(R.drawable.border_notif_button_viewed));
                }
                // Not viewed specific
                else {
                    container.setBackground(getResources().getDrawable(R.drawable.border_notif_button_unviewed));
                }
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                container.setBackground(getResources().
                        getDrawable(R.drawable.border_event_button_pressed));
            }
            return true;
        }
    }
}
