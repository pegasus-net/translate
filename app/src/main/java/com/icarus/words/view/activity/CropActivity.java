package com.icarus.words.view.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;

import com.icarus.words.R;
import com.icarus.words.view.CropView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import a.icarus.component.BaseActivity;
import a.icarus.utils.FileUtil;
import a.icarus.utils.Recycle;
import a.icarus.utils.ToastUtil;

public class CropActivity extends BaseActivity {
    private CropView cropView;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_crop);
        cropView = findViewById(R.id.crop_image);
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        Uri uri = intent.getData();
        InputStream is = null;
        try {
            int sampleSize = 1;
            is = getContentResolver().openInputStream(uri);
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inSampleSize = sampleSize;
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, option);
            cropView.setBitmap(bitmap);
            cropView.setBackgroundColor(0xFF000000);
            cropView.setMaxScale(8);
        } catch (IOException | OutOfMemoryError e) {
            e.printStackTrace();
            ToastUtil.show("图片加载失败");
            finish();
        } finally {
            Recycle.close(is);
        }
    }

    @Override
    protected void initListener() {
        findViewById(R.id.cancel).setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        findViewById(R.id.crop).setOnClickListener(v -> onSaveClicked());
    }

    private void onSaveClicked() {
        Bitmap crop = cropView.crop();
        File file = new File(getCacheDir(), System.currentTimeMillis() + "crop.jpeg");
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
            crop.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Recycle.close(bos);
        }
        Intent intent = new Intent();
        intent.setData(FileUtil.getUri(file));
        setResult(RESULT_OK, intent);
        finish();
    }
}
