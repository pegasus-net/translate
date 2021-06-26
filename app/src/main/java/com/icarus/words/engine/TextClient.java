package com.icarus.words.engine;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.icarus.words.engine.entity.ErrorCode;
import com.icarus.words.engine.entity.TextResponse;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import a.icarus.utils.MD5;
import a.icarus.utils.OkHttpUtil;
import a.icarus.utils.Strings;
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
    public static final String SALT = String.valueOf(System.currentTimeMillis()/1000);

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
