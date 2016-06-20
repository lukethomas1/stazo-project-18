package com.stazo.project_18;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by ericzhang on 6/18/16.
 */
public class WriteCommentFrag extends Fragment {
    private String passedEventID;
    private View v;

    public void submitComment() {
        Firebase fb = ((Project_18) this.getActivity().getApplication()).getFB();
        String commentText = ((EditText) v.findViewById(R.id.commentText)).getText().toString();

        //make comment based off of eventinfo
//        Comment comment = new Comment(this.passedEventID);
//        comment.addComment(commentText);
//        fb.child("CommentDatabase").child(this.passedEventID).addListenerForSingleValueEvent(
//                new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        //get arraylist of comments
//                        ArrayList<String> commentList = dataSnapshot.child("comments").getValue(
//                                new GenericTypeIndicator<ArrayList<String>>() {
//                                });
//                        if (commentList != null) {
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(FirebaseError firebaseError) {
//                    }
//                });
//        fb.child("CommentDatabase").child(this.passedEventID).setValue(comment);

        //used push instead of updating an arrray list
        fb.child("CommentDatabase").child(this.passedEventID).child("comments").push().setValue(commentText);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.write_comment, container, false);

        final Button submitComment = (Button) v.findViewById(R.id.submitComment);
        submitComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitComment();
            }
        });
        return v;
    }

    public void setEventID(String passedEventID) {
        this.passedEventID = passedEventID;
    }

}