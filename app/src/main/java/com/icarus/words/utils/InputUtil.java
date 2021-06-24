package com.icarus.words.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class InputUtil {
    public static void hide(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
