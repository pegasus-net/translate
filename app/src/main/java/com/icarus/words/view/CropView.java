package com.icarus.words.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

@SuppressWarnings("unused")
public class CropView extends SurfaceView implements SurfaceHolder.Callback {


    private final Paint paint;
    private final Matrix matrix;
    private final RectF cropArea;
    private final RectF hCropLine;
    private final RectF vCropLine;
    private final RectF displayArea;
    private final Path pathA;
    private final Path pathB;
    private final Path pathX;
    private final float[] matrixValues;
    private RepeatThread renderThread;
    private Runnable runOnLayout;

    private float width = 0;
    private float height = 0;
    private float baseScale = 1.0f;
    private float tScale = 1.0f;
    private float paddingStart = 0;
    private float paddingTop = 0;
    private float lastX_0 = 0;
    private float lastY_0 = 0;
    private float lastX_1 = 0;
    private float lastY_1 = 0;
    private float lastScale = 1.0f;

    private int mode = 0;
    private static final int DRAG = 1;
    private static final int SCALE = 2;

    private int operationId = 0;
    private static final int NONE = 0;
    private static final int CROP_BOX_CENTER = 1;
    private static final int CROP_BOX_TOP = 1 << 1;
    private static final int CROP_BOX_LEFT = 1 << 2;
    private static final int CROP_BOX_BOTTOM = 1 << 3;
    private static final int CROP_BOX_RIGHT = 1 << 4;
    private static final int IMAGE = 1 << 5;
    private static final int CORRECTION = 1 << 6;
    private static final float MAX_SCALE_AUTO = -1.0f;

    private Bitmap bitmap;
    private int sensitivity = 50;
    private int lineLen = 40;
    private int lineWidth = 8;
    private int lineColor = 0xFF22AD81;
    private int shadowColor = 0x80000000;
    private int backgroundColor = 0xFFFFFFFF;
    private boolean allShow = true;
    private float maxScale = MAX_SCALE_AUTO;
    private float minSize = 60;


    public CropView(Context context) {
        this(context, null);
    }

    public CropView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        matrix = new Matrix();
        cropArea = new RectF();
        displayArea = new RectF();
        hCropLine = new RectF();
        vCropLine = new RectF();
        pathA = new Path();
        pathB = new Path();
        pathX = new Path();
        matrixValues = new float[9];
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        renderThread = new RepeatThread(() -> {
            Canvas canvas = getHolder().lockCanvas();
            if (canvas != null) {
                try {
                    drawView(canvas);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    getHolder().unlockCanvasAndPost(canvas);
                }
            }
        });
        renderThread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        this.width = width;
        this.height = height;
        if (runOnLayout != null) {
            runOnLayout.run();
            runOnLayout = null;
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        renderThread.cancel();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float dx;
        float dy;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mode = DRAG;
                lastX_0 = event.getX();
                lastY_0 = event.getY();
                operationId = getDragType(lastX_0, lastY_0);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (mode != NONE) {
                    mode = SCALE;
                }
                lastX_1 = event.getX(1);
                lastY_1 = event.getY(1);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                int pointerCount = event.getPointerCount();
                if (pointerCount == 3) {
                    mode = NONE;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                dx = event.getX() - lastX_0;
                dy = event.getY() - lastY_0;
                if (mode == DRAG) {
                    drag(dx, dy, operationId);
                } else if (mode == SCALE) {
                    if (event.getPointerCount() >= 2) {
                        double sqrt1 = Math.sqrt((lastX_0 - lastX_1) * (lastX_0 - lastX_1)
                                + (lastY_0 - lastY_1) * (lastY_0 - lastY_1));
                        double sqrt2 = Math.sqrt((event.getX() - event.getX(1)) * (event.getX() - event.getX(1))
                                + (event.getY() - event.getY(1)) * (event.getY() - event.getY(1)));
                        scale(sqrt1, sqrt2);

                        lastX_1 = event.getX(1);
                        lastY_1 = event.getY(1);
                    }
                }
                lastX_0 = event.getX();
                lastY_0 = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                operationId = NONE;
                lastScale = 1;
                break;
        }
        return true;
    }

    private void dragCropBox(float dx, float dy, int id) {
        float dw = displayArea.right - displayArea.left;
        float dh = displayArea.bottom - displayArea.top;
        float allowMinSize = Math.min(this.minSize, Math.min(dw, dh));
        switch (id) {
            case CORRECTION:
//                cropArea.left = Math.max(cropArea.left, displayArea.left);
//                cropArea.right = Math.min(cropArea.right, displayArea.right);
//                cropArea.top = Math.max(cropArea.top, displayArea.top);
//                cropArea.bottom = Math.min(cropArea.bottom, displayArea.bottom);
                while (cropArea.right - cropArea.left < allowMinSize) {
                    cropArea.left = Math.max(cropArea.left - 0.5f, displayArea.left);
                    cropArea.right = Math.min(cropArea.right + 0.5f, displayArea.right);
                }
                while (cropArea.bottom - cropArea.top < allowMinSize) {
                    cropArea.top = Math.max(cropArea.top - 0.5f, displayArea.top);
                    cropArea.bottom = Math.min(cropArea.bottom + 0.5f, displayArea.bottom);
                }
                break;
            case CROP_BOX_LEFT | CROP_BOX_TOP:
                cropArea.left = Math.min(cropArea.right - allowMinSize,
                        Math.max(cropArea.left + dx, displayArea.left));
                cropArea.top = Math.min(cropArea.bottom - allowMinSize,
                        Math.max(cropArea.top + dy, displayArea.top));
                break;
            case CROP_BOX_RIGHT | CROP_BOX_TOP:
                cropArea.right = Math.max(cropArea.left + allowMinSize,
                        Math.min(cropArea.right + dx, displayArea.right));
                cropArea.top = Math.min(cropArea.bottom - allowMinSize,
                        Math.max(cropArea.top + dy, displayArea.top));
                break;
            case CROP_BOX_CENTER | CROP_BOX_TOP:
                cropArea.top = Math.min(cropArea.bottom - allowMinSize,
                        Math.max(cropArea.top + dy, displayArea.top));
                break;
            case CROP_BOX_LEFT | CROP_BOX_BOTTOM:
                cropArea.left = Math.min(cropArea.right - allowMinSize,
                        Math.max(cropArea.left + dx, displayArea.left));
                cropArea.bottom = Math.max(cropArea.top + allowMinSize,
                        Math.min(cropArea.bottom + dy, displayArea.bottom));
                break;
            case CROP_BOX_RIGHT | CROP_BOX_BOTTOM:
                cropArea.right = Math.max(cropArea.left + allowMinSize,
                        Math.min(cropArea.right + dx, displayArea.right));
                cropArea.bottom = Math.max(cropArea.top + allowMinSize,
                        Math.min(cropArea.bottom + dy, displayArea.bottom));
                break;
            case CROP_BOX_CENTER | CROP_BOX_BOTTOM:
                cropArea.bottom = Math.max(cropArea.top + allowMinSize,
                        Math.min(cropArea.bottom + dy, displayArea.bottom));
                break;
            case CROP_BOX_LEFT | CROP_BOX_CENTER:
                cropArea.left = Math.min(cropArea.right - allowMinSize,
                        Math.max(cropArea.left + dx, displayArea.left));
                break;
            case CROP_BOX_RIGHT | CROP_BOX_CENTER:
                cropArea.right = Math.max(cropArea.left + allowMinSize,
                        Math.min(cropArea.right + dx, displayArea.right));
                break;
            case CROP_BOX_CENTER:
                float moveX;
                float moveY;
                if (cropArea.left + dx <= displayArea.left) {
                    moveX = displayArea.left - cropArea.left;
                } else if (cropArea.right + dx >= displayArea.right) {
                    moveX = displayArea.right - cropArea.right;
                } else {
                    moveX = dx;
                }
                if (cropArea.top + dy <= displayArea.top) {
                    moveY = displayArea.top - cropArea.top;
                } else if (cropArea.bottom + dy >= displayArea.bottom) {
                    moveY = displayArea.bottom - cropArea.bottom;
                } else {
                    moveY = dy;
                }
                cropArea.left = cropArea.left + moveX;
                cropArea.right = cropArea.right + moveX;
                cropArea.top = cropArea.top + moveY;
                cropArea.bottom = cropArea.bottom + moveY;
                break;
        }
    }

    private void drag(float dx, float dy, int operationId) {
        if (operationId == IMAGE) {
            applyTranslate(dx, dy);
        } else {
            dragCropBox(dx, dy, operationId);
        }
    }

    private void scale(double sqrt1, double sqrt2) {
        float scale = (float) (sqrt2 / sqrt1);
        if (Math.abs(sqrt2 - sqrt1) < 3) {
            scale = 1;
        }
        if (tScale * scale <= 1) {
            scale = 1 / tScale;
        }
        if (tScale * scale >= maxScale) {
            scale = maxScale / tScale;
        }
        if (Math.abs(scale - 1) >= 0.0000001f) {
            if (lastScale == 1) {
                lastScale = scale;
            }
            if ((lastScale > 1 && scale > 1) || (lastScale < 1 && scale < 1)) {
                tScale = tScale * scale;
                matrix.postScale(scale, scale, (lastX_0 + lastX_1) / 2, (lastY_0 + lastY_1) / 2);
                scaleChanged();
            }
        }
        lastScale = scale;
    }

    private void drawView(Canvas canvas) {
        canvas.drawColor(backgroundColor);
        if (bitmap == null) return;
        paint.setColor(0xFFFFFFFF);
        canvas.drawBitmap(bitmap, matrix, paint);

        paint.setColor(shadowColor);
        paint.setStyle(Paint.Style.FILL);
        pathA.rewind();
        pathA.addRect(cropArea, Path.Direction.CCW);
        hCropLine.set(0, 0, width, height);
        drawPathOut(canvas, hCropLine, cropArea);
        paint.setColor(lineColor);
        paint.setStyle(Paint.Style.FILL);
        float cw = cropArea.right - cropArea.left;
        float ch = cropArea.bottom - cropArea.top;
        float allowLineLen = Math.min(this.lineLen, Math.min(cw, ch));
        float allowLineWidth = Math.min(this.lineWidth, Math.min(cw, ch));
        hCropLine.set(cropArea.left, cropArea.top,
                cropArea.left + allowLineLen, cropArea.top + allowLineWidth);
        vCropLine.set(cropArea.left, cropArea.top,
                cropArea.left + allowLineWidth, cropArea.top + allowLineLen);
        drawPath(canvas, hCropLine, vCropLine);
        hCropLine.set(cropArea.right - allowLineLen, cropArea.top,
                cropArea.right, cropArea.top + allowLineWidth);
        vCropLine.set(cropArea.right - allowLineWidth, cropArea.top,
                cropArea.right, cropArea.top + allowLineLen);
        drawPath(canvas, hCropLine, vCropLine);
        hCropLine.set(cropArea.left, cropArea.bottom - allowLineWidth,
                cropArea.left + allowLineLen, cropArea.bottom);
        vCropLine.set(cropArea.left, cropArea.bottom - allowLineLen,
                cropArea.left + allowLineWidth, cropArea.bottom);
        drawPath(canvas, hCropLine, vCropLine);
        hCropLine.set(cropArea.right - allowLineLen, cropArea.bottom - allowLineWidth,
                cropArea.right, cropArea.bottom);
        vCropLine.set(cropArea.right - allowLineWidth, cropArea.bottom - allowLineLen,
                cropArea.right, cropArea.bottom);
        drawPath(canvas, hCropLine, vCropLine);
        if (allShow) {
            hCropLine.set((cropArea.right + cropArea.left) / 2 - allowLineLen / 2.0f, cropArea.top,
                    (cropArea.right + cropArea.left) / 2 + allowLineLen / 2.0f, cropArea.top + allowLineWidth);
            vCropLine.set((cropArea.right + cropArea.left) / 2 - allowLineLen / 2.0f, cropArea.top,
                    (cropArea.right + cropArea.left) / 2 + allowLineLen / 2.0f, cropArea.top + allowLineWidth);
            drawPath(canvas, hCropLine, vCropLine);
            hCropLine.set((cropArea.right + cropArea.left) / 2 - allowLineLen / 2.0f, cropArea.bottom - allowLineWidth,
                    (cropArea.right + cropArea.left) / 2 + allowLineLen / 2.0f, cropArea.bottom);
            vCropLine.set((cropArea.right + cropArea.left) / 2 - allowLineLen / 2.0f, cropArea.bottom - allowLineWidth,
                    (cropArea.right + cropArea.left) / 2 + allowLineLen / 2.0f, cropArea.bottom);
            drawPath(canvas, hCropLine, vCropLine);
            hCropLine.set(cropArea.left, (cropArea.top + cropArea.bottom) / 2 - allowLineLen / 2.0f,
                    cropArea.left + allowLineWidth, (cropArea.top + cropArea.bottom) / 2 + allowLineLen / 2.0f);
            vCropLine.set(cropArea.left, (cropArea.top + cropArea.bottom) / 2 - allowLineLen / 2.0f,
                    cropArea.left + allowLineWidth, (cropArea.top + cropArea.bottom) / 2 + allowLineLen / 2.0f);
            drawPath(canvas, vCropLine, vCropLine);
            hCropLine.set(cropArea.right - allowLineWidth, (cropArea.top + cropArea.bottom) / 2 - allowLineLen / 2.0f,
                    cropArea.right, (cropArea.top + cropArea.bottom) / 2 + allowLineLen / 2.0f);
            vCropLine.set(cropArea.right - allowLineWidth, (cropArea.top + cropArea.bottom) / 2 - allowLineLen / 2.0f,
                    cropArea.right, (cropArea.top + cropArea.bottom) / 2 + allowLineLen / 2.0f);
            drawPath(canvas, vCropLine, vCropLine);
        }
    }

    private void drawPathOut(Canvas canvas, RectF rectA, RectF rectB) {
        pathA.rewind();
        pathA.addRect(rectA, Path.Direction.CCW);
        pathB.rewind();
        pathB.addRect(rectB, Path.Direction.CCW);
        pathX.op(pathA, pathB, Path.Op.DIFFERENCE);
        canvas.drawPath(pathX, paint);
    }

    private void drawPath(Canvas canvas, RectF rectA, RectF rectB) {
        pathA.rewind();
        pathA.addRect(rectA, Path.Direction.CCW);
        pathB.rewind();
        pathB.addRect(rectB, Path.Direction.CCW);
        pathX.op(pathA, pathB, Path.Op.UNION);
        canvas.drawPath(pathX, paint);
    }


    private void setBaseScale() {
        if (bitmap != null) {
            int bw = bitmap.getWidth();
            int bh = bitmap.getHeight();
            if (bw > width || bh > height) {
                baseScale = Math.min(height / bh, width / bw);
            } else {
                baseScale = 1;
            }
            tScale = 1.0f;
            if (maxScale == MAX_SCALE_AUTO) {
                maxScale = 2 / baseScale;
            }
            matrix.setScale(baseScale, baseScale);
            scaleChanged();
            float displayWidth = displayArea.right - displayArea.left;
            float displayHeight = displayArea.bottom - displayArea.top;
            cropArea.set(displayArea.left + displayWidth / 4,
                    displayArea.top + displayHeight / 4,
                    displayArea.right - displayWidth / 4,
                    displayArea.bottom - displayHeight / 4);
            dragCropBox(0, 0, CORRECTION);
        }
    }

    private int getDragType(float x, float y) {
        if (x > cropArea.right + sensitivity ||
                x < cropArea.left - sensitivity ||
                y < cropArea.top - sensitivity ||
                y > cropArea.bottom + sensitivity) {
            return IMAGE;
        }
        float cw = cropArea.right - cropArea.left;
        float ch = cropArea.bottom - cropArea.top;
        float min = Math.min(sensitivity, Math.min(cw, ch) / 3);
        int xType;
        int yType;
        if (x <= cropArea.left + sensitivity) {
            xType = CROP_BOX_LEFT;
        } else if (x >= cropArea.right - min) {
            xType = CROP_BOX_RIGHT;
        } else {
            xType = CROP_BOX_CENTER;
        }
        if (y <= cropArea.top + min) {
            yType = CROP_BOX_TOP;
        } else if (y >= cropArea.bottom - min) {
            yType = CROP_BOX_BOTTOM;
        } else {
            yType = CROP_BOX_CENTER;
        }
        return xType | yType;
    }

    private void applyTranslate(float dx, float dy) {
        matrix.getValues(matrixValues);
        float moveX = 0;
        float moveY = 0;
        if (paddingStart == 0) {
            float x = matrixValues[2] + dx;
            float w = width - bitmap.getWidth() * baseScale * tScale;
            if (x > 0) {
                moveX = -matrixValues[2];
            } else if (x < w) {
                moveX = w - matrixValues[2];
            } else {
                moveX = dx;
            }
            if (Math.abs(moveX) < 1) {
                moveX = 0;
            }
        }
        if (paddingTop == 0) {
            float y = matrixValues[5] + dy;
            float h = height - bitmap.getHeight() * baseScale * tScale;
            if (y > 0) {
                moveY = -matrixValues[5];
            } else if (y < h) {
                moveY = h - matrixValues[5];
            } else {
                moveY = dy;
            }
            if (Math.abs(moveY) < 1) {
                moveY = 0;
            }
        }
        matrix.postTranslate(moveX, moveY);
    }

    private void scaleChanged() {
        float realScale = baseScale * tScale;
        paddingStart = Math.max((width - bitmap.getWidth() * realScale) / 2, 0);
        paddingTop = Math.max((height - bitmap.getHeight() * realScale) / 2, 0);
        displayArea.set(paddingStart, paddingTop, width - paddingStart, height - paddingTop);
        if (paddingStart != 0) {
            matrix.getValues(matrixValues);
            matrixValues[2] = paddingStart;
            matrix.setValues(matrixValues);
        }
        if (paddingTop != 0) {
            matrix.getValues(matrixValues);
            matrixValues[5] = paddingTop;
            matrix.setValues(matrixValues);
        }
        cropArea.set(Math.max(cropArea.left, displayArea.left),
                Math.max(cropArea.top, displayArea.top),
                Math.min(cropArea.right, displayArea.right),
                Math.min(cropArea.bottom, displayArea.bottom));
        applyTranslate(0, 0);
        dragCropBox(0, 0, CORRECTION);
    }

    /*public method*/
    public void setBitmap(Bitmap bitmap) {
        synchronized (this) {
            if (this.bitmap != null) {
                this.bitmap.recycle();
                this.bitmap = null;
                System.gc();
            }
            this.bitmap = bitmap;
        }
        if (width == 0 || height == 0) {
            runOnLayout = this::setBaseScale;
            return;
        }
        setBaseScale();
    }

    public void setLineWidth(int width) {
        lineWidth = width;
    }

    public void setSensitivity(int sensitivity) {
        this.sensitivity = sensitivity;
    }

    public void setLineLen(int lineLen) {
        this.lineLen = Math.min(lineLen, sensitivity);
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public void setShadowColor(int shadowColor) {
        this.shadowColor = shadowColor;
    }

    public void setAllLineShow(boolean allShow) {
        this.allShow = allShow;
    }

    public void setMaxScale(float maxScale) {
        this.maxScale = maxScale;
    }

    public void setMinSize(float minSize) {
        this.minSize = minSize;
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Bitmap crop() {
        Bitmap result = Bitmap.createBitmap((int) (cropArea.right - cropArea.left),
                (int) (cropArea.bottom - cropArea.top), bitmap.getConfig());
        Canvas canvas = new Canvas(result);
        float[] value = new float[9];
        matrix.getValues(value);
        Matrix matrixS = new Matrix();
        matrixS.setValues(value);
        matrixS.postTranslate(-cropArea.left, -cropArea.top);
        canvas.drawBitmap(bitmap, matrixS, new Paint());
        return result;
    }
}
