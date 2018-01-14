package com.example.rajk.geofiretrial3.design;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.rajk.geofiretrial3.R;
import com.example.rajk.geofiretrial3.main.LoginActivity;
import com.example.rajk.geofiretrial3.model.SharedPreference;

public class FirstSlider extends AppCompatActivity {

    private ViewPager viewPager;
    private slideadapter myadapter;
    Button next, skip;
    private SharedPreference sharedPreference;
    private TextView[] dots;
    private LinearLayout dotslayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_slider);
        viewPager = (ViewPager) findViewById(R.id.slide_viewpager);
        myadapter = new slideadapter(this);
        sharedPreference = new SharedPreference(this);
        next = (Button) findViewById(R.id.next);
        skip = (Button) findViewById(R.id.skip);
        dotslayout = (LinearLayout) findViewById(R.id.layoutdots);

        if (!sharedPreference.check()) {
            sharedPreference.setfirst(true);
            Intent intent = new Intent(FirstSlider.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }

        addButtonDots(0);
        changecolor();
        viewPager.setAdapter(myadapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                addButtonDots(position);
                if (position == 3) {
                    next.setText("Proceed");
                    skip.setVisibility(View.GONE);
                } else {
                    next.setText("Proceed");
                    skip.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstSlider.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = viewPager.getCurrentItem() + 1;
                if (current < 4) {
                    viewPager.setCurrentItem(current);
                } else {
                    Intent intent = new Intent(FirstSlider.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }


    private void addButtonDots(int position) {
        dots = new TextView[4];
        int[] coloractive = getResources().getIntArray(R.array.dotactive);
        int[] colorinactive = getResources().getIntArray(R.array.dotinactive);
        dotslayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorinactive[position]);
            dotslayout.addView(dots[i]);
        }
        if (dots.length > 0) {
            dots[position].setTextColor(coloractive[position]);
        }
    }

    private void changecolor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }
}