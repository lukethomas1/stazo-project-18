package com.stazo.project_18;

/**
 * Created by ericzhang on 5/14/16.
 */
import android.*;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

public class EventInfoFrag extends Fragment implements GestureDetector.OnGestureListener {

    private Firebase fb;
    private static final int COM_PIC_SIZ = 150;
    private static final int CAM_ICON_SIZ = 100;

    public String passedEventID;
    private User currUser;
    public static Event currEvent;
    private View v;
    private View bottomSheet;
    private BottomSheetBehavior mBottomSheetBehavior;
    private User me;
    private Bitmap picBitmap;
    private Bitmap mainImageBitmap;
    private String cameraPhotoPath;
    private Bitmap[] imageArray = null;
    private long[] imageTimeArray = null;
    private String[] imageUserArray = null;
    boolean enableClick = true;
    private Iterator<DataSnapshot> urlIterator;
    private int numImagesLoaded;
    private int numCommentsLoaded = 0;

    // Joined scrollview stuff
    private int SECTION_SIZE = 5;
    private int page = 0;
    private InteractiveScrollViewHorizontal scrollView;
    private LayoutInflater inflater;

    private boolean autoOpen = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.event_info, container, false);
        v.setVisibility(View.INVISIBLE);
        fb = ((Project_18) this.getActivity().getApplication()).getFB();
        me = ((Project_18) this.getActivity().getApplication()).getMe();
        // Get the event_id to display
        //String event_id = this.passedEventID;
        // Display event info
        System.out.println("EVENT ID: " + this.passedEventID);
        grabEventInfo(this.passedEventID);

        bottomSheet = v.findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        //mBottomSheetBehavior.setPeekHeight(680);
        //mBottomSheetBehavior.setPeekHeight(610);
        if (autoOpen) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            v.findViewById(R.id.writeCommentLayout).setVisibility(View.VISIBLE);
        } else {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            v.findViewById(R.id.writeCommentLayout).setVisibility(View.GONE);
        }

        mBottomSheetBehavior.setHideable(true);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mBottomSheetBehavior.setPeekHeight(0);
                    v.findViewById(R.id.writeCommentLayout).setVisibility(View.GONE);
                }
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    v.findViewById(R.id.writeCommentLayout).setVisibility(View.VISIBLE);
                }
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    v.findViewById(R.id.writeCommentLayout).setVisibility(View.GONE);
                    if (getActivity() != null) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                    recycleImages();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // React to dragging events
            }
        });

        final Button attendButton = (Button) v.findViewById(R.id.attend);
        // if the user is already attending an event, change the button text to "Joined"
        if (me.getAttendingEvents().contains(passedEventID)) {
            attendButton.setBackgroundColor(getResources().getColor(R.color.colorDividerLight));
            attendButton.setTextColor(getResources().getColor(R.color.colorDivider));
            attendButton.setTypeface(null, Typeface.ITALIC);
            attendButton.setText("Joined");
        }
        // listener for attendButton
        attendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attendClick(attendButton);
            }
        });

        //set up write comment
        v.findViewById(R.id.writeCommentLayout).setVisibility(View.GONE);
        final Button submitComment = (Button) v.findViewById(R.id.submitComment);
        submitComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitComment();
            }
        });
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        this.inflater = inflater;
        //pull necessary images and comments
        pullComments(inflater);
//        pullMainImage();
//        pullEventImages();
        setUpImageIterator(); //also pulls first image
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /*((ImageView) getActivity().findViewById(R.id.upArrow)).setImageBitmap(
                Bitmap.createScaledBitmap((BitmapFactory.decodeResource(getResources(),
                                R.drawable.up_arrow_big)),
                        30,
                        30,
                        true));*/
        // set scrollView
        scrollView = (InteractiveScrollViewHorizontal) getActivity().findViewById(R.id.joinedScrollView);

        scrollView.setOnBottomReachedListener(
                new InteractiveScrollViewHorizontal.OnBottomReachedListener() {
                    @Override
                    public void onBottomReached() {
                        // do something
                        loadMore();
                    }
                }
        );
    }

    public void collapse() {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // GATES AND JUSTIN IGNORE CODE BETWEEN HERE...

    private synchronized void loadMore() {
        page++;
        generateJoined();
    }

    private void generateJoined() {
        //(new UserNamesTask(fb)).execute();
        final HashMap<String, String> idToName = new HashMap<String, String>();
        int startingPoint = page * SECTION_SIZE;
        if (startingPoint >= currEvent.getAttendees().size()) {
            return;
        }

        int numToLoad = Math.min(SECTION_SIZE, currEvent.getAttendees().size() - startingPoint);

        for (int i = page * SECTION_SIZE; ; i++) {

            if (numToLoad == 0) {
                Log.d("sizzle", "currEvent.getAttendees() is " + currEvent.getAttendees().toString());
                (new SetButtonTask(idToName)).execute();

                break;
            }

            final String id = currEvent.getAttendees().get(i);

            idToName.put(id, Project_18.allUsers.get(id));

            numToLoad--;
        }
    }

    private class SetButtonTask extends AsyncTask<Void, Void, Void> {

        private ConcurrentHashMap<String, Bitmap> idToBitmap =
                new ConcurrentHashMap<String, Bitmap>();

        private HashMap<String, String> userList = new HashMap<String, String>();

        public SetButtonTask(HashMap<String, String> userList) {
            this.userList = userList;
        }

        @Override
        protected Void doInBackground(Void... v) {

            for (String id: userList.keySet()) {

                if (((Project_18) getActivity().getApplication()).
                        getBitmapFromMemCache(id) != null) {
                    idToBitmap.put(id, ((Project_18) getActivity().getApplication()).
                            getBitmapFromMemCache(id));
                }

                else {
                    try {
                        idToBitmap.put(id,
                                BitmapFactory.decodeStream((new URL("https://graph.facebook.com/" +
                                        id +
                                        "/picture?width=" +
                                        Project_18.pictureSize)).openConnection().getInputStream()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {

            // put the same thing in cached
            for (String id: idToBitmap.keySet()) {
                Bitmap unscaled = Bitmap.createBitmap(idToBitmap.get(id));

                // cache it
                ((Project_18) getActivity().getApplication()).addBitmapToMemoryCache(id, unscaled);

                // scale it
                idToBitmap.put(id, Project_18.BITMAP_RESIZER(unscaled,
                        140,
                        140));
            }

            constructUsersLayout(idToBitmap, userList);
            scrollView.ready();
        }
    }

    private synchronized void constructUsersLayout (ConcurrentHashMap<String, Bitmap> idToBitmap,
                                                    HashMap<String, String> idToName) {

        ((LinearLayout) getActivity().findViewById(R.id.joinedLayout)).removeAllViews();

        for (String id: idToBitmap.keySet()) {
            addToUsersLayout(idToBitmap.get(id), idToName.get(id), id);
        }
    }

    // add button to the usersLayout
    private void addToUsersLayout(final Bitmap profPicBitmap, final String name,
                                  final String id) {


        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // the button we'll be building
                final ImageButton b = new ImageButton(getActivity().getApplicationContext());

                b.setImageBitmap(profPicBitmap);

                // touch animation
                /*b.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // set filter when pressed
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            b.setColorFilter(new
                                    PorterDuffColorFilter(getResources().getColor(R.color.colorDividerLight),
                                    PorterDuff.Mode.MULTIPLY));
                        }

                        // handle "click"
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            Log.d("myTag", "imageButton pressed");
                            if (id.equals(Project_18.me.getID())) {
                                MainAct.viewPager.setCurrentItem(MainAct.PROF_POS);
                            }
                            else {
                                ((MainAct) getActivity()).goToOtherProfile(id);
                            }
                        }

                        // remove filter on release/cancel
                        if (event.getAction() == MotionEvent.ACTION_UP ||
                                event.getAction() == MotionEvent.ACTION_CANCEL) {
                            b.clearColorFilter();
                        }
                        return true;
                    }
                });*/

                // contains button and name of the user
                LinearLayout buttonLayout = new LinearLayout(getActivity().getApplicationContext());

                // make button look good and add to buttonLayout
                makePretty(b, name, buttonLayout);

                ((LinearLayout) getActivity().findViewById(R.id.joinedLayout)).addView(buttonLayout);
            }
        });
    }

    private void makePretty(ImageButton b, String userName, LinearLayout buttonLayout) {
        //b.setBackground(getResources().getDrawable(R.drawable.button_pressed));

        b.setBackgroundColor(getResources().getColor(R.color.white));

        LinearLayout.LayoutParams bLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        bLP.gravity = Gravity.CENTER_HORIZONTAL;
        b.setPadding(0,0,0,0);
        b.setLayoutParams(bLP);

        TextView tv = new TextView(getActivity().getApplicationContext());
        tv.setText(userName.split(" ")[0]);
        tv.setTextSize(getDPI(3));
        tv.setTextColor(getResources().getColor(R.color.colorTextPrimary));

        makePretty(tv);

        LinearLayout.LayoutParams tLP =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        tLP.gravity = Gravity.CENTER_HORIZONTAL;
        tv.setLayoutParams(tLP);

        buttonLayout.setOrientation(LinearLayout.VERTICAL);
        buttonLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        buttonLayout.setPadding(0,0,20,0);

        buttonLayout.addView(b);
        buttonLayout.addView(tv);
    }

    private void makePretty(TextView tv) {
        tv.setTextColor(getResources().getColor(R.color.colorTextPrimary));
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // ... AND HERE

    public void hideEventInfo() {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public void toggleState() {

        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        else {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            //getActivity().findViewById(R.id.upArrow).setRotation(0);
        }
    }

    /* toggle frag state */
    public void toggleState(View v) {
        toggleState();
    }

    //setter method for main act to pass in eventID
    public void setEventID(String passedEventID) {
        this.passedEventID = passedEventID;
    }
    public void setAutoOpen(boolean autoOpen) {
        this.autoOpen = autoOpen;
    }


    // Pulls event info and delegates to showInfo to display the correct info
    private void grabEventInfo(final String event_id) {
        fb.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // get the info for the event
                        currEvent = new Event(dataSnapshot.child("Events").
                                child(event_id).getValue(
                                new GenericTypeIndicator<HashMap<String, Object>>() {
                                }));
                        if (currEvent.getName() == null) {
                            handleNonExistentEvent();
                            return;
                        }

                        // get the info for the user
                        currUser = new User((HashMap<String, Object>) dataSnapshot.child("Users").
                                child(currEvent.getCreator_id()).getValue());

                        // display event
                        showInfo(currEvent, currUser);

                        mBottomSheetBehavior.setPeekHeight(
                                //getActivity().findViewById(R.id.arrowButtonLayout).getHeight() +
                                getActivity().findViewById(R.id.measurement).getHeight());

                        // set text for number Joined
                        ((TextView) getActivity().findViewById(R.id.numJoinedText)).
                                setText("Joined (" + currEvent.getAttendees().size() + ")");

                        if (currEvent.getAttendees().size() == 0) {
                            getActivity().findViewById(R.id.noJoinedText).setVisibility(View.VISIBLE);
                        }

                        // generateJoined scrollview
                        generateJoined();

                        // remove this listener
                        fb.child("Events").child(event_id).removeEventListener(this);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
    }

    // Called from grabEventInfo, programatically updates the textviews to display the correct info
    // Justin TODO Update the textviews in the layout to show the correct info
    private void showInfo(Event e, User u) {

        //Initialize Local Variables
        TextView eventDate = (TextView) this.getActivity().findViewById(R.id.eventDate);
        TextView eventName = (TextView) this.getActivity().findViewById(R.id.eventName);
        TextView eventLoc = (TextView) this.getActivity().findViewById(R.id.locValue);
        TextView eventDescription = (TextView) this.getActivity().findViewById(R.id.eventDesc);
        TextView eventLength = (TextView) this.getActivity().findViewById(R.id.eventLength);
        TextView eventCreator = (TextView) this.getActivity().findViewById(R.id.eventCreator);
        TextView eventStart = (TextView) this.getActivity().findViewById(R.id.starts);
        TextView eventTimes = (TextView) this.getActivity().findViewById(R.id.times);
        TextView eventDesc = (TextView) this.getActivity().findViewById(R.id.desc);
        //TextView eventTime = (TextView) this.getActivity().findViewById(R.id.eventTimeTo);
        ImageView eventCreatorPic = (ImageView) this.getActivity().findViewById(R.id.creatorPic);
        long startHour = 0;
        long startMinute = 0;
        //End Initialization

        //ImageView eventIcon = (ImageView) this.getActivity().findViewById(R.id.eventIcon);
        int findType = e.getType();

        // setting the event info text fields
        eventLoc.setOnTouchListener(new LocationOnTouchListener(eventLoc));
        eventLoc.setText(e.getAddress(getActivity()));
        eventName.setText(e.getName());
        eventDescription.setVisibility(View.VISIBLE);
        eventDesc.setVisibility(View.VISIBLE);

        if(e.getDescription().length() == 0){
            eventDescription.setVisibility(View.GONE);
            eventDesc.setVisibility(View.GONE);
        } else {
            eventDescription.setText(e.getDescription());
        }
        eventCreator.setText(u.getName());

        // setting the event creator image
        getFBPhoto(u.getID());

        // show time fields
        //Initialize time
        long startTime = e.getStartTime();
        long endTime = e.getEndTime();
        Date start = new Date(startTime);
        Date end = new Date(endTime);

        Date now = new Date();

        //Set start time
        String startText = buildStartDay(start) + " at " + buildStartTime(start);
        eventDate.setText(startText);

        if(now.getTime() > start.getTime()){
            eventStart.setText("Started");
            long hourdiff = (start.getHours() - now.getHours() + 1);
            long mindiff = start.getMinutes() - now.getMinutes();
            if(mindiff < 0){
                hourdiff--;
                mindiff += 60;
            }
            eventTimes.setText("Ends in: " + hourdiff + " h " + mindiff + "m");
        }

        //Set event length
        String durationText = buildDurationTime(startTime, endTime);
        eventLength.setText(durationText);

        final Button deleteButtonBig = (Button) v.findViewById(R.id.deleteEventBig);
        final Button deleteButtonSmall = (Button) v.findViewById(R.id.deleteEventSmall);
        Log.d("EventInfoFrag", "CreatorID: " + currEvent.getCreator_id());
        Log.d("EventInfoFrag", "UserID: " + me.getID());
        Log.d("EventInfoFrag", "CreatorID == UserID? " + (currEvent.getCreator_id().equals(me.getID())));
        if (currEvent.getCreator_id().equals(me.getID())) {
            Log.d("EventInfoFrag", "My Event: Delete Button Shown");
            //deleteButtonSmall.setVisibility(View.VISIBLE);
            deleteButtonBig.setVisibility(View.VISIBLE);
        }
        else {
            Log.d("EventInfoFrag", "My Event: Delete Button Hidden");
            //deleteButtonSmall.setVisibility(View.GONE);
            deleteButtonBig.setVisibility(View.GONE);
        }

        v.setVisibility(View.VISIBLE);
    }

    public void getFBPhoto(final String id) {

        new Thread(new Runnable() {
            public void run() {
                try {
                    URL imageURL = new URL("https://graph.facebook.com/" +
                            id + "/picture?width=" + Project_18.pictureSize);
                    Log.d("EventInfoFrag", "https://graph.facebook.com/" +
                            id + "/picture?width=" + Project_18.pictureSize);
                    picBitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                final ImageView iv = (ImageView) v.findViewById(R.id.creatorPic);

                iv.post(new Runnable() {
                    public void run() {

                        // cache image
                        ((Project_18) getActivity().getApplication()).
                                addBitmapToMemoryCache(id, Bitmap.createBitmap(picBitmap));

                        // round the shape
                        // picBitmap = getRoundedShape(picBitmap, Project_18.pictureSize);

                        picBitmap = Project_18.BITMAP_RESIZER(picBitmap, 250, 250);
                        iv.setImageBitmap(picBitmap);
                        iv.setVisibility(View.VISIBLE);

                        // unhide-layout
                        (v.findViewById(R.id.creatorPic)).setVisibility(View.VISIBLE);
                    }

                });

                // handle touch/click
                /*iv.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        // set filter when pressed
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            iv.setColorFilter(new
                                    PorterDuffColorFilter(getResources().getColor(R.color.colorDividerLight),
                                    PorterDuff.Mode.MULTIPLY));
                        }

                        // handle "click"
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            Log.d("myTag", "imageButton pressed");
                            if (id.equals(Project_18.me.getID())) {
                                MainAct.viewPager.setCurrentItem(MainAct.PROF_POS);
                            } else {
                                ((MainAct) getActivity()).goToOtherProfile(id);
                            }
                        }

                        // remove filter on release/cancel
                        if (event.getAction() == MotionEvent.ACTION_UP ||
                                event.getAction() == MotionEvent.ACTION_CANCEL) {
                            iv.clearColorFilter();
                        }
                        return true;
                    }
                });*/

            }
        }).start();
    }

    public Bitmap getRoundedShape(Bitmap scaleBitmapImage, String pictureSize) {
        int targetWidth = Integer.parseInt(pictureSize);
        int targetHeight = Integer.parseInt(pictureSize);
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                targetHeight,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2,
                ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth),
                        ((float) targetHeight)) / 2),
                Path.Direction.CCW);

        canvas.clipPath(path);
        Bitmap sourceBitmap = scaleBitmapImage;
        canvas.drawBitmap(sourceBitmap,
                new Rect(0, 0, sourceBitmap.getWidth(),
                        sourceBitmap.getHeight()),
                new Rect(0, 0, targetWidth, targetHeight), null);
        return targetBitmap;
    }

    public String buildStartDay(Date start) {
        String finalString;
        String dayString;
        // get today's day in MM/dd format
        Calendar c = Calendar.getInstance();
        SimpleDateFormat today = new SimpleDateFormat("MM/dd", Locale.US);

        // convert start time to MM/dd format
        SimpleDateFormat startDay = new SimpleDateFormat("MM/dd", Locale.US);
        dayString = startDay.format(start);

        // compare today's date
        if (dayString.equals(today.format(c.getTime()))) {
            finalString = "Today";
        }
        else {
            startDay = new SimpleDateFormat("MMM dd", Locale.US);
            finalString = startDay.format(start);
        }
        return finalString;
    }

    public String buildStartTime(Date start) {
        SimpleDateFormat startTime = new SimpleDateFormat("h:mm a", Locale.US);
        return startTime.format(start);
    }

    public String buildDurationTime(long startTime, long endTime) {
        String finalString = "";
        long length = endTime - startTime;
        long eventHour = length/(1000 * 60 * 60);
        long eventMin = length/(1000 * 60) - eventHour*60;

        if (eventHour > 0) {
            finalString = finalString + eventHour;
            if (eventHour == 1) {
                finalString = finalString + " hr";
            }
            else {
                finalString = finalString + " hrs";
            }
            Log.d("EventInfoFrag", "eventMin: " + eventMin);
            if (eventMin > 0) {
                finalString = finalString + " and " + eventMin;
            }
        }
        if (eventMin > 0) {
            if (eventMin == 1) {
                finalString = finalString + " min";
            }
            else {
                finalString = finalString + " mins";
            }
        }

        return finalString;
    }

    // Camera stuff

    private void photoChooser() {
        final CharSequence[] items = { "Take Photo", "Select from Library",
                "Cancel" };
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(
                getContext());

        builder.setTitle("Choose option:");
        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    takePhoto();
                } else if (items[item].equals("Select from Library")) {
                    selectPhoto();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void selectPhoto() {
        if(ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 2); //2 is the callback number code for this specific call #magicnumbersftw #fuckstyle
        }

        //if no permissions, then ask
        else {
            System.out.println("no photo gallery permissions! :(");
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 2);

        }
    }

    private void takePhoto() {
        //just in case the phone doesn't have a camera
        if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            //check if capable of adding an image
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {

                //check if camera permissions are granted
                if(ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {

                    //preemptively set file location
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        System.out.println("Could not create file for camera photo");
                    }

                    // pass in file location and open camera
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(getActivity(),
                                "com.stazo.project_18.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, 1);
                    }
                }

                //if no permissions, then ask
                else {
                    System.out.println("no camera permissions! :(");
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{android.Manifest.permission.CAMERA}, 1);

                }
            }
            else {
                System.out.println("your phone can't use camera for some reason");
            }
        }
        else {
            //tell user u can't or smth
            System.out.println("no camera on ur cheapo phone lmao! :(");
        }
    }

    private File createImageFile() throws IOException {
        //just say the file name is timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "event_photo_" + timeStamp;
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        cameraPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //right after the user decies whether or not to give app camera permissions
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            }
            else {
                System.out.println("You didn't give us permission to use ur shitty camera :(");
            }
        }

        //right after the user decides whether or not to give app photo gallery permissions
        if (requestCode == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Now user should be able to access photo gallery
                selectPhoto();
            }
            else {
                System.out.println("You didn't give us permission to view ur shitty photos :(");
            }
        }

    }

    // On intent return with bitmap in data
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == android.app.Activity.RESULT_OK) {
            //also save the photo to gallery
//            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//            File f = new File(cameraPhotoPath);
//            Uri cameraPhotoUri = Uri.fromFile(f);
//            mediaScanIntent.setData(cameraPhotoUri);
//            this.sendBroadcast(mediaScanIntent);

            try {
                //rotate bitmap and save it back
                Matrix matrix = new Matrix();
                matrix.postRotate(getImageOrientation(cameraPhotoPath));
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inSampleSize = 2;
                Bitmap imageBitmap = BitmapFactory.decodeFile(cameraPhotoPath, options);
                Bitmap rotatedBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);

                File file = new File(cameraPhotoPath); // the File to save to
                OutputStream fOut = new FileOutputStream(file);
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
                fOut.close();

                //push to firebase
                pushEventImage(Uri.fromFile((new File(cameraPhotoPath))));
            }
            catch (Exception e) {
                System.out.println("Something went wrong when accessing photo from file path or pushing photo");
            }
        }

        if (requestCode == 2 && resultCode == android.app.Activity.RESULT_OK) {
            Uri imageUri = data.getData();
            try {

                Matrix matrix = new Matrix();
                matrix.postRotate(getImageOrientation2(imageUri));
                Bitmap imageBitmap = BitmapFactory.decodeFile(getRealPathFromURI(getContext(), imageUri));
                Bitmap rotatedBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);

                File file = new File(getRealPathFromURI(getContext(), imageUri)); // the File to save to
                OutputStream fOut = new FileOutputStream(file);
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
                fOut.close();

                //push to firebase
                pushEventImage(imageUri);
            }
            catch(Exception e) {
                System.out.println("Something went wrong when u selected an image or pushing photo");
            }
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static int getImageOrientation(String imagePath){
        int rotate = 0;
        try {

            File imageFile = new File(imagePath);
            System.out.println("getting exif");
            ExifInterface exif = new ExifInterface(
                    imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
            System.out.println("rotation: " + rotate);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public int getImageOrientation2(Uri photoUri) {
        int photoRotation = 0;
        boolean hasRotation = false;
        String[] projection = { MediaStore.Images.ImageColumns.ORIENTATION };
        try {
            Cursor cursor = getActivity().getContentResolver().query(photoUri, projection, null, null, null);
            if (cursor.moveToFirst()) {
                photoRotation = cursor.getInt(0);
                hasRotation = true;
            }
            cursor.close();
        } catch (Exception e) {}

        if (!hasRotation) {
            try {
                ExifInterface exif = new ExifInterface(photoUri.getPath());
                int exifRotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED);

                switch (exifRotation) {
                    case ExifInterface.ORIENTATION_ROTATE_90: {
                        photoRotation = 90;
                        break;
                    }
                    case ExifInterface.ORIENTATION_ROTATE_180: {
                        photoRotation = 180;
                        break;
                    }
                    case ExifInterface.ORIENTATION_ROTATE_270: {
                        photoRotation = 270;
                        break;
                    }
                }
            } catch(Exception e) {e.printStackTrace();};
        }
        System.out.println("photo rotation: " + photoRotation);
        return photoRotation;
    }

    public void pushEventImage(Uri imageFile) {

        //files stored in this format: EventImagesDatbase/eventid/userid_timestamp.jpg
        StorageReference storageRef = ((Project_18) getActivity().getApplication()).getFBStorage();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        StorageReference mainImageStorage = storageRef.child("EventImagesDatabase/" +
                this.passedEventID + "/" + me.getID() + "_" + timeStamp+ ".jpg");
        StorageMetadata metta = new StorageMetadata.Builder()
                .setCustomMetadata("Time", Long.toString(Calendar.getInstance().getTimeInMillis()))
                .setCustomMetadata("User", me.getID())
                .build();
        UploadTask uploadTask = mainImageStorage.putFile(imageFile, metta);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("your upload failed");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                System.out.println("your upload succeeded");
                System.out.println("download url is: " + taskSnapshot.getDownloadUrl());
                //push download url to image datbase in firebase
                fb.child("ImagesDatabase").child("EventImages").child(passedEventID).push().setValue(taskSnapshot.getDownloadUrl().toString());
                //for instant update
                pullEventImages();
            }
        });
    }

    private int currHighlightIndex;
    public void viewHighlights() {
        if (numImagesLoaded > 0) {
            currHighlightIndex = 0;

            final View highlightLayout = inflater.inflate(R.layout.stream_image_layout, null);
            ((FrameLayout) v.findViewById(R.id.eventFrameLayout)).addView(highlightLayout);

            final RelativeLayout imageLayout = (RelativeLayout) v.findViewById(R.id.streamImageLayout);
            final ImageView imageFrameView = (ImageView) v.findViewById(R.id.streamImageView);
            final TextView imageTimeView = (TextView) v.findViewById(R.id.streamTimeView);
            final TextView imageUserView = (TextView) v.findViewById(R.id.streamUserView);
            final LinearLayout loadingView = (LinearLayout) v.findViewById(R.id.streamLoadingText);
            loadingView.setClickable(true);

            imageFrameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println(enableClick);
                    if (enableClick) {
                        System.out.println("next image");
                        currHighlightIndex++;
                        pullNextEventImage();
                        if (currHighlightIndex < numImagesLoaded) {
                            if (imageArray[currHighlightIndex] != null) {
                                String userText = ((Project_18)getActivity().getApplication())
                                        .allUsers.get(imageUserArray[currHighlightIndex]);
                                imageUserView.setText(userText);
                                String timeText = new SimpleDateFormat("HH:mm a")
                                        .format(imageTimeArray[currHighlightIndex]);
                                imageTimeView.setText(timeText);
                                imageFrameView.setImageBitmap(imageArray[currHighlightIndex]);
                            }
                            else {
                                //loading
                                imageLayout.setVisibility(View.INVISIBLE);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        while (true) {
                                            SystemClock.sleep(2000);
                                            System.out.println("try");
                                            if (imageArray != null) {
                                                if (imageArray[currHighlightIndex] != null) {
                                                    imageLayout.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            String userText = ((Project_18)getActivity().getApplication())
                                                                    .allUsers.get(imageUserArray[currHighlightIndex]);
                                                            imageUserView.setText(userText);
                                                            String timeText = new SimpleDateFormat("HH:mm a")
                                                                    .format(imageTimeArray[currHighlightIndex]);
                                                            imageTimeView.setText(timeText);
                                                            imageFrameView.setImageBitmap(imageArray[currHighlightIndex]);
                                                            imageLayout.setVisibility(View.VISIBLE);
                                                        }
                                                    });
                                                    break;
                                                }
                                            }
                                            else {
                                                break;
                                            }
                                        }
                                    }
                                }).start();
                            }
                        }
                        else {
                            System.out.println("no more images to show");
                            //hide everything
//                            imageUserView.setText(null);
//                            imageTimeView.setText(null);
//                            imageFrameView.setImageBitmap(null);
//                            imageFrameView.setBackgroundColor(Color.TRANSPARENT);
//                            imageFrameView.setClickable(false);
                            ((ViewGroup) highlightLayout.getParent()).removeView(highlightLayout);
                        }
                    }
                }
            });

            pullNextEventImage();
            if (imageArray[currHighlightIndex] != null) {
                System.out.println("first image");
                String userText = ((Project_18)getActivity().getApplication())
                        .allUsers.get(imageUserArray[currHighlightIndex]);
                imageUserView.setText(userText);
                String timeText = new SimpleDateFormat("HH:mm a")
                        .format(imageTimeArray[currHighlightIndex]);
                imageTimeView.setText(timeText);
                imageFrameView.setImageBitmap(imageArray[currHighlightIndex]);

            }
            else {
                //loading
                System.out.println("loading");
                imageLayout.setVisibility(View.INVISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            SystemClock.sleep(2000);
                            System.out.println("try");
                            if (imageArray != null) {
                                if (imageArray[currHighlightIndex] != null) {
                                    imageLayout.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            String userText = ((Project_18)getActivity().getApplication())
                                                    .allUsers.get(imageUserArray[currHighlightIndex]);
                                            imageUserView.setText(userText);
                                            String timeText = new SimpleDateFormat("HH:mm a")
                                                    .format(imageTimeArray[currHighlightIndex]);
                                            imageTimeView.setText(timeText);
                                            imageFrameView.setImageBitmap(imageArray[currHighlightIndex]);
                                            imageLayout.setVisibility(View.VISIBLE);
                                        }
                                    });
                                    break;
                                }
                            }
                            else {
                                break;
                            }
                        }
                    }
                }).start();

            }



            class eric extends GestureDetector.SimpleOnGestureListener {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    //if user swipes down
                    if (e2.getY() - e1.getY() > 150 && Math.abs(velocityY) > 200) {
                        System.out.println("Bottom swipe");

                        //get rid of image view
                        imageUserView.setText(null);
                        imageTimeView.setText(null);
                        imageFrameView.setImageBitmap(null);
                        imageFrameView.setBackgroundColor(Color.TRANSPARENT);
                        imageFrameView.setEnabled(false);
                        ((ViewGroup) highlightLayout.getParent()).removeView(highlightLayout);
                        return false; // Top to bottom
                    }
                    return false;
                }
            }

            final GestureDetector gestureDetector = new GestureDetector(getActivity(), new eric());
            imageFrameView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return false;
                }
            });
            loadingView.setOnTouchListener((new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return false;
                }
            }));

        }

        else {
            System.out.println("show no pic dialog");
            final CharSequence[] items = {"OK"};
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(
                    getContext());

            builder.setTitle("This event has no pictures yet.");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (items[item].equals("OK")) {
                        dialog.dismiss();
                    }
                }
            });
            builder.show();
        }
    }

    public void setUpImageIterator() {
        System.out.println("generating list of url's");
        fb.child("ImagesDatabase").child("EventImages").child(this.passedEventID).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int childrenCount = (int) dataSnapshot.getChildrenCount();
                        imageArray = new Bitmap[childrenCount];
                        imageTimeArray = new long[childrenCount];
                        imageUserArray = new String[childrenCount];
                        Iterable<DataSnapshot> urlIterable = dataSnapshot.getChildren();
                        urlIterator = urlIterable.iterator();
                        numImagesLoaded = 0; //starts at 0 and gets inc for each next() in iterator loop
                        pullNextEventImage();
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
    }

    public void pullNextEventImage() {
        if (urlIterator != null) {
            if (urlIterator.hasNext()) {
                System.out.println("pulling event image: " + numImagesLoaded);
                try {
                    URL imageUrl = new URL(urlIterator.next().getValue().toString());
                    System.out.println(imageUrl.toString());
                    new Thread(new EventImageRunnable(numImagesLoaded, imageUrl)).start();
                }
                catch (Exception e) {
                    System.out.println("Exception: " + e.toString());
                }
                numImagesLoaded++;
            }
        }
    }

    public void pullEventImages() {
        System.out.println("pulling all event images");
        fb.child("ImagesDatabase").child("EventImages").child(this.passedEventID).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int childrenCount = (int) dataSnapshot.getChildrenCount();
                        imageArray = new Bitmap[childrenCount];
                        imageTimeArray = new long[childrenCount];
                        imageUserArray = new String[childrenCount];
                        Iterable<DataSnapshot> urlIterable = dataSnapshot.getChildren();
                        Iterator<DataSnapshot> iterator = urlIterable.iterator();
                        numImagesLoaded = 0; //starts at 0 and gets inc for each next() in iterator loop
                        while(iterator.hasNext()) {
                            try {
                                URL imageUrl = new URL(iterator.next().getValue().toString());
                                System.out.println(imageUrl.toString());
                                new Thread(new EventImageRunnable(numImagesLoaded, imageUrl)).start();
                            }
                            catch (Exception e) {
                                System.out.println("Exception: " + e.toString());
                            }
                            numImagesLoaded++;
                        }
                        if (numImagesLoaded != childrenCount) {
                            System.out.println("some URL got bopped in pullEventImages");
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });

    }

    private class EventImageRunnable implements Runnable {
        private int index;
        private URL imageUrl;
        public EventImageRunnable (int index, URL imageUrl) {
            this.index = index;
            this.imageUrl = imageUrl;
        }

        @Override
        public void run() {
            try {
                FirebaseStorage.getInstance().getReferenceFromUrl(String.valueOf(imageUrl))
                        .getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        if (storageMetadata.getCustomMetadata("Time") != null) {
                            imageTimeArray[index] = Long.parseLong(storageMetadata.getCustomMetadata("Time"));
                        }
                        imageUserArray[index] = storageMetadata.getCustomMetadata("User");
                    }
                });
//                Bitmap imageBitmap = BitmapFactory.decodeStream(imageUrl.openStream());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inSampleSize = 2;
                Bitmap imageBitmap = BitmapFactory.decodeStream(imageUrl.openStream(), null, options);
                if (imageArray != null) {
                    imageArray[index] =  imageBitmap;
                }
                else {
                    imageBitmap.recycle();
                }
                System.out.println("Received event image number " + index);
            }
            catch(Exception e) {
                System.out.println("Exception: " + e.toString());
            }
        }
    }

    public void viewCommentClick() {
        //open comment view window
        ViewCommentFrag viewFrag = new ViewCommentFrag();
        viewFrag.setEventID(this.passedEventID);
        FragmentTransaction trans = this.getActivity().getSupportFragmentManager().beginTransaction();
        trans.add(R.id.show_writeComment, viewFrag).addToBackStack("ViewCommentFrag").commit();
    }

    public void pullComments(final LayoutInflater inflater) {
        final Context context = getContext();
        fb.child("CommentDatabase").child(this.passedEventID).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //get arraylist of comments from iterable snapshots by iterating through and adding
                        final ArrayList<Comment> commentList = new ArrayList<Comment>();
                        Iterable<DataSnapshot> commentIterable = dataSnapshot.child("comments").getChildren();
                        Iterator<DataSnapshot> commentIterator = commentIterable.iterator();
                        while (commentIterator.hasNext()) {
                            commentList.add((Comment) commentIterator.next().getValue(Comment.class));
                        }

                        if (commentList.size() == 0) {
                            getActivity().findViewById(R.id.noCommentsText).setVisibility(View.VISIBLE);
                        }

                        //show through views and layouts
                        for (int i = numCommentsLoaded; i < commentList.size(); i++) {

                            final String id = commentList.get(i).getUser_ID();

                            //profile pic
                            Bitmap profileImage = null;
                            final ImageView profileView = new ImageView(context);
                            profileView.setImageBitmap(profileImage);
                            /*profileView.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    // set filter when pressed
                                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                        profileView.setColorFilter(new
                                                PorterDuffColorFilter(getResources().getColor(R.color.colorDividerLight),
                                                PorterDuff.Mode.MULTIPLY));
                                    }

                                    // handle "click"
                                    if (event.getAction() == MotionEvent.ACTION_UP) {
                                        Log.d("myTag", "imageButton pressed");
                                        if (id.equals(Project_18.me.getID())) {
                                            MainAct.viewPager.setCurrentItem(MainAct.PROF_POS);
                                        } else {
                                            ((MainAct) getActivity()).goToOtherProfile(id);
                                        }
                                    }

                                    // remove filter on release/cancel
                                    if (event.getAction() == MotionEvent.ACTION_UP ||
                                            event.getAction() == MotionEvent.ACTION_CANCEL) {
                                        profileView.clearColorFilter();
                                    }
                                    return true;
                                }
                            });*/
                            //get cache and check ID against it
                            //HashMap<String, Bitmap> imageCache = Project_18.cachedIdToBitmap;
                            //if this line is crashing, need to just save ref to activity(ask eric)
                            if (((Project_18) getActivity().getApplication()).
                                    getBitmapFromMemCache(commentList.get(i).getUser_ID()) != null) {
                                profileImage = ((Project_18) getActivity().getApplication()).
                                        getBitmapFromMemCache(commentList.get(i).getUser_ID());
                                profileView.setImageBitmap(Project_18.BITMAP_RESIZER(profileImage, COM_PIC_SIZ, COM_PIC_SIZ));
                            } else {
                                Thread profileThread = new Thread(new ProfilePicRunnable(profileImage, commentList.get(i).getUser_ID(), profileView));
                                profileThread.start();
                            }

                            //user_id
                            final TextView userText = new TextView(context);
                            if (Project_18.cachedIdToName.containsKey(commentList.get(i).getUser_ID())) {
                                userText.setText(Project_18.cachedIdToName.get(commentList.get(i).getUser_ID()));
                            } else {
                                fb.child("Users").child(commentList.get(i).getUser_ID()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        userText.setText((String) dataSnapshot.child("name").getValue());
                                        Project_18.cachedIdToName.put(dataSnapshot.getKey(), (String) dataSnapshot.child("name").getValue());
                                    }

                                    @Override
                                    public void onCancelled(FirebaseError firebaseError) {

                                    }
                                });
                            }

                            //comment
                            TextView commentText = new TextView(context);
                            commentText.setText(commentList.get(i).getComment());
                            commentText.setTextColor(getResources().getColor(R.color.colorTextPrimary));

                            //layout
                            LinearLayout mainLayout = (LinearLayout) v.findViewById(R.id.viewCommentLayout);
                            LinearLayout commentLayout = new LinearLayout(context);
                            commentLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT));
                            commentLayout.setOrientation(LinearLayout.HORIZONTAL);
                            LinearLayout textLayout = new LinearLayout(context);
                            textLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                            textLayout.setOrientation(LinearLayout.VERTICAL);

                            textLayout.addView(userText);
                            textLayout.addView(commentText);
                            commentLayout.addView(profileView);
                            commentLayout.addView(textLayout);

                            //layout params for views
                            profileView.setLayoutParams(new LinearLayout.LayoutParams(getDPI(60), getDPI(70)));
                            LinearLayout.LayoutParams userTextLayoutParams = new LinearLayout.LayoutParams((getDPI(250)), getDPI(20));
                            userTextLayoutParams.setMargins(getDPI(10), 0, 0, 0);
                            userText.setLayoutParams(userTextLayoutParams);
                            userText.setTypeface(null, Typeface.BOLD);
                            userText.setTextSize(16);
                            userText.setTextColor(getResources().getColor(R.color.colorTextPrimary));
                            LinearLayout.LayoutParams commentTextLayoutParams = new LinearLayout.LayoutParams(getDPI(250), LinearLayout.LayoutParams.WRAP_CONTENT);
                            commentTextLayoutParams.setMargins(getDPI(10), 0, 0, 0);
                            commentText.setLayoutParams(commentTextLayoutParams);

                            //spacer and inc counter
                            View space = inflater.inflate(R.layout.spacer, null);
                            getActivity().runOnUiThread(new UpdateViewRunnable(mainLayout, commentLayout, space));
                            numCommentsLoaded++;
                        }

                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
    }

    private class ProfilePicRunnable implements Runnable {
        private Bitmap profileImage;
        String user_ID;
        ImageView profileView;
        ProfilePicRunnable(Bitmap profileImage, String user_ID, ImageView profileView) {
            this.profileImage = profileImage;
            this.user_ID = user_ID;
            this.profileView = profileView;
        }
        public void run() {
            try {
                URL imageURL = new URL("https://graph.facebook.com/" + user_ID
                        + "/picture?width=" + Project_18.pictureSize);
                profileImage = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
                profileView.post(new Runnable() {
                    @Override
                    public void run() {
                        profileView.setImageBitmap(Project_18.BITMAP_RESIZER(profileImage,
                                COM_PIC_SIZ, COM_PIC_SIZ));
                    }
                });

                // add to cache
                ((Project_18) getActivity().getApplication()).
                        addBitmapToMemoryCache(user_ID, profileImage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private class UpdateViewRunnable implements Runnable {
        private LinearLayout mainLayout;
        private View addedView;
        private View addedView2;

        UpdateViewRunnable(LinearLayout mainLayout, View addedView, View addedView2) {
            this.mainLayout = mainLayout;
            this.addedView = addedView;
            this.addedView2 = addedView2;
        }
        @Override
        public void run() {
            mainLayout.addView(addedView);
            //mainLayout.addView(addedView2);
        }
    }

    public int getDPI(int size){
        DisplayMetrics metrics;
        metrics = new DisplayMetrics();
        if (getActivity() == null) {
            System.out.println("null");
        }
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return (size * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;
    }

    public void submitComment() {
        //if this line is crashing, need to just save ref to activity(ask eric)
        Firebase fb = ((Project_18) this.getActivity().getApplication()).getFB();
        String commentText = ((EditText) v.findViewById(R.id.commentText)).getText().toString();

        //used push instead of updating an arrray list, pushing it into the comments array of
        //the EventComments tied to an Event_ID
        String user_ID = ((Project_18)this.getActivity().getApplication()).getMe().getID();
        Comment comment = new Comment(this.passedEventID, commentText, user_ID);
        fb.child("CommentDatabase").child(this.passedEventID).child("comments").push().setValue(comment);

        //hide keyboard and remove text after comment is pushed
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        ((EditText) v.findViewById(R.id.commentText)).setText(null);

        // NOTIFICATION STUFF
        ArrayList<String> usersWhoCare = new ArrayList<>(EventInfoFrag.currEvent.getAttendees());

        // Add in the event creator if they aren't in it already
        if (!usersWhoCare.contains(currEvent.getCreator_id())) {
            usersWhoCare.add(currEvent.getCreator_id());
        }
        // Take me, the commenter, out of it
        if (usersWhoCare.contains(Project_18.me.getID())) {
            usersWhoCare.remove(Project_18.me.getID());
        }

        ArrayList<String> meList = new ArrayList<String>();
        meList.add(Project_18.me.getName());

        // send out notification
        (new NotificationCommentEvent(Notification2.TYPE_COMMENT_EVENT,
                meList,
                passedEventID,
                EventInfoFrag.currEvent.getName(),
                Project_18.me.getID())).
                pushToFirebase(fb, usersWhoCare);

        // if it's the first comment we need to hide this
        getActivity().findViewById(R.id.noCommentsText).setVisibility(View.GONE);
    }



    public void attendClick(Button b) {
        if(b.getText() == "Joined"){
            // get the info for the user
            me.unattendEvent(currEvent.getEvent_id(), fb);

            b.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            b.setTextColor(getResources().getColor(R.color.colorTextPrimary));
            b.setTypeface(null, Typeface.BOLD);
            b.setText("Join");

        } else {
            me.attendEvent(currEvent.getEvent_id(), currEvent.getName(), currEvent.getCreator_id(), fb);
            b.setBackgroundColor(getResources().getColor(R.color.colorDividerLight));
            b.setTextColor(getResources().getColor(R.color.colorDivider));
            b.setTypeface(null, Typeface.ITALIC);
            b.setText("Joined");

        }
    }

    public void deleteEvent() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());

        builder.setTitle("Delete Event");
        builder.setMessage("Are you sure? Like really sure?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                Log.i("EventInfoFrag", "Delete Event Confirmed");

                // Delete event
                currEvent.deleteFromFirebase(fb, currEvent.getCreator_id(), currEvent.getEvent_id());
                dialog.dismiss();
                startActivity(new Intent(getActivity(), MainAct.class));
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i("EventInfoFrag", "Delete Event Cancelled");

                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public String getPassedEventID() {
        return passedEventID;
    }

    public class LocationOnTouchListener implements View.OnTouchListener {

        private TextView container;

        public LocationOnTouchListener(TextView container) {
            this.container = container;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (MainAct.viewPager.getCurrentItem() == MainAct.MAP_POS) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                else {
                    Intent i = new Intent(getActivity(), MainAct.class);
                    i.putExtra("toEvent", currEvent.getEvent_id());
                    startActivity(i);
                }
                container.setTextColor(getResources().getColor(R.color.colorTextPrimary));
            }
            if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                container.setTextColor(getResources().getColor(R.color.colorTextPrimary));
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                container.setTextColor(getResources().getColor(R.color.colorAccentDark));
            }

            return true;
        }
    }

    private void handleNonExistentEvent() {
        Toast toast = Toast.makeText(getActivity(), "Event no longer exists", Toast.LENGTH_SHORT);
        toast.show();
        onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        Firebase.setAndroidContext(getContext());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recycleImages();
    }

    public void recycleImages() {

        if (imageArray != null) {
            System.out.println("Destroying view... freeing image memory alloc");
            for (int i = 0; i < imageArray.length; i++) {
                if (imageArray[i] != null) {
                    imageArray[i].recycle();
                }
            }
        }
        imageArray = null;
        if (mainImageBitmap != null) {
            mainImageBitmap.recycle();
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }
}