package com.icarus.words.engine;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.icarus.words.engine.entity.ErrorCode;
import com.icarus.words.engine.entity.TextResponse;
import com.icarus.words.utils.MD5Util;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

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
        StringBuilder builder = new StringBuilder();
        builder.append(ROOT_URL).append("?")
                .append("q=").append(words).append("&")
                .append("from=").append(from).append("&")
                .append("to=").append(to).append("&")
                .append("appid=").append(APP_ID).append("&")
                .append("salt=").append(SALT).append("&")
                .append("sign=").append(MD5Util.encode(APP_ID + words + SALT + KEY, false));
        return builder.toString();
    }

    public void translateAsync(String from, String to, String words, TextCallBack callback) {
        words = words.replaceAll("\n", " ");
        String url = createUrl(words, from, to);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                callback.onResponse(ErrorCode.NET, null);
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                ResponseBody body = response.body();
                if (null == body) {
                    callback.onResponse(ErrorCode.NET, null);
                    return;
                }
                String json = body.string();
                try {
                    TextResponse textResponse = new Gson().fromJson(json, TextResponse.class);
                    if (null == textResponse) {
                        callback.onResponse(ErrorCode.JSON, null);
                        return;
                    }
                    callback.onResponse(0, textResponse);
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                    callback.onResponse(ErrorCode.JSON, null);
                }
            }
        });

    }

    public interface TextCallBack {
        void onResponse(int code, TextResponse response);
    }
}
