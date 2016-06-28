package com.stazo.project_18;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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

        for (String id: allUsers.keySet()) {
            if (id.equals(Project_18.me.getID())) {
                continue;
            }
            String name = allUsers.get(id);
            if (name.toLowerCase().contains(query.toLowerCase())) {
                if (friends.keySet().contains(id)) {
                    matchFriends.put(id, name);
                }
                else {
                    matchOthers.put(id, name);
                }
            }
        }

        matchUsers.putAll(matchFriends);
        matchUsers.putAll(matchOthers);

        for (final String id: matchUsers.keySet()) {
            LinearLayout container = new LinearLayout(getActivity());
            LinearLayout.LayoutParams lp = new LinearLayout.
                    LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 150);
            //lp.height = 120;
            lp.gravity = Gravity.CENTER;
            container.setLayoutParams(lp);
            container.setOrientation(LinearLayout.HORIZONTAL);
            container.setGravity(Gravity.CENTER);
            container.setBackgroundColor(getResources().getColor(R.color.white));

            // set up button
            Button userButton = new Button(getContext());
            userButton.setText(matchUsers.get(id));
            makePretty(userButton, false);
            userButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToOtherProfile(id);
                }
            });

            // set up image

            final ImageView iv = new ImageView(getActivity());
            final Bitmap unscaledBitmap;

            if (((Project_18) getActivity().getApplication()).getBitmapFromMemCache(id) != null) {

                // grab image
                unscaledBitmap = ((Project_18) getActivity().getApplication()).getBitmapFromMemCache(id);

                // set iv
                Bitmap profPicBitmap = Project_18.BITMAP_RESIZER(unscaledBitmap, 110, 110);
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

        for (final Event e: matchEvents) {
            Button eventButton = new Button(getContext());
            eventButton.setText(e.getName());
            makePretty(eventButton, true);
            eventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToEventInfo(e.getEvent_id());
                }
            });
            queryButtonLayout.addView(eventButton);
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
            Bitmap profPicBitmap = Project_18.BITMAP_RESIZER(unscaledBitmap, 110, 110);
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

    private void makePretty(Button button, boolean forEvent){
        RelativeLayout.LayoutParams lp;
        if (forEvent) {
            lp = new
                    RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
        }
        else {
            lp = new
                    RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
        }
        button.setTextSize(12);
        //button.setTypeface(Typeface.MONOSPACE);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER_VERTICAL);
        button.setPadding(40, 0, 0, 0);
        button.setLayoutParams(lp);
        if (forEvent) {
            button.setBackgroundColor(getResources().getColor(R.color.white));
        }
        else {
            button.setBackground(null);
        }
    }

    private void makePretty(TextView tv) {
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(16);
        tv.setBackgroundColor(getResources().getColor(R.color.white));
        tv.setTextColor(getResources().getColor(R.color.colorTextPrimary));
        tv.setHeight(150);
    }

    private void makePretty(ImageView iv) {
        iv.setBackground(null);
        iv.setAdjustViewBounds(true);
    }

    private void goToEventInfo(String event_id) {
        //((MainAct) getActivity()).simulateClick(event_id);
        ((MainAct) getActivity()).goToEventInfo(event_id);
    }
    private void goToOtherProfile(String user_id) {
        ((MainAct) getActivity()).goToOtherProfile(user_id);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
