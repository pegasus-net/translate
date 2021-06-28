package com.icarus.words.adapter;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.icarus.words.R;
import com.icarus.words.data.Word;

import java.util.List;

import a.icarus.impl.ListAdapter;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

public class WordAdapter extends ListAdapter<Word, WordAdapter.ViewHolder> {
    public WordAdapter(List<Word> list, int layoutId) {
        super(list, layoutId);
    }

    @Override
    protected ViewHolder onCreateViewHolder(View convertView) {
        return new ViewHolder(convertView);
    }

    @Override
    protected void onBindViewHolder(ViewHolder holder, Word item) {
        int position = list.indexOf(item);
        String letter = list.get(position).en.substring(0, 1);
        holder.top.setText(letter.toUpperCase());
        if (position == 0) {
            holder.top.setVisibility(View.VISIBLE);
            holder.split.setVisibility(View.GONE);
        } else {
            if (!letter.equalsIgnoreCase(list.get(position - 1).en.substring(0, 1))) {
                holder.top.setVisibility(View.VISIBLE);
                holder.split.setVisibility(View.GONE);
            } else {
                holder.top.setVisibility(View.GONE);
                holder.split.setVisibility(View.VISIBLE);
            }
        }
        holder.word.setText(item.en);
    }

    public static class ViewHolder extends ListAdapter.ViewHolder {

        private CheckBox checkBox;
        private TextView word, top;
        private ImageView imageView;
        private View split;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = findViewById(R.id.cb);
            word = findViewById(R.id.word);
            top = findViewById(R.id.first_letter);
            imageView = findViewById(R.id.imageView);
            split = findViewById(R.id.split);
        }

        public <T extends View> T findViewById(@IdRes int id) {
            return rootView.findViewById(id);
        }
    }
}
