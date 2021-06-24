package com.icarus.words.engine;

import android.content.Context;
import android.graphics.Bitmap;

import com.baidu.translate.asr.TransAsrClient;
import com.baidu.translate.asr.TransAsrConfig;
import com.baidu.translate.ocr.OcrCallback;
import com.baidu.translate.ocr.OcrClient;
import com.baidu.translate.ocr.OcrClientFactory;

public class TranslateEngine {
    public static String FROM;
    public static String TO;
    public static final String AUTO = "auto";
    public static final String ENGLISH = "en";
    public static final String CHINESE = "zh";
    public static final String JAPANESE = "jp";
    private static TextClient textClient;
    private static OcrClient ocrClient;
    private static TransAsrClient asrClient;

    public static void init(Context context, String appId, String key) {
        textClient = new TextClient(appId, key);
        ocrClient = OcrClientFactory.create(context, appId, key);
        TransAsrConfig config = new TransAsrConfig(appId, key);
        asrClient = new TransAsrClient(context, config);
        setMode(AUTO, CHINESE);
    }

    public static void setMode(String from, String to) {
        FROM = from;
        TO = to;
    }

    public static void textTranslate(String text, TextClient.TextCallBack callBack) {
        textTranslate(FROM, TO, text, callBack);
    }

    public static void textTranslate(String from, String to, String text, TextClient.TextCallBack callBack) {
        if (textClient != null) {
            textClient.translateAsync(from, to, text, callBack);
        }
    }

    public static void imgTranslate(Bitmap bitmap, OcrCallback callback) {
        imgTranslate(FROM, TO, bitmap, callback);
    }

    public static void imgTranslate(String from, String to, Bitmap bitmap, OcrCallback callback) {
        if (ocrClient != null) {
            ocrClient.getOcrResult(from, to, bitmap, callback);
        }
    }

    public static TransAsrClient getAsrClient() {
        return asrClient;
    }


}
