package com.icarus.words.view.fragment;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.baidu.translate.asr.OnRecognizeListener;
import com.baidu.translate.asr.TransAsrClient;
import com.icarus.words.R;
import com.icarus.words.data.TranslateResult;
import com.icarus.words.engine.TranslateEngine;
import com.icarus.words.engine.entity.ErrorCode;
import com.icarus.words.view.activity.CollectActivity;
import com.icarus.words.view.activity.CropActivity;

import java.io.File;

import a.icarus.component.BaseFragment;
import a.icarus.utils.FileUtil;
import a.icarus.utils.SoftInputUtil;
import a.icarus.utils.Strings;
import a.icarus.utils.ToastUtil;
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
    public static final int REQUEST_PICTURE = 101;
    private static final int TAKE_PHOTO = 102;
    private static final int REQUEST_CROP = 103;
    public static final int REQUEST_COLLECT = 201;
    private ConstraintLayout inputText, inputVoice, inputTip;
    private EditText input;
    private TextView resultDisplay;
    private ImageView inputClear, resultCopy;
    private ImageView loading;
    private Button translate;
    private CheckBox collect;
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
    private TranslateResult result;
    private ImageView enterCollect;
    private Uri originalImageUri;
    private PopupWindow mPopWindow;
    private ValueAnimator animator;


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
        collect = findViewById(R.id.collect);
        loading = findViewById(R.id.loading);
        inputGroup = findViewById(R.id.group);
        tipImage = findViewById(R.id.tip_image);
        tipText = findViewById(R.id.tip_text);
        voiceCancel = findViewById(R.id.voice_cancel);
        voiceTrans = findViewById(R.id.voice_translate);
        wave = findViewById(R.id.wave);
        enterCollect = findViewById(R.id.enter_collect);

    }

    @Override
    protected void initData() {
        createWindow();
        setInputArea(inputText);
        loadingAnim = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        loadingAnim.setDuration(300);
        loadingAnim.setRepeatCount(Animation.INFINITE);
        loading.setVisibility(View.GONE);
        AnimationDrawable waveAnim = (AnimationDrawable) wave.getBackground();
        waveAnim.start();
        collect.setChecked(false);
        transFrom.setText(TranslateEngine.FROM.equals(TranslateEngine.CHINESE) ? "??????" : "??????");
        transTo.setText(TranslateEngine.TO.equals(TranslateEngine.CHINESE) ? "??????" : "??????");
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
                inputCount.setText(Strings.format("%d/2000", inputStr.length()));
                translate.setEnabled(!TextUtils.isEmpty(inputStr.trim()));
            }
        };
        input.addTextChangedListener(inputWatcher);
        input.setHorizontallyScrolling(false);
        input.setMaxLines(Integer.MAX_VALUE);
        input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                translate();
                return true;
            }
            return false;
        });
        clearInput();
        asrClient = TranslateEngine.getAsrClient();
        asrClient.setRecognizeListener((resultType, result) -> {
            if (resultType == OnRecognizeListener.TYPE_FINAL_RESULT) {
                if (result.getError() == 0) {
                    translateFinish(result.getAsrResult().trim(), result.getTransResult().trim());
                } else {
                    translateFailed(result.getError());
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
        enterCollect.setOnClickListener(v -> {
            Intent intent = new Intent(mActivity, CollectActivity.class);
            startActivityForResult(intent, REQUEST_COLLECT);
        });
        inputGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (isRecord) {
                cancelRecord();
            }
            clearInput();
            if (checkedId == R.id.rb_text) {
                setInputArea(inputText);
                type = TEXT;
                return;
            }
            setInputArea(inputTip);
            SoftInputUtil.hideSoftInput(input);
            if (checkedId == R.id.rb_image) {
                type = IMAGE;
                tipImage.setImageResource(R.drawable.tip_image);
                tipText.setText("??????????????????");
            } else if (checkedId == R.id.rb_voice) {
                type = VOICE;
                tipImage.setImageResource(R.drawable.tip_voice);
                tipText.setText("??????????????????");
            }
        });
        tipImage.setOnClickListener(v -> translateSpecial());
        voiceCancel.setOnClickListener(v -> cancelRecord());
        voiceTrans.setOnClickListener(v -> {
            asrClient.stopRecognize();
            isRecord = false;
            setInputArea(inputTip);
        });
        collect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (result == null || TextUtils.isEmpty(result.dst)) {
                if (isChecked) {
                    buttonView.setChecked(false);
                    ToastUtil.show("???????????????");
                }
                return;
            }
            if (isChecked) {
                result.save();
                ToastUtil.show("????????????");
            } else {
                if (result.isSaved()) {
                    result.delete();
                    ToastUtil.show("????????????");
                }
            }
        });
    }

    private void cancelRecord() {
        isRecord = false;
        asrClient.cancelRecognize();
        setInputArea(inputTip);
        translateFailed(ErrorCode.CANCEL);
    }


    @Override
    public void onPause() {
        super.onPause();
        if (isRecord) {
            cancelRecord();
        }
    }


    private void translateSpecial() {
        if (isTranslate) {
            ToastUtil.show("???????????????");
            return;
        }
        if (type == IMAGE) {
            windowOpen();
        }
        if (type == VOICE) {
            startRecordSafe();
        }
    }


    private void createWindow() {
        View contentView = LayoutInflater.from(mActivity).inflate(R.layout.window_upload_picture,
                (ViewGroup) rootView, false);
        mPopWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopWindow.setContentView(contentView);
        mPopWindow.setAnimationStyle(R.style.popupWindow_anim);
        mPopWindow.setBackgroundDrawable(new ColorDrawable(0));
        mPopWindow.setOnDismissListener(() -> screenAlphaAnimStart(0.5f, 1.0f, 300));
        contentView.findViewById(R.id.item_close).setOnClickListener(v -> windowClose());
        contentView.findViewById(R.id.item_0).setOnClickListener(v -> {
            File file = new File(mContext.getCacheDir(), "original_image.jpeg");
            originalImageUri = FileUtil.getEmptyUri(file);
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, originalImageUri);
            startActivityForResult(intent, TAKE_PHOTO);
            windowClose();
        });
        contentView.findViewById(R.id.item_1).setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, REQUEST_PICTURE);
            windowClose();
        });
    }

    private void windowOpen() {
        if (mPopWindow.isShowing()) return;
        mPopWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
        screenAlphaAnimStart(1.0f, 0.5f, 500);
    }

    private void screenAlphaAnimStart(float from, float to, int duration) {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
            animator = null;
        }
        animator = ValueAnimator.ofFloat(from, to);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            WindowManager.LayoutParams attributes = mActivity.getWindow().getAttributes();
            attributes.alpha = (float) animation.getAnimatedValue();
            mActivity.getWindow().setAttributes(attributes);
        });
        animator.start();
    }

    private void windowClose() {
        mPopWindow.dismiss();
    }

    public void startRecordSafe() {
        TranslateFragmentPermissionsDispatcher.startRecordWithPermissionCheck(this);
    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    public void startRecord() {
        setInputArea(inputVoice);
        isRecord = true;
        asrClient.startRecognize(TranslateEngine.FROM, TranslateEngine.TO);
        waitTranslateFinish();
    }

    @OnShowRationale(Manifest.permission.RECORD_AUDIO)
    public void show(PermissionRequest request) {
        request.proceed();
    }

    @OnNeverAskAgain(Manifest.permission.RECORD_AUDIO)
    public void never() {
        ToastUtil.show("??????????????????????????????");
    }

    @OnPermissionDenied(Manifest.permission.RECORD_AUDIO)
    public void denied() {
        ToastUtil.show("????????????????????????");
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
                transFrom.setText(TranslateEngine.FROM.equals(TranslateEngine.CHINESE) ? "??????" : "??????");
                transTo.setText(TranslateEngine.TO.equals(TranslateEngine.CHINESE) ? "??????" : "??????");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        exchange.startAnimation(rotate);
    }

    private void translate() {
        if (isTranslate) {
            ToastUtil.show("???????????????");
            return;
        }
        if (TextUtils.isEmpty(inputStr.trim())) {
            ToastUtil.show("????????????????????????");
            return;
        }
        waitTranslateFinish();
        TranslateEngine.textTranslate(inputStr, (state, response) -> {
            mActivity.runOnUiThread(() -> {
                if (state != 0) {
                    translateFailed(state);
                    return;
                }
                if (response.isSuccess()) {
                    translateFinish(response.getSrc().trim(), response.getDst().trim());
                } else {
                    translateFailed(response.getError());
                }
            });
            isTranslate = false;
        });
    }


    private void copy() {
        if (result != null && !TextUtils.isEmpty(result.dst)) {
            SoftInputUtil.copy(result.dst);
        } else {
            ToastUtil.show("???????????????");
        }
    }

    private void setInputArea(View view) {
        inputText.setVisibility(view == inputText ? View.VISIBLE : View.GONE);
        inputVoice.setVisibility(view == inputVoice ? View.VISIBLE : View.GONE);
        inputTip.setVisibility(view == inputTip ? View.VISIBLE : View.GONE);
    }

    private void resetInputArea() {
        clearInput();
        if (type != TEXT) {
            setInputArea(inputTip);
        }
    }

    private void clearInput() {
        input.setText("");
    }


    //TODO onActivityResult;
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case REQUEST_COLLECT:
                if (result != null && result.isSaved()) {
                    result.delete();
                }
                collect.setChecked(false);
                break;
            case TAKE_PHOTO: {
                CropActivity.actionStart(this, originalImageUri, REQUEST_CROP);
            }
            case REQUEST_PICTURE:
                if (data != null) {
                    CropActivity.actionStart(this, data.getData(), REQUEST_CROP);
                }
                break;
            case REQUEST_CROP:
                if (data != null) {
                    imgTranslate(data.getData());
                }
                break;
        }

    }

    private void imgTranslate(Uri uri) {
        ContentResolver resolver = mContext.getContentResolver();
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(resolver.openInputStream(uri));
            waitTranslateFinish();
            TranslateEngine.imgTranslate(bitmap, ocrResult -> {
                if (null != ocrResult) {
                    if (ocrResult.getError() == 0) {
                        translateFinish(ocrResult.getSumSrc().trim(), ocrResult.getSumDst().trim());
                    } else {
                        translateFailed(ocrResult.getError());
                    }
                } else {
                    translateFailed(ErrorCode.NET);
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
            translateFailed(ErrorCode.FILE);
        }
    }


    private void waitTranslateFinish() {
        isTranslate = true;
        result = null;
        collect.setChecked(false);
        SoftInputUtil.hideSoftInput(input);
        resultDisplay.setText("");
        loading.setVisibility(View.VISIBLE);
        loading.startAnimation(loadingAnim);
    }

    private void translateFinish(String src, String dst) {
        result = new TranslateResult(type, src, dst);
        loadingAnim.cancel();
        loading.setVisibility(View.GONE);
        resultDisplay.setTextColor(0xFF333333);
        resultDisplay.setText(dst);
        setInputArea(inputText);
        input.setText(src);
        isTranslate = false;
    }

    private void translateFailed(int code) {
        loadingAnim.cancel();
        loading.setVisibility(View.GONE);
        isTranslate = false;
        if (code != ErrorCode.CANCEL) {
            ToastUtil.show(code + "????????????");
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPopWindow != null && mPopWindow.isShowing()) {
            mPopWindow.dismiss();
        }
    }
}
