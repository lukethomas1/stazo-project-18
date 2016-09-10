package com.stazo.campus;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ericzhang on 5/6/16.
 */
public class TestFrag1 extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View frag1 = inflater.inflate(R.layout.test1, container, false);
        return frag1;
    }
}
