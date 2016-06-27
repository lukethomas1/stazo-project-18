package com.stazo.project_18;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class Welcome3Frag extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.welcome_3, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        Bitmap original = BitmapFactory.decodeResource(getResources(), R.drawable.welcome_1_image);
        float heightOverWidth = ((float) original.getHeight())/((float)original.getWidth());

        ((ImageView) getActivity().findViewById(R.id.welcome_3_image)).setImageBitmap(
                Bitmap.createScaledBitmap((BitmapFactory.decodeResource(getResources(),
                                R.drawable.welcome_1_image)),
                        600,
                        (int) (600 * heightOverWidth),
                        true));

    }
}
