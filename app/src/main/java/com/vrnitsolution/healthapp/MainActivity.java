package com.vrnitsolution.healthapp;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.NotificationCompat;
import androidx.viewpager.widget.ViewPager;

import com.vrnitsolution.healthapp.DashboardProfile.DashboardProfile;
import com.vrnitsolution.healthapp.adapter.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity {
    ViewPager mSlideViewPager;
    LinearLayout mDottLayout;
    TextView[] dots;
    ViewPagerAdapter viewPagerAdapter;
    AppCompatButton backbtn, nextbtn;

    SharedPreferences msharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        Window window = this.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mSlideViewPager = findViewById(R.id.viewPager);
        mDottLayout = findViewById(R.id.layoutindicator);

        backbtn = findViewById(R.id.backbtn);
        nextbtn = findViewById(R.id.nextbtn);

        viewPagerAdapter = new ViewPagerAdapter(this);
        mSlideViewPager.setAdapter(viewPagerAdapter);
        setUpIndication(0);
        mSlideViewPager.addOnPageChangeListener(viewlistner);

        msharedPreferences = getSharedPreferences("nySp", MODE_PRIVATE);
        boolean isFirstTime = msharedPreferences.getBoolean("firstTime", true);
        if (isFirstTime) {
            SharedPreferences.Editor editor = msharedPreferences.edit();
            editor.putBoolean("firstTime", false);
            editor.commit();
        } else {
            finish();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }


    public void Back(View view) {
        if (getItem(0) > 0) {
            mSlideViewPager.setCurrentItem(getItem(-1), true);
        }
    }

    public void Next(View view) {
        if (getItem(0) < 3) {
            mSlideViewPager.setCurrentItem(getItem(1), true);
        } else {
            finish();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    }

    public void Skip(View view) {
        finish();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }

    public void setUpIndication(int position) {
        dots = new TextView[4];
        mDottLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.inactive, getApplicationContext().getTheme()));
            mDottLayout.addView(dots[i]);
        }
        dots[position].setTextColor(getResources().getColor(R.color.active, getApplicationContext().getTheme()));
    }

    ViewPager.OnPageChangeListener viewlistner = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            setUpIndication(position);
            if (position > 0) {
                backbtn.setVisibility(View.VISIBLE);
            } else {
                backbtn.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private int getItem(int i) {
        return mSlideViewPager.getCurrentItem() + i;
    }
}