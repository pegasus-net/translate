package com.icarus.words.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

import com.icarus.words.R;
import com.icarus.words.data.TranslateResult;

import java.util.List;

import a.icarus.impl.ListAdapter;

public class CollectAdapter extends ListAdapter<TranslateResult, CollectAdapter.ViewHolder> {
    public final int[] type;

    public CollectAdapter(List<TranslateResult> list, int layoutId) {
        super(list, layoutId);
        type = new int[]{R.drawable.type_text, R.drawable.type_image, R.drawable.type_voice};
    }


    @Override
    protected ViewHolder onCreateViewHolder(View convertView) {
        return new ViewHolder(convertView);
    }

    @Override
    protected void onBindViewHolder(ViewHolder holder, TranslateResult item) {
        holder.src.setText(item.src);
        SpannableString string = new SpannableString("图" + item.dst);
        ForegroundColorSpan span = new ForegroundColorSpan(Color.TRANSPARENT);
        string.setSpan(span,0,1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.dst.setText(string);
        holder.type.setImageResource(type[item.type]);
    }

    public static class ViewHolder extends ListAdapter.ViewHolder {
        public TextView src, dst;
        public ImageView type;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            src = findViewById(R.id.src);
            dst = findViewById(R.id.dst);
            type = findViewById(R.id.type);

        }

        public <T extends View> T findViewById(@IdRes int id) {
            return rootView.findViewById(id);
        }
    }

}
