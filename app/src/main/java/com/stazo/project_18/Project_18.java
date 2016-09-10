package com.stazo.project_18;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.LruCache;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by isaacwang on 4/25/16.
 */
public class Project_18 extends Application {
    private static final String fbString = "https://stazo-project-18.firebaseio.com/";
    //private static final String fbString = "https://project-18-isaac.firebaseio.com/";
    private static final String fbStorageString = "gs://stazodatabase.appspot.com";

    // Popularity thresholds
    public static final int POP_THRESH1 = 10;
    public static final int POP_THRESH2 = 20;

    public static final int MARK_SIZE_1 = 90;
    public static final int MARK_SIZE_2 = 110;
    public static final int MARK_SIZE_3 = 140;


    private static int eventsTextSize = 16;
    private static int detailsTextSize = 12;

    public static User me;

    public static ArrayList<Event> getPulledEvents() {
        return pulledEvents;
    }

    public static void setPulledEvents(ArrayList<Event> pulledEvents) {
        Project_18.pulledEvents = pulledEvents;
    }

    public static ArrayList<Event> pulledEvents = new ArrayList<Event>(); // list of all the events pulled
    public static ArrayList<Integer> filteredCategories = new ArrayList<>();
    public static int GMTOffset = ((new GregorianCalendar()).getTimeZone()).getRawOffset();
    public static long relevantTime = System.currentTimeMillis();
    public static String relevantText = new String();
    public static Firebase getFB() { return new Firebase(fbString);}
    public static StorageReference getFBStorage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        return storage.getReferenceFromUrl(fbStorageString);}
    public User getMe() { return me; }
    public void setMe(User user) { me = user; }
    public static final String pictureSize = "250";
    public static final String pictureSizeHigh = "400";
    public static final String pictureSizeLow = "140";

    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    final int cacheSize = maxMemory / 8;

    public LruCache<String, Bitmap> memoryCache = new LruCache<String, Bitmap>(cacheSize) {
        @Override
        protected int sizeOf(String key, Bitmap bitmap) {
            // The cache size will be measured in kilobytes rather than
            // number of items.
            //return bitmap.getByteCount() / 1024;
            return 1;
        }
    };

    // store images
    //public static HashMap<String, Bitmap> cachedIdToBitmap = new HashMap<String, Bitmap>();
    public static HashMap<String, String> cachedIdToName = new HashMap<>();
    public static HashMap<String, String> allUsers = new HashMap<String, String>();

    public LruCache<String, Bitmap> getMemoryCache(){
        return memoryCache;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }

    // stores a pulled event locally (pulledEvents)
    public void addPulledEvent(Event e) {
        if (!pulledEvents.contains(e)) {
            pulledEvents.add(e);
        }
    }

    // update the time we're interested in
    public void setRelevantTime(int progress) {
        relevantTime = System.currentTimeMillis() + (progress * 60*60*1000);
    }

    public long getRelevantTime() {return relevantTime;}

    public static boolean isInTime(Event e) {
        return (e.getStartTime() < (System.currentTimeMillis() + 60*60*1000) &&
        e.getEndTime() > (System.currentTimeMillis()));
    }

    public void setRelevantText(String newText) {
        relevantText = newText;
    }

    public void pullAllUsers() {
        getFB().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> usersIterable = dataSnapshot.getChildren();
                for (DataSnapshot user : usersIterable) {
                    allUsers.put(user.getKey(), (String) user.child("name").getValue());
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public static Bitmap BITMAP_RESIZER(Bitmap bitmap,int newWidth,int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float ratioX = newWidth / (float) bitmap.getWidth();
        float ratioY = newHeight / (float) bitmap.getHeight();
        float middleX = newWidth / 2.0f;
        float middleY = newHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, middleX - bitmap.getWidth() / 2, middleY - bitmap.getHeight() / 2,
                new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;

    }

    public void makeEventButton(Context context, Event e, LinearLayout container,
                                View.OnTouchListener listener,
                                boolean withBorders, User user) {

        TextView eventName = new Button(context);
        eventName.setText(e.toString());
        ImageView iv = new ImageView(context);
        iv.setImageResource(R.drawable.icon_multiple_people);
        TextView numGoing = new TextView(context);
        numGoing.setText(Integer.toString(e.getAttendees().size()));
        TextView info = new TextView(context);
        info.setText(e.getTimeString(false));
        TextView hostOrJoined = new TextView(context);

        if (e.getCreator_id().equals(user.getID())) {
            hostOrJoined.setText("Host");
            hostOrJoined.setTextColor(getResources().getColor(R.color.colorPrimary));
        } else if (e.getAttendees().contains(user.getID())){
            hostOrJoined.setText("Joined");
            hostOrJoined.setTextColor(getResources().getColor(R.color.colorAccentDark));
        }

        // FORMATTING

        LinearLayout.LayoutParams containerLP = new LinearLayout.
                LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        containerLP.gravity = Gravity.CENTER_VERTICAL;
        container.setLayoutParams(containerLP);
        container.setOrientation(LinearLayout.HORIZONTAL);

        if (withBorders) {
            container.setBackground(getResources().getDrawable(R.drawable.border_event_button));
        } else {
            container.setBackgroundColor(getResources().getColor(R.color.white));
        }

        LinearLayout eventNameAndInfo = new LinearLayout(context);
        LinearLayout.LayoutParams lp = new
                LinearLayout.LayoutParams(950,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity=Gravity.NO_GRAVITY;
        eventNameAndInfo.setLayoutParams(lp);
        eventNameAndInfo.setOrientation(LinearLayout.VERTICAL);

        LinearLayout nameAndHost = new LinearLayout(context);
        LinearLayout.LayoutParams nameAndHostLP = new
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        nameAndHost.setLayoutParams(nameAndHostLP);
        nameAndHost.setOrientation(LinearLayout.HORIZONTAL);

        hostOrJoined.setPadding(50, 0, 0, 60);
        hostOrJoined.setTextSize(detailsTextSize);
        hostOrJoined.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams hostLP = new
                LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        hostOrJoined.setLayoutParams(hostLP);

        eventName.setTextSize(eventsTextSize);
        eventName.setTypeface(null, Typeface.NORMAL);
        eventName.setGravity(Gravity.CENTER_VERTICAL);
        eventName.setPadding(80, 0, 0, 0);
        eventName.setWidth(740);
        eventName.setAllCaps(false);
        eventName.setBackground(null);

        nameAndHost.addView(eventName);
        nameAndHost.addView(hostOrJoined);

        if (e.happeningSoon()) {
            info.setTextColor(getResources().getColor(R.color.colorAccentDark));
        } else {
            info.setTextColor(getResources().getColor(R.color.colorDivider));
        }

        info.setTextSize(detailsTextSize);
        info.setPadding(160, 0, 0, 20);
        info.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams numGoingLP=new LinearLayout.
                LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        numGoingLP.gravity = Gravity.CENTER_VERTICAL;
        numGoingLP.rightMargin = 20;
        numGoing.setLayoutParams(numGoingLP);
        numGoing.setTextColor(getResources().getColor(R.color.colorAccentDark));

        LinearLayout.LayoutParams ivLP=new LinearLayout.
                LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        ivLP.gravity=Gravity.CENTER_VERTICAL;
        ivLP.rightMargin = 20;
        iv.setLayoutParams(ivLP);
        iv.setColorFilter(getResources().getColor(R.color.colorTextPrimary));


        // ADDING VIEWS

        eventNameAndInfo.addView(nameAndHost);
        eventNameAndInfo.addView(info);

        container.addView(eventNameAndInfo);
        container.addView(iv);
        container.addView(numGoing);


        // LISTENERS
        container.setOnTouchListener(listener);
        eventName.setOnTouchListener(listener);
        iv.setOnTouchListener(listener);
        numGoing.setOnTouchListener(listener);
        info.setOnTouchListener(listener);
        hostOrJoined.setOnTouchListener(listener);
    }

}
