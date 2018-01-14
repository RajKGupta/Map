package com.example.rajk.geofiretrial3.design;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.rajk.geofiretrial3.R;

/**
 * Created by Soumya on 1/14/2018.
 */

public class slideadapter extends PagerAdapter {
    Context context;
    LayoutInflater inflater;

    //List of Texts
    public String[] textlist = {"text1", "text2", "text3", "text4"};

    //List of Background colors
    public int[] bgcolors = {
            Color.rgb(229, 115, 115),
            Color.rgb(192, 108, 132),
            Color.rgb(108, 91, 123),
            Color.rgb(53, 92, 125)
    };

    public slideadapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return textlist.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == (LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.slide1, container, false);
        LinearLayout layoutslide = (LinearLayout) view.findViewById(R.id.slide_layout);
        TextView text = (TextView) view.findViewById(R.id.text);
        layoutslide.setBackgroundColor(bgcolors[position]);
        text.setText(textlist[position]);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
