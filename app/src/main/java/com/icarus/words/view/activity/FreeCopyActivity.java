package com.icarus.words.view.activity;

import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.icarus.words.R;

import a.icarus.component.BaseActivity;

public class FreeCopyActivity extends BaseActivity {
    public static final String COPY_DATA = "copy_data";
    private TextView copyArea;

    @Override
    protected void initTheme() {
        super.initTheme();
        Log.i("TAG", "initTheme: "+getActionBar());
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    @Override
    protected void initView() {
        setContentView(R.layout.activity_free_copy);
        copyArea = findViewById(R.id.copy_area);
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        String dataString = intent.getStringExtra(COPY_DATA);
        copyArea.setText(dataString);
    }

    @Override
    protected void initListener() {

    }
}
