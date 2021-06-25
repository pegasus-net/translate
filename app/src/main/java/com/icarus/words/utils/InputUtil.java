package com.icarus.words.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import a.icarus.component.Icarus;
import a.icarus.utils.ToastUtil;

public class InputUtil {
    public static void hide(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void copy(String text) {
        copy(text, true);
    }

    public static void copy(String text, boolean notify) {
        ClipboardManager manager =
                (ClipboardManager) Icarus.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        manager.setPrimaryClip(ClipData.newPlainText("text", text));
        if (notify) {
            ToastUtil.show(Icarus.getContext(), "复制成功");
        }
    }
}
