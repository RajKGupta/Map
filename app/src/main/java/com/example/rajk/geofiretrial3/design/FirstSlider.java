package com.example.rajk.geofiretrial3.design;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
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
    FloatingActionButton next;
    private SharedPreference sharedPreference;
    private TextView[] dots;
    private LinearLayout dotslayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        sharedPreference = new SharedPreference(this);

        if (!sharedPreference.check()) {
            Intent intent = new Intent(FirstSlider.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }

        setContentView(R.layout.activity_first_slider);
        viewPager = (ViewPager) findViewById(R.id.slide_viewpager);
        myadapter = new slideadapter(this);

        next = (FloatingActionButton) findViewById(R.id.next);
        dotslayout = (LinearLayout) findViewById(R.id.layoutdots);

        addButtonDots(0);
        changecolor();

        viewPager.setAdapter(myadapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                /*switch (position) {
                    case 0:
                        viewPager.setBackgroundColor(oneToTwo.with(positionOffset));
                        break;
                    case 1:
                        viewPager.setBackgroundColor(twoToThree.with(positionOffset));
                        break;
                    case 2:
                        viewPager.setBackgroundColor(threeToFour.with(positionOffset));
                        break;
                    case 3:
                        viewPager.setBackgroundColor(ContextCompat.getColor(FirstSlider.this, R.color.dark_dot_active_screen4));
                }*/
            }

            @Override
            public void onPageSelected(int position) {
                addButtonDots(position);
                if (position == 3) {
                    next.setImageResource(R.drawable.ic_proceed);
                } else {
                    next.setImageResource(R.drawable.ic_arrow);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = viewPager.getCurrentItem() + 1;
                if (current < 4) {
                    viewPager.setCurrentItem(current);
                } else {
                    sharedPreference.setfirst(false);
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
        dotslayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(coloractive[position]);
            dotslayout.addView(dots[i]);
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