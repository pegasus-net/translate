package com.icarus.words.engine;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.icarus.words.engine.entity.ErrorCode;
import com.icarus.words.engine.entity.TextResponse;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import a.icarus.utils.MD5;
import a.icarus.utils.OkHttpUtil;
import a.icarus.utils.Strings;
import a.icarus.utils.ThreadPool;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class TextClient {
    private final String APP_ID;
    private final String KEY;
    public static final String ROOT_URL = "https://api.fanyi.baidu.com/api/trans/vip/translate";
    public static final String SALT = "1456225";

    public TextClient(String appId, String key) {
        APP_ID = appId;
        KEY = key;
    }

    private String createUrl(String words, String from, String to) {
        return ROOT_URL.concat("?")
                .concat("q=").concat(words).concat("&")
                .concat("from=").concat(from).concat("&")
                .concat("to=").concat(to).concat("&")
                .concat("appid=").concat(APP_ID).concat("&")
                .concat("salt=").concat(SALT).concat("&")
                .concat("sign=").concat(MD5.encode(APP_ID + words + SALT + KEY, false));

    }

    public void translateAsync(String from, String to, String words, TextCallBack callback) {
        words = words.replaceAll("\n", " ");
        String url = createUrl(words, from, to);
        OkHttpUtil.asyncCall(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onResponse(ErrorCode.NET, null);
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                ResponseBody body = response.body();
                int code = 0;
                TextResponse textResponse = null;
                if (null == body) {
                    code = ErrorCode.NET;
                } else {
                    String json = body.string();
                    try {
                        textResponse = new Gson().fromJson(json, TextResponse.class);
                        if (null == textResponse) {
                            code = ErrorCode.JSON;

                        }
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                        code = ErrorCode.JSON;
                        textResponse = null;
                    }
                }
                final int fCode = code;
                final TextResponse fResponse = textResponse;
                new Handler(Looper.getMainLooper()).post(() -> callback.onResponse(fCode, fResponse));
            }
        });
    }


    public interface TextCallBack {
        void onResponse(int code, TextResponse response);
    }
}
