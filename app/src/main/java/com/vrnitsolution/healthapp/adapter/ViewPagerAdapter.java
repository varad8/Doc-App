package com.vrnitsolution.healthapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.vrnitsolution.healthapp.R;

public class ViewPagerAdapter extends PagerAdapter {
    Context context;
    int[] images = {
            R.drawable.doctor_1, R.drawable.doctor_2,R.drawable.doctor_3, R.drawable.trackreport
    };
    int[] headings = {
            R.string.text1,R.string.text2, R.string.text3, R.string.text4
    };

    int[] description = {
            R.string.description1, R.string.description2, R.string.description3, R.string.description4,
    };

    public ViewPagerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (LinearLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        LayoutInflater layoutInflater=(LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view=layoutInflater.inflate(R.layout.slider_layout,container,false);
        ImageView slideimage=(ImageView) view.findViewById(R.id.image1);
        TextView slideheading=(TextView) view.findViewById(R.id.dc1);
        TextView slidedesc=(TextView) view.findViewById(R.id.des);


        slideimage.setImageResource(images[position]);
        slideheading.setText(headings[position]);
        slidedesc.setText(description[position]);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout)object);
    }
}

