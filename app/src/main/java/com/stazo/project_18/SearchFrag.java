package com.stazo.project_18;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * to handle interaction events.
 */
public class SearchFrag extends Fragment {
    private static final int MAX_USERS = 3;
    private static final int MAX_FRIENDS = 5;

    private static final int eventsTextSize = 16;
    private static final int detailsTextSize = 12;

    private View v;
    private Toolbar toolbar;
    private LinearLayout queryButtonLayout, friendsButtonLayout, othersButtonLayout;
    private HashMap<String, String> allUsers;
    private HashMap<String, String> matchUsers;
    private ArrayList<Event> allEvents;
    private ArrayList<Event> matchEvents;
    private HashMap<String, String> friends;
    private ArrayList<UserButtonTask> tasks = new ArrayList<>();

    public SearchFrag() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_search, container, false);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        queryButtonLayout = (LinearLayout) getActivity().findViewById(R.id.queryButtonLayout);
        friendsButtonLayout = (LinearLayout) getActivity().findViewById(R.id.friendsButtonLayout);
        othersButtonLayout = (LinearLayout) getActivity().findViewById(R.id.othersButtonLayout);

        allEvents = ((Project_18) getActivity().getApplication()).getPulledEvents();

        allUsers = new HashMap<>(Project_18.allUsers);
        friends = new HashMap<>(Project_18.me.getFriends());
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.toolbar_search_menu, menu);

        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.app_name));

    }

    public void updateResults(String query) {

        for (UserButtonTask task: tasks) {
            task.cancel(true);
        }

        queryButtonLayout.removeAllViews();
        friendsButtonLayout.removeAllViews();
        othersButtonLayout.removeAllViews();


        if (query.equals(new String(""))) {
            return;
        }

        // UPDATE USERS
        matchUsers = new HashMap<String, String>();
        HashMap<String, String> matchFriends = new HashMap<>();
        HashMap<String, String> matchOthers = new HashMap<>();

        // look through friends first
        for (String id: friends.keySet()) {
            String name = friends.get(id);
            if (name.toLowerCase().contains(query.toLowerCase())) {
                matchFriends.put(id, name);
                if (matchFriends.size() == MAX_FRIENDS) {
                    break;
                }
            }
        }

        for (String id : allUsers.keySet()) {
            // we only want MAX_USERS loaded
            if (matchFriends.size() + matchOthers.size() >= MAX_USERS) {
                break;
            }
            if (id.equals(Project_18.me.getID()) || friends.containsKey(id)) {
                continue;
            }
            String name = allUsers.get(id);
            if (name.toLowerCase().contains(query.toLowerCase())) {
                matchOthers.put(id, name);
            }
        }


        matchUsers.putAll(matchFriends);
        matchUsers.putAll(matchOthers);

        for (final String id: matchUsers.keySet()) {
            LinearLayout container = new LinearLayout(getActivity());
            LinearLayout.LayoutParams lp = new LinearLayout.
                    LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            //lp.height = 120;
            lp.gravity = Gravity.CENTER;
            container.setLayoutParams(lp);
            container.setOrientation(LinearLayout.HORIZONTAL);
            container.setGravity(Gravity.CENTER);
            container.setBackgroundColor(getResources().getColor(R.color.white));
            container.setPadding(40,0,0,0);

            // set up button
            Button userButton = new Button(getContext());
            userButton.setText(matchUsers.get(id));
            makePretty(userButton);
            userButton.setOnTouchListener(new EventButtonOnTouchListener(id, container));

            // set up image

            final ImageView iv = new ImageView(getActivity());
            final Bitmap unscaledBitmap;

            if (((Project_18) getActivity().getApplication()).getBitmapFromMemCache(id) != null) {

                // grab image
                unscaledBitmap = ((Project_18) getActivity().getApplication()).getBitmapFromMemCache(id);

                // set iv
                Bitmap profPicBitmap = Project_18.BITMAP_RESIZER(unscaledBitmap, 140, 140);
                iv.setImageBitmap(profPicBitmap);

                makePretty(iv);

                // add to layout
                container.addView(iv);
                container.addView(userButton);
                if (friends.keySet().contains(id)) {
                    friendsButtonLayout.addView(container);
                }
                else {
                    othersButtonLayout.addView(container);
                }
            }

            else {
                UserButtonTask newTask = new UserButtonTask(id, userButton, container,
                        friends.keySet().contains(id));
                tasks.add(newTask);
                newTask.execute();
            }
        }

        // UPDATE EVENTS
        matchEvents = new ArrayList<Event>();
        ArrayList<Event> levelOne = new ArrayList<Event>();
        ArrayList<Event> levelTwo = new ArrayList<Event>();
        ArrayList<Event> levelThree = new ArrayList<Event>();

        for (Event e: allEvents) {
            // stop when we've found 10 name matches
            if (levelTwo.size() + levelThree.size() >= 10) {
                break;
            }

            switch (e.findRelevance(query)) {
                case 3:
                    levelThree.add(e);
                    break;
                case 2:
                    levelTwo.add(e);
                    break;
                case 1:
                    levelOne.add(e);
                    break;
                default:
                    break;
            }
        }

        Collections.sort(levelThree, new popularityCompare());
        Collections.sort(levelTwo, new popularityCompare());
        Collections.sort(levelOne, new popularityCompare());

        matchEvents.addAll(levelThree);
        matchEvents.addAll(levelTwo);
        matchEvents.addAll(levelOne);

        int counter = 0;
        for (final Event e: matchEvents) {

            // load at most 10 events
            if (counter >= 10) {
                break;
            }

            LinearLayout container = new LinearLayout(getActivity());
            EventButtonOnTouchListener listener = new EventButtonOnTouchListener(e, container);

            ((Project_18) getActivity().getApplication()).makeEventButton
                    (getActivity(), e, container, listener, false);

            // add to layout
            queryButtonLayout.addView(container);
            counter++;
        }

        if (matchUsers.isEmpty() && matchEvents.isEmpty()) {
            TextView emptyText = new TextView(getActivity());
            emptyText.setText("No matches found");
            queryButtonLayout.addView(emptyText);
            makePretty(emptyText);
        }
    }

    private class UserButtonTask extends AsyncTask<Void, Void, Void> {
        private String id;
        private Button userButton;
        private LinearLayout container;
        private ImageView iv = new ImageView(getActivity());
        private Bitmap unscaledBitmap;
        private boolean isFriend;

        public UserButtonTask(String id, Button userButton, LinearLayout container, boolean isFriend){
            this.id = id;
            this.userButton = userButton;
            this.container = container;
            this.isFriend = isFriend;
        }

        @Override
        protected Void doInBackground(Void... v) {
            try {
                unscaledBitmap =
                        BitmapFactory.decodeStream((new URL("https://graph.facebook.com/" +
                                id +
                                "/picture?width=" +
                                Project_18.pictureSize)).openConnection().getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void v) {

            // cache image
            ((Project_18) getActivity().getApplication()).
                    addBitmapToMemoryCache(id, Bitmap.createBitmap(unscaledBitmap));

            // set iv
            Bitmap profPicBitmap = Project_18.BITMAP_RESIZER(unscaledBitmap, 140, 140);
            iv.setImageBitmap(profPicBitmap);

            makePretty(iv);

            // add to Layout
            container.addView(iv);
            container.addView(userButton);

            if (isFriend) {
                friendsButtonLayout.addView(container);
            }
            else {
                othersButtonLayout.addView(container);
            }
        }
    }

    private class popularityCompare implements Comparator<Event> {
        @Override
        public int compare(Event e1, Event e2) {
            if (e1.getPopularity() < (e2.getPopularity())) {
                return 1;
            }
            if (e1.getPopularity() > (e2.getPopularity())) {
                return -1;
            }
            return 0;
        }
    }

    private void makePretty(Button button){
        RelativeLayout.LayoutParams lp;

            lp = new
                    RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);

        button.setTextSize(16);
        //button.setTypeface(Typeface.MONOSPACE);
        button.setAllCaps(false);
        button.setTypeface(null, Typeface.NORMAL);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setPadding(40, 0, 0, 0);
        button.setLayoutParams(lp);

        button.setBackground(null);

    }

    private void makePretty(TextView tv) {
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(16);
        tv.setBackgroundColor(getResources().getColor(R.color.white));
        tv.setTextColor(getResources().getColor(R.color.colorTextPrimary));
        tv.setHeight(150);
    }

    private void makePretty(ImageView iv) {
        LinearLayout.LayoutParams lp = new LinearLayout.
                LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_VERTICAL;
        lp.bottomMargin = 20;
        lp.topMargin = 20;
        iv.setBackground(null);
        iv.setLayoutParams(lp);
        iv.setAdjustViewBounds(true);
    }

    private void goToEventInfo(String event_id) {
        //((MainAct) getActivity()).simulateClick(event_id);
        ((MainAct) getActivity()).goToEventInfo(event_id, true);
    }
    private void goToOtherProfile(String user_id) {
        ((MainAct) getActivity()).goToOtherProfile(user_id);
    }

    private void makePretty(TextView eventName, ImageView iv, TextView numGoing,
                            TextView info, LinearLayout container) {

        LinearLayout.LayoutParams containerLP = new LinearLayout.
                LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        containerLP.gravity = Gravity.CENTER_VERTICAL;
        container.setLayoutParams(containerLP);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setBackgroundColor(getResources().getColor(R.color.white));
        //container.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        LinearLayout.LayoutParams numGoingLP=new LinearLayout.
                LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        numGoingLP.gravity = Gravity.CENTER_VERTICAL;
        numGoingLP.rightMargin = 20;
        numGoing.setLayoutParams(numGoingLP);

        LinearLayout.LayoutParams ivLP=new LinearLayout.
                LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        ivLP.gravity=Gravity.CENTER_VERTICAL;
        ivLP.rightMargin = 20;
        iv.setLayoutParams(ivLP);

        LinearLayout eventNameAndInfo = new LinearLayout(getActivity());
        LinearLayout.LayoutParams lp = new
                LinearLayout.LayoutParams(950,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity=Gravity.NO_GRAVITY;
        eventNameAndInfo.setLayoutParams(lp);
        eventNameAndInfo.setOrientation(LinearLayout.VERTICAL);

        info.setTextColor(getResources().getColor(R.color.colorDivider));
        info.setTextSize(detailsTextSize);
        info.setPadding(120, 20, 0, 20);
        info.setGravity(Gravity.CENTER_VERTICAL);

        eventName.setTextSize(eventsTextSize);
        eventName.setTypeface(null, Typeface.NORMAL);
        eventName.setGravity(Gravity.CENTER_VERTICAL);
        eventName.setPadding(40, 15, 0, 0);
        eventName.setBackground(null);
        eventName.setTextColor(getResources().getColor(R.color.colorTextPrimary));

        eventNameAndInfo.addView(eventName);
        eventNameAndInfo.addView(info);

        container.addView(eventNameAndInfo);
        container.addView(iv);
        container.addView(numGoing);
    }

    public class EventButtonOnTouchListener implements View.OnTouchListener {

        private Event e;
        private String userId;
        private LinearLayout container;

        public EventButtonOnTouchListener(Event e, LinearLayout container) {
            this.e = e;
            this.container = container;
        }

        public EventButtonOnTouchListener(String userId, LinearLayout container) {
            this.userId = userId;
            this.container = container;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (e != null) {
                    goToEventInfo(e.getEvent_id());
                }
                else {
                    goToOtherProfile(userId);
                }
                container.setBackgroundColor(getResources().getColor(R.color.white));
            }
            if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                container.setBackgroundColor(getResources().getColor(R.color.white));
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                container.setBackgroundColor(getResources().getColor(R.color.colorDividerLight));
            }
            return true;
        }

        public void setE(Event e) {
            this.e = e;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
