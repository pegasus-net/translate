package com.icarus.words.engine;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.icarus.words.data.Word;
import com.icarus.words.engine.entity.Words;
import com.icarus.words.utils.StreamUtil;

import org.litepal.LitePal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class WordsEngine {
    public static void init(Context context) {
        new Thread() {
            @Override
            public void run() {
                long s = System.currentTimeMillis();
                InputStream open = null;
                try {
                    open = context.getAssets().open("data.db");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                File dir = new File(context.getFilesDir().getParent(), "databases");
                if (dir.exists()) {
                    if (!dir.isDirectory()) {
                        dir.delete();
                        dir.mkdir();
                    }
                } else {
                    dir.mkdir();
                }
                File file = new File(dir, "data.db");
                if (file.exists()) {
                    file.delete();
                }
                try {
                    file.createNewFile();
                    StreamUtil.copy(open, new FileOutputStream(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    public static List<Word> query(int level) {
        return LitePal.where("level = ?", Integer.toString(level)).find(Word.class);
    }
}
