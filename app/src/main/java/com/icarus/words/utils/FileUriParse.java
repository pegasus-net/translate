package com.icarus.words.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import com.icarus.words.App;

import java.io.File;
import java.io.IOException;

import androidx.core.content.FileProvider;

public class FileUriParse {
    public static Uri parse(File file) {
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(App.getContext(), "com.words.provider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }
}
