package com.stazo.project_18;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ericzhang on 6/20/16.
 */
public class ViewCommentFrag extends Fragment{
    private String passedEventID;
    private View v;

    public void submitComment() {
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
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.view_comment, container, false);
        final Firebase fb = ((Project_18) this.getActivity().getApplication()).getFB();
        final Context context = getContext();

        //POTENTIALLY BAD, SHOULD DO ASYNC INSTEAD, BUT MIGHT FUCK MESS UP VIEWS
        //DANGER DANGER DANGUH
        //CAUTION CAUTION CAUTION
        /*
        ALSO STUFF TO STILL KINDA FIX: The loading time on 10+ comments w different profiles, which is caused by loading a shitton of images

         */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        fb.child("CommentDatabase").child(this.passedEventID).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //get arraylist of comments
                        final ArrayList<Comment> commentList = new ArrayList<Comment>();

//                commentList = dataSnapshot.child("comments").getValue(
//                        new GenericTypeIndicator<ArrayList<String>>() {});

                        //get iterable snapshots, iterate through and add to commentList
                        Iterable<DataSnapshot> commentIterable = dataSnapshot.child("comments").getChildren();
                        while (commentIterable.iterator().hasNext()) {
                            //System.out.println(commentIterable.iterator().next().getValue());
                            commentList.add((Comment) commentIterable.iterator().next().getValue(Comment.class));
                        }

                        //print it
                        for (int i = 0; i < commentList.size(); i++) {
                            System.out.print(commentList.get(i).getUser_ID() + ": ");
                            System.out.println(commentList.get(i).getComment());
                        }

                        //show through views and layouts
                        for (int i = 0; i < commentList.size(); i++) {

                            //profile pic
                            Bitmap profPicBitmap;

                            //get cache and check ID against it
                            HashMap<String, Bitmap> imageCache = Project_18.cachedIdToBitmap;
                            if (imageCache.keySet().contains(commentList.get(i).getUser_ID())) {
                                System.out.println("cache hit");
                                profPicBitmap = imageCache.get(commentList.get(i).getUser_ID());
                            } else {
                                try {
                                    URL imageURL = new URL("https://graph.facebook.com/" + commentList.get(i).getUser_ID()
                                            + "/picture?type=large");
                                    profPicBitmap = Bitmap.createScaledBitmap(
                                            BitmapFactory.decodeStream(imageURL.openConnection().getInputStream()),
                                            150,
                                            150,
                                            true);
                                    Project_18.cachedIdToBitmap.put(commentList.get(i).getUser_ID(), profPicBitmap);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            ImageView profileView = new ImageView(context);
                            profileView.setImageBitmap(profPicBitmap);

                            //user_id
                            final TextView userText = new TextView(context);
                            if (Project_18.cachedIdToName.containsKey(commentList.get(i).getUser_ID())) {
                                userText.setText(Project_18.cachedIdToName.get(commentList.get(i).getUser_ID()));
                                System.out.println("name cache hit");
                            }
                            else {
                                fb.child("Users").child(commentList.get(i).getUser_ID()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        userText.setText((String)dataSnapshot.child("name").getValue());
                                        Project_18.cachedIdToName.put(dataSnapshot.getKey(), (String)dataSnapshot.child("name").getValue());
                                    }

                                    @Override
                                    public void onCancelled(FirebaseError firebaseError) {

                                    }
                                });
                            }

                            //comment
                            TextView commentText = new TextView(context);
                            commentText.setText(commentList.get(i).getComment());


                            //layout
                            LinearLayout mainLayout = (LinearLayout) v.findViewById(R.id.viewCommentLayout);
                            LinearLayout commentLayout = new LinearLayout(context);
                            commentLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                                    LayoutParams.MATCH_PARENT));
                            commentLayout.setOrientation(LinearLayout.HORIZONTAL);
                            LinearLayout textLayout = new LinearLayout(context);
                            textLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                                    LayoutParams.WRAP_CONTENT));
                            textLayout.setOrientation(LinearLayout.VERTICAL);

                            mainLayout.addView(commentLayout);
                            textLayout.addView(userText);
                            textLayout.addView(commentText);
                            commentLayout.addView(profileView);
                            commentLayout.addView(textLayout);

                            //layout params for views
                            profileView.setLayoutParams(new LayoutParams(getDPI(60), getDPI(70)));
                            LayoutParams userTextLayoutParams = new LayoutParams((getDPI(250)), getDPI(20));
                            userTextLayoutParams.setMargins(getDPI(10), 0, 0, 0);
                            userText.setLayoutParams(userTextLayoutParams);
                            userText.setTypeface(null, Typeface.BOLD);
                            userText.setTextSize(16);
                            LayoutParams commentTextLayoutParams = new LayoutParams(getDPI(250), LinearLayout.LayoutParams.WRAP_CONTENT);
                            commentTextLayoutParams.setMargins(getDPI(10), 0, 0, 0);
                            commentText.setLayoutParams(commentTextLayoutParams);

                            TextView spacer = new TextView(context);
                            View space = inflater.inflate(R.layout.spacer, null);
                            mainLayout.addView(space);

                        }

                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });

        final Button submitComment = (Button) v.findViewById(R.id.submitComment);
        submitComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitComment();
            }
        });

        return v;
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

    public void setEventID(String passedEventID) {
        this.passedEventID = passedEventID;
    }

}
