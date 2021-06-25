package com.icarus.words.utils;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.icarus.words.R;

import java.util.Objects;

import a.icarus.utils.ConversionTool;


public class BottomNavigationHelper {
    public static void displayBadge(BottomNavigationView bottomNavigationView, int position, int count) {
        try {
            final int maxSize = bottomNavigationView.getItemIconSize();
            if (position > maxSize || position < 0) {
                return;
            }
            final BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigationView.getChildAt(0);
            final View mTab = menuView.getChildAt(position);
            final BottomNavigationItemView itemView = (BottomNavigationItemView) mTab;
            View mBadge = itemView.getChildAt(2);
            if (mBadge == null) {
                mBadge = LayoutInflater.from(bottomNavigationView.getContext()).inflate(R.layout.badge, menuView, false);
                itemView.addView(mBadge);
            }
            if (count <= 0) {
                mBadge.setVisibility(View.GONE);
            } else {
                mBadge.setVisibility(View.VISIBLE);
                TextView textView = mBadge.findViewById(R.id.count);
                ViewGroup.LayoutParams layoutParams = textView.getLayoutParams();
                layoutParams.width = count < 100 ? (int) ConversionTool.dp2px(16) :
                        ViewGroup.LayoutParams.WRAP_CONTENT;
                textView.setLayoutParams(layoutParams);
                textView.setText(count < 100 ? String.valueOf(count) : "99+");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void bind(ViewPager pager, BottomNavigationView navigation) {
        final PagerAdapter adapter = pager.getAdapter();
        final Menu menu = navigation.getMenu();
        if (adapter == null || adapter.getCount() <= 0) {
            return;
        }
        if (menu == null || menu.size() <= 0) {
            return;
        }
        if (pager.getAdapter().getCount() != navigation.getMenu().size()) {
            return;
        }
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                navigation.setSelectedItemId(navigation.getMenu().getItem(position).getItemId());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        navigation.setOnNavigationItemSelectedListener(item -> {
            for (int i = 0; i < menu.size(); i++) {
                if (Objects.equals(menu.getItem(i), item)) {
                    pager.setCurrentItem(i, false);
                }
            }
            return true;
        });
    }
}
