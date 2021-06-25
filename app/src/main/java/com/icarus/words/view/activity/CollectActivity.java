package com.icarus.words.view.activity;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.icarus.words.R;
import com.icarus.words.data.TranslateResult;

import org.litepal.LitePal;

import java.util.Collections;
import java.util.List;

import a.icarus.component.BaseActivity;

public class CollectActivity extends BaseActivity {

    private List<TranslateResult> results;
    private final int[] type = {R.drawable.type_text, R.drawable.type_image, R.drawable.type_voice};

    @Override
    protected void initView() {
        setContentView(R.layout.activity_collect);


    }

    @Override
    protected void initData() {
        results = LitePal.findAll(TranslateResult.class);
        Collections.reverse(results);
        ListView listView = findViewById(R.id.collect_list);
        listView.setAdapter(new Adapter());
    }

    @Override
    protected void initListener() {

    }

    private class Adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return results.size();
        }

        @Override
        public TranslateResult getItem(int position) {
            return results.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_item,
                    parent, false);
            ((TextView) inflate.findViewById(R.id.src)).setText(getItem(position).src);
            ((TextView) inflate.findViewById(R.id.dst)).setText("    " + getItem(position).dst);
            ((ImageView) inflate.findViewById(R.id.type)).setImageResource(type[getItem(position).type]);
            inflate.findViewById(R.id.delete).setOnClickListener(v -> {
                getItem(position).delete();
                results.remove(getItem(position));
                notifyDataSetChanged();
                if (position == 0){
                    setResult(RESULT_OK);
                }
            });
            return inflate;
        }
    }
}
