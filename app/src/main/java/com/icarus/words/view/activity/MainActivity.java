package com.icarus.words.view.activity;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.icarus.words.R;
import com.icarus.words.utils.BottomNavigationHelper;
import com.icarus.words.view.fragment.TranslateFragment;

import java.util.ArrayList;
import java.util.List;

import a.icarus.component.BaseActivity;
import a.icarus.impl.ColorFragment;
import a.icarus.impl.FragmentAdapter;

public class MainActivity extends BaseActivity {

    private BottomNavigationView navigation;
    private ViewPager pager;
    private final List<Fragment> fragments = new ArrayList<>();


    @Override
    protected void initView() {
        setContentView(R.layout.activity_main);
        navigation = findViewById(R.id.bottom);
        pager = findViewById(R.id.view_pager);
    }

    @Override
    protected void initData() {
        fragments.add(new ColorFragment(0xffffff00));
        fragments.add(new ColorFragment(0xffff00ff));
        fragments.add(new TranslateFragment());
        fragments.add(new ColorFragment(0xff00ffff));
        pager.setAdapter(new FragmentAdapter(this, fragments));
        pager.setOffscreenPageLimit(3);
        BottomNavigationHelper.bind(pager, navigation);
    }

    @Override
    protected void initListener() {

    }

}