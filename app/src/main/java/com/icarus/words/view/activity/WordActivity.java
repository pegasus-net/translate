package com.icarus.words.view.activity;

import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.icarus.words.R;
import com.icarus.words.adapter.WordAdapter;
import com.icarus.words.data.Word;
import com.icarus.words.engine.WordsEngine;
import com.icarus.words.view.Bar;

import java.util.ArrayList;
import java.util.List;

import a.icarus.component.BaseActivity;
import androidx.recyclerview.widget.RecyclerView;

public class WordActivity extends BaseActivity {
    private ListView listView;
    private Bar letterBar;
    private List<Word> words;
    private TextView letter;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_word);
        listView = findViewById(R.id.list);
        letterBar = findViewById(R.id.letter_bar);
    }

    @Override
    protected void initData() {
        words = WordsEngine.query(1);
        listView.setAdapter(new WordAdapter(words, R.layout.item_word));
        listView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

            }
        });
        letterBar.setOnPositionChangeListener(p -> {

            for (int i = 0; i < words.size(); i++) {
                String c = words.get(i).en.substring(0, 1).toUpperCase();
                boolean b = c.equals(String.valueOf((char) ('A' + p)));
                if (b) {
                    listView.setSelection(i);
                    letter.setText(c);
                    break;
                }
            }

        });
    }

    @Override
    protected void initListener() {

    }
}
