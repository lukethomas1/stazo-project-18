package com.stazo.project_18;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ericzhang on 5/6/16.
 */
public class TestFrag2 extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View frag2 = inflater.inflate(R.layout.test2, container, false);
        return frag2;
    }
}