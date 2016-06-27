package com.stazo.project_18;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;
import android.util.LruCache;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;

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

    // Popularity thresholds
    public static final int POP_THRESH1 = 10;
    public static final int POP_THRESH2 = 20;

    public static User me;
    public static ArrayList<Event> pulledEvents = new ArrayList<Event>(); // list of all the events pulled
    public static ArrayList<Integer> filteredCategories = new ArrayList<>();
    public static int GMTOffset = ((new GregorianCalendar()).getTimeZone()).getRawOffset();
    public static long relevantTime = System.currentTimeMillis();
    public static String relevantText = new String();
    public Firebase getFB() { return new Firebase(fbString);}
    public User getMe() { return me; }
    public void setMe(User user) { me = user; }
    public static final String pictureSize = "250";
    public static final String pictureSizeHigh = "400";

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

    private void refreshPulledEvents() {
    }

    public ArrayList<Event> getPulledEvents (){return pulledEvents;}
    public void clearPulledEvents() {pulledEvents = new ArrayList<Event>();}

    // update the time we're interested in
    public void setRelevantTime(int progress) {
        relevantTime = System.currentTimeMillis() + (progress * 60*60*1000);
    }

    public long getRelevantTime() {return relevantTime;}

    public void setRelevantText(String newText) {
        relevantText = newText;
    }

    // returns list of event_ids in order of relevance
    public ArrayList<String> findRelevantEventIds () {
        ArrayList<String> relatedEventIds = new ArrayList<String>();
        for (Event e: pulledEvents) {
            int relevance = e.findRelevance(relevantText);
            switch (relevance) {
                case 2:
                    // if it is a relevant category, and at a relevant time
                    //if (filteredCategories.contains(new Integer(e.getType())) &&
                      //      e.isInTime(relevantTime)) {
                        // add to start
                        relatedEventIds.add(0, e.getEvent_id());
                    //}
                    break;
                case 1:

                    // if it is a relevant category, and at a relevant time
                    //if (filteredCategories.contains(new Integer(e.getType())) &&
                      //      e.isInTime(relevantTime)) {
                        // add to end
                        relatedEventIds.add(e.getEvent_id());
                    //}
                    break;
                default:
                    break;
            }
        }
        return relatedEventIds;
    }

    /*public ArrayList<Event> findRelevantEvents (final boolean noTime) {
        final Firebase fb = getFB();

        fb.child("Events").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        clearPulledEvents();
                        // For every event in fb.child("Events"), create event and displayEvent
                        for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                            // get the info, storage?
                            Event e = new Event(eventSnapshot.getValue(
                                    new GenericTypeIndicator<HashMap<String, Object>>() {
                                    }));

                            // add the event to the local ArrayList
                            addPulledEvent(e);
                        }

                        // remove this listener
                        fb.child("Events").removeEventListener(this);

                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
    }*/

    // filter based on search and categories, use time if !noTime
    public ArrayList<Event> findRelevantEvents (boolean noTime) {
        ArrayList<Event> relatedEvents = new ArrayList<Event>();
        for (Event e: pulledEvents) {
            int relevance = e.findRelevance(relevantText);
            switch (relevance) {
                case 2:
                    // if it is a relevant category, and at a relevant time
                    //if (filteredCategories.contains(new Integer(e.getType())) &&
                      //      (noTime || e.isInTime(relevantTime))) {
                        // add to start
                        relatedEvents.add(0, e);
                    //}
                    break;
                case 1:
                    // if it is a relevant category, and at a relevant time
                    //if (filteredCategories.contains(new Integer(e.getType())) &&
                      //      (noTime || e.isInTime(relevantTime))) {
                        // add to end
                        relatedEvents.add(e);
                    //}
                    break;
                default:
                    break;
            }
        }
        return relatedEvents;
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
}
