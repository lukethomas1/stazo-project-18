package com.stazo.project_18;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.view_comment, container, false);
        Firebase fb = ((Project_18) this.getActivity().getApplication()).getFB();

        //POTENTIALLY BAD, SHOULD DO ASYNC INSTEAD, BUT MIGHT FUCK MESS UP VIEWS
        //DANGER DANGER DANGUH
        //CAUTION CAUTION CAUTION
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        fb.child("CommentDatabase").child(this.passedEventID).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //get arraylist of comments
                        ArrayList<Comment> commentList = new ArrayList<Comment>();

//                commentList = dataSnapshot.child("comments").getValue(
//                        new GenericTypeIndicator<ArrayList<String>>() {});

                        //get iterable snapshots, iterate through and add to commentList
                        Iterable<DataSnapshot> commentIterable = dataSnapshot.child("comments").getChildren();
                        while(commentIterable.iterator().hasNext()) {
                            //System.out.println(commentIterable.iterator().next().getValue());
                            commentList.add((Comment) commentIterable.iterator().next().getValue(Comment.class));
                        }

                        //print it
                        for (int i = 0; i < commentList.size(); i++) {
                            System.out.print(commentList.get(i).getUser_ID() + ": ");
                            System.out.println(commentList.get(i).getComment());
                        }

                        //show through textviews
                        LinearLayout layout = (LinearLayout) v.findViewById(R.id.viewCommentLayout);
                        for (int i = 0; i < commentList.size(); i++) {

                            //profile pic
                            Bitmap profPicBitmap;
                            try {
                                URL imageURL = new URL("https://graph.facebook.com/" + commentList.get(i).getUser_ID()
                                        + "/picture?type=large");
                                profPicBitmap = Bitmap.createScaledBitmap(
                                        BitmapFactory.decodeStream(imageURL.openConnection().getInputStream()),
                                        150,
                                        150,
                                        true);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            ImageView ProfileView = new ImageView(getContext());
                            ProfileView.setImageBitmap(profPicBitmap);
                            layout.addView(ProfileView);

                            //user_id
                            final TextView userText = new TextView(getContext());

                            //userText.setText(commentList.get(i).getUser_ID());
                            GraphRequest request = GraphRequest.newMeRequest(
                                    AccessToken.getCurrentAccessToken(),
                                    new GraphRequest.GraphJSONObjectCallback() {
                                        @Override
                                        public void onCompleted(
                                                JSONObject object,
                                                GraphResponse response) {
                                            //add to userText
                                            String username;
                                            try {
                                                username = object.getString("name");
                                            }
                                            catch (Exception e) {username = "Error fetching name";}
                                            userText.setText(username);
                                        }
                                    });
                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "id,name,link");
                            request.setParameters(parameters);
                            request.executeAsync();

                            layout.addView(userText);

                            //comment
                            TextView commentText = new TextView(getContext());
                            commentText.setText(commentList.get(i).getComment());
                            layout.addView(commentText);
                        }

                    }
                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                    }
                });
        return v;
    }

    public void setEventID(String passedEventID) {
        this.passedEventID = passedEventID;
    }

}
