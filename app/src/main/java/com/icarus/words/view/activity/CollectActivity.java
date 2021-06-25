package com.icarus.words.view.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.icarus.words.R;
import com.icarus.words.adapter.CollectAdapter;
import com.icarus.words.data.TranslateResult;
import com.icarus.words.utils.InputUtil;

import org.litepal.LitePal;

import java.util.Collections;
import java.util.List;

import a.icarus.component.BaseActivity;
import a.icarus.component.BottomPopupWindow;
import a.icarus.simpleImpl.ListAdapter;
import a.icarus.utils.ToastUtil;

public class CollectActivity extends BaseActivity {

    private List<TranslateResult> results;
    private final int[] type = {R.drawable.type_text, R.drawable.type_image, R.drawable.type_voice};
    private CollectAdapter adapter;
    private ListView listView;
    private BottomPopupWindow window;
    private int position = 0;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_collect);
        listView = findViewById(R.id.collect_list);

    }

    @Override
    protected void initData() {
        results = LitePal.findAll(TranslateResult.class);
        Collections.reverse(results);
        adapter = new CollectAdapter(results, R.layout.result_item);
        listView.setAdapter(adapter);
        View popupView = LayoutInflater.from(this).inflate(R.layout.window_copy_result, null);
        popupView.findViewById(R.id.item_0).setOnClickListener(v -> {
            InputUtil.copy(results.get(position).src);
            window.dismiss();
        });
        popupView.findViewById(R.id.item_1).setOnClickListener(v -> {
            InputUtil.copy(results.get(position).dst);
            window.dismiss();
        });
        popupView.findViewById(R.id.item_2).setOnClickListener(v -> {
            Intent intent = new Intent(this, FreeCopyActivity.class);
            String data = results.get(position).src + "\n" + results.get(position).dst;
            intent.putExtra(FreeCopyActivity.COPY_DATA, data);
            startActivity(intent);
            window.dismiss();
        });
        popupView.findViewById(R.id.item_3).setOnClickListener(v -> {
            if (position == 0) {
                setResult(RESULT_OK);
            }
            results.get(position).delete();
            results.remove(position);
            adapter.notifyDataSetChanged();
            window.dismiss();
        });
        popupView.findViewById(R.id.item_close).setOnClickListener(v -> window.dismiss());
        window = new BottomPopupWindow(popupView);
        window.setWindow(getWindow());

    }

    @Override
    protected void initListener() {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (window.isShowing()) return;
            CollectActivity.this.position = position;
            window.show();
        });
    }


}
