package com.stazo.project_18;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

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

        fb.child("CommentDatabase").child(this.passedEventID).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //get arraylist of comments
                        ArrayList<String> commentList = new ArrayList<String>();

//                commentList = dataSnapshot.child("comments").getValue(
//                        new GenericTypeIndicator<ArrayList<String>>() {});

                        //get iterable snapshots, iterate through and add to commentList
                        Iterable<DataSnapshot> commentIterable = dataSnapshot.child("comments").getChildren();
                        while(commentIterable.iterator().hasNext()) {
                            //System.out.println(commentIterable.iterator().next().getValue());
                            commentList.add((String) commentIterable.iterator().next().getValue());
                        }

                        //print it
                        for (int i = 0; i < commentList.size(); i++) {
                            System.out.println(commentList.get(i));
                        }

                        //show through textviews
                        LinearLayout layout = (LinearLayout) v.findViewById(R.id.viewCommentLayout);
                        for (int i = 0; i < commentList.size(); i++) {
                            TextView commentText = new TextView(getContext());
                            commentText.setText(commentList.get(i));
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