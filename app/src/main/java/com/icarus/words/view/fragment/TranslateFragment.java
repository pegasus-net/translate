package com.icarus.words.view.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.baidu.translate.asr.OnRecognizeListener;
import com.baidu.translate.asr.TransAsrClient;
import com.icarus.words.R;
import com.icarus.words.engine.TranslateEngine;
import com.icarus.words.utils.InputUtil;

import a.icarus.component.BaseFragment;
import a.icarus.utils.FormatUtil;
import a.icarus.utils.ToastUtil;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class TranslateFragment extends BaseFragment {
    private static final int TEXT = 0;
    private static final int IMAGE = 1;
    private static final int VOICE = 2;
    private ConstraintLayout inputText, inputVoice, inputTip;
    private EditText input;
    private TextView resultDisplay;
    private ImageView inputClear, resultCopy;
    private ImageView loading;
    private Button translate;
    private ImageView exchange;
    private TextView transFrom, transTo, inputCount;
    private String inputStr;
    private boolean animStart = false;
    private boolean isTranslate = false;
    private RotateAnimation loadingAnim;
    private RadioGroup inputGroup;
    private ImageView tipImage;
    private TextView tipText;
    private int type = 0;
    private TransAsrClient asrClient;
    private ImageView voiceCancel, voiceTrans;
    private boolean isRecord = false;
    private ImageView wave;

    @Override
    protected int setLayout() {
        return R.layout.fragment_translate;
    }

    @Override
    protected void initView(View view) {
        inputText = findViewById(R.id.input_text);
        inputVoice = findViewById(R.id.input_voice);
        inputTip = findViewById(R.id.input_tip);
        input = findViewById(R.id.input);
        resultDisplay = findViewById(R.id.result);
        inputClear = findViewById(R.id.input_clear);
        resultCopy = findViewById(R.id.copy);
        translate = findViewById(R.id.translate);
        transFrom = findViewById(R.id.tv_from);
        transTo = findViewById(R.id.tv_to);
        inputCount = findViewById(R.id.input_count);
        exchange = findViewById(R.id.exchange);
        loading = findViewById(R.id.loading);
        inputGroup = findViewById(R.id.group);
        tipImage = findViewById(R.id.tip_image);
        tipText = findViewById(R.id.tip_text);
        voiceCancel = findViewById(R.id.voice_cancel);
        voiceTrans = findViewById(R.id.voice_translate);
        wave = findViewById(R.id.wave);

    }

    @Override
    protected void initData() {
        setInputArea(inputText);
        loadingAnim = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        loadingAnim.setDuration(300);
        loadingAnim.setRepeatCount(Animation.INFINITE);
        loading.setVisibility(View.GONE);
        AnimationDrawable waveAnim = (AnimationDrawable) wave.getBackground();
        waveAnim.start();
        transFrom.setText(TranslateEngine.FROM.equals(TranslateEngine.CHINESE) ? "中文" : "英文");
        transTo.setText(TranslateEngine.TO.equals(TranslateEngine.CHINESE) ? "中文" : "英文");
        TextWatcher inputWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                inputStr = input.getText().toString();
                inputCount.setText(FormatUtil.format("%d/2000", inputStr.length()));
                translate.setEnabled(inputStr.trim().length() != 0);
            }
        };
        input.addTextChangedListener(inputWatcher);
        input.setText("");
        asrClient = TranslateEngine.getAsrClient();
        asrClient.setRecognizeListener((resultType, result) -> {
            if (resultType == OnRecognizeListener.TYPE_PARTIAL_RESULT) return;
            if (resultType == OnRecognizeListener.TYPE_FINAL_RESULT) {
                if (result.getError() == 0) {
                    translateFinish(result.getAsrResult(), result.getTransResult());
                } else {
                    translateFailed();
                }
            }
        });


    }

    @Override
    protected void initListener() {
        exchange.setOnClickListener(v -> exchange());
        inputClear.setOnClickListener(v -> resetInputArea());
        resultCopy.setOnClickListener(v -> copy());
        translate.setOnClickListener(v -> translate());
        inputGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (isRecord) {
                cancelTape();
            }
            if (checkedId == R.id.rb_text) {
                setInputArea(inputText);
                type = TEXT;
                return;
            }
            setInputArea(inputTip);
            InputUtil.hide(input);
            input.setText("");
            if (checkedId == R.id.rb_image) {
                type = IMAGE;
                tipImage.setImageResource(R.drawable.tip_image);
                tipText.setText("点击上传图片");
            } else if (checkedId == R.id.rb_voice) {
                type = VOICE;
                tipImage.setImageResource(R.drawable.tip_voice);
                tipText.setText("点击开始录音");
            }
        });
        tipImage.setOnClickListener(v -> translateSpecial());
        voiceCancel.setOnClickListener(v -> cancelTape());
        voiceTrans.setOnClickListener(v -> {
            asrClient.stopRecognize();
            isRecord = false;
            setInputArea(inputTip);
        });
    }

    private void cancelTape() {
        isRecord = false;
        asrClient.cancelRecognize();
        setInputArea(inputTip);
        translateFailed();
    }

    private void translateSpecial() {
        if (isTranslate) {
            ToastUtil.show(mContext, "正在翻译中");
            return;
        }
        if (type == IMAGE) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, 100);
        }
        if (type == VOICE) {
            startRecordSafe();
        }
    }

    public void startRecordSafe() {
        TranslateFragmentPermissionsDispatcher.startRecordWithPermissionCheck(this);
    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    public void startRecord() {
        setInputArea(inputVoice);
        isRecord = true;
        asrClient.startRecognize(TranslateEngine.FROM, TranslateEngine.TO);
        waitTranslate();
    }

    @OnShowRationale(Manifest.permission.RECORD_AUDIO)
    public void show(PermissionRequest request) {
        request.proceed();
    }

    @OnNeverAskAgain(Manifest.permission.RECORD_AUDIO)
    public void never() {
        ToastUtil.show(mContext, "录音权限已被永久拒绝");
    }

    @OnPermissionDenied(Manifest.permission.RECORD_AUDIO)
    public void denied() {
        ToastUtil.show(mContext, "录音权限获取失败");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        TranslateFragmentPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void exchange() {
        if (animStart) return;
        RotateAnimation rotate = new RotateAnimation(0, 180,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animStart = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animStart = false;
                TranslateEngine.setMode(TranslateEngine.TO, TranslateEngine.FROM);
                transFrom.setText(TranslateEngine.FROM.equals(TranslateEngine.CHINESE) ? "中文" : "英文");
                transTo.setText(TranslateEngine.TO.equals(TranslateEngine.CHINESE) ? "中文" : "英文");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        exchange.startAnimation(rotate);
    }

    private void translate() {
        if (isTranslate) {
            ToastUtil.show(mContext, "正在翻译中");
            return;
        }
        waitTranslate();
        TranslateEngine.textTranslate(inputStr, (state, response) -> {
            mActivity.runOnUiThread(() -> {
                if (state && response.isSuccess()) {
                    translateFinish(response.getSrc(), response.getDst());
                } else {
                    translateFailed();
                }
            });
            isTranslate = false;
        });
    }


    private void copy() {
        String result = resultDisplay.getText().toString().trim();
        if (!TextUtils.isEmpty(result)) {
            ClipboardManager manager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            manager.setPrimaryClip(ClipData.newPlainText("text", result));
            ToastUtil.show(mContext, "复制成功");
        }
    }

    private void setInputArea(View view) {
        inputText.setVisibility(view == inputText ? View.VISIBLE : View.GONE);
        inputVoice.setVisibility(view == inputVoice ? View.VISIBLE : View.GONE);
        inputTip.setVisibility(view == inputTip ? View.VISIBLE : View.GONE);
    }

    private void resetInputArea() {
        input.setText("");
        if (type != TEXT) {
            setInputArea(inputTip);
        }
    }

    private <T extends View> T findViewById(@IdRes int id) {
        return rootView.findViewById(id);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            ContentResolver resolver = mContext.getContentResolver();
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(resolver.openInputStream(uri));
                waitTranslate();
                TranslateEngine.imgTranslate(bitmap, ocrResult -> {
                    if (null != ocrResult && ocrResult.getError() == 0) {
                        translateFinish(ocrResult.getSumSrc(), ocrResult.getSumDst());
                    } else {
                        translateFailed();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                translateFailed();
            }
        }
    }

    private void waitTranslate() {
        isTranslate = true;
        InputUtil.hide(input);
        resultDisplay.setText("");
        loading.setVisibility(View.VISIBLE);
        loading.startAnimation(loadingAnim);
    }

    private void translateFinish(String src, String dst) {
        loadingAnim.cancel();
        loading.setVisibility(View.GONE);
        resultDisplay.setTextColor(0xFF333333);
        resultDisplay.setText(dst);
        setInputArea(inputText);
        input.setText(src);
        isTranslate = false;
    }

    private void translateFailed() {
        loadingAnim.cancel();
        loading.setVisibility(View.GONE);
        isTranslate = false;
        ToastUtil.show(mContext, "翻译失败");
    }
}
