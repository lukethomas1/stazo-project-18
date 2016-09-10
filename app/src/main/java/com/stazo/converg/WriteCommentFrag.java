package com.stazo.converg;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.Firebase;

/**
 * Created by ericzhang on 6/18/16.
 */
public class WriteCommentFrag extends Fragment {
    private String passedEventID;
    private View v;

    public void submitComment() {
        Firebase fb = ((Converg) this.getActivity().getApplication()).getFB();
        String commentText = ((EditText) v.findViewById(R.id.commentText)).getText().toString();

        //used push instead of updating an arrray list, pushing it into the comments array of
        //the EventComments tied to an Event_ID
        String user_ID = ((Converg)this.getActivity().getApplication()).getMe().getID();
        Comment comment = new Comment(this.passedEventID, commentText, user_ID);
        fb.child("CommentDatabase").child(this.passedEventID).child("comments").push().setValue(comment);
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