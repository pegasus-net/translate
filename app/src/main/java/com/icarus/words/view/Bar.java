package com.icarus.words.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class Bar extends View {
    private final Rect rect;
    private int width;
    private int height;
    private final Paint paint;
    private int position = -1;
    private OnPositionChangeListener listener;

    public Bar(Context context) {
        this(context, null);
    }

    public Bar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Bar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        paint.setTextSize(36);
        paint.setColor(0xFF666666);
        paint.setTextAlign(Paint.Align.CENTER);
        rect = new Rect();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getMeasuredWidth();
        height = getMeasuredHeight();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float height = this.height / 26.0f;
        float width = this.width;
        for (int i = 0; i < 26; i++) {
            String s = String.valueOf((char) ('A' + i));
            paint.getTextBounds(s, 0, 1, rect);
            canvas.drawText(s, width / 2.0f, height * i + height / 2.0f + (rect.bottom - rect.top) / 2.0f, paint);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                int p = (int) (event.getY() / (height / 26.0f));
                setPosition(p);
                break;
            case MotionEvent.ACTION_UP:
                position = -1;
                break;
        }
        return true;
    }

    public void setPosition(int position) {
        if (this.position == position) {
            return;
        }
        position = Math.max(0, Math.min(position, 26));
        this.position = position;
        if (listener != null) {
            listener.positionChanged(position);
        }
    }

    public void setOnPositionChangeListener(OnPositionChangeListener listener) {
        this.listener = listener;
        if (listener != null) {
            listener.positionChanged(position);
        }
    }

    public interface OnPositionChangeListener {
        void positionChanged(int p);
    }
}
