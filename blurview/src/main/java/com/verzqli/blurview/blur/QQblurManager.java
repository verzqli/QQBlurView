package com.verzqli.blurview.blur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.verzqli.blurview.stackblur.StackBlurManager;
import com.verzqli.blurview.thread.ThreadManagerV2;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


/**
 * <pre>
 *     author: XuPei
 *     time  : 2019/8/15
 *     desc  :
 * </pre>
 */
public class QQblurManager {
    private static HandlerThread mHandlerThread = ThreadManagerV2.newFreeHandlerThread("QQBlur", -8);
    private static Field mField;
    private Context mContext;
    private Canvas mCanvas;
    private Paint mPaint;
    private RectF mRectF = new RectF();
    //这个drawable是为不模糊的版本做准备的，如果不进行高斯模糊，就显示这个
    private Drawable mDrawable = new ColorDrawable(Color.parseColor("#DAFAFAFC"));
    private Handler mHandler;
    private View mTargetView;
    private List<View> mViewList = new ArrayList();
    private volatile boolean isDetachToWindow = true;
    public volatile Bitmap mBitmap;

    //模糊半径
    public int mRadius = 6;
    private float mScale = 8.0f;
    public static int mBlurType = 0;

    public volatile View mBlurView;
    public boolean isDrawing;

    private float c = 1.0f;
    private float b = 1.0f;

    private int mBlurBitmapEraseColor = 0;

    private boolean isDrawCanvas;
    //线程并发数
    private int mThreadCount = 4;
    public long mDrawCount;
    public long mDrawTime;
    private float mRoundRectRadiusX;
    private float mRoundRectRadiusY;
    private long mBlurBitmapWidth;
    private long mBlurBitmapHeight;
    private long mBlurBitmapByteCount;

    /**
     * 这两个值也暂未知道具体是做什么用的
     * 应该和下面一样为调试提供数据，并不参与逻辑
     */
    private long j;
    private long k;

    /**
     * 这四个数值是QQ计算主线程采样和后台现场处理
     * 打印log工开发者调试的数值，与代码基本无关联
     */
    private long mPreViewCount;
    private long mPreViewTime;
    public long mBlurThreadCount;
    public long mBlurThreadTime;

    static {
        mHandlerThread.start();
    }

    private void preDrawCanvas() {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        if (this.mTargetView != null && this.mBlurView != null && this.mBlurView.getWidth() > 0 && this.mBlurView.getHeight() > 0) {
            Bitmap createBitmap;
            int scaleWidth = QQblurManager.ceil((float) this.mBlurView.getWidth(), this.mScale);
            int scaleHeight = QQblurManager.ceil((float) this.mBlurView.getHeight(), this.mScale);
            int a3 = QQblurManager.fixBy16(scaleWidth);
            int a4 = QQblurManager.fixBy16(scaleHeight);
            this.c = ((float) scaleHeight) / ((float) a4);
            this.b = ((float) scaleWidth) / ((float) a3);
            float f = this.mScale * this.b;
            float f2 = this.mScale * this.c;
            try {
                createBitmap = Bitmap.createBitmap(a3, a4, Config.ARGB_8888);
            } catch (Throwable e) {
                Log.e("QQBlur", "prepareBlurBitmap: ", e);
                createBitmap = null;
            }
            if (createBitmap != null) {
                this.mBlurBitmapWidth = (long) createBitmap.getWidth();
                this.mBlurBitmapHeight = (long) createBitmap.getHeight();
                if (VERSION.SDK_INT >= 19) {
                    mBlurBitmapByteCount = (long) createBitmap.getAllocationByteCount();
                } else {
                    mBlurBitmapByteCount = (long) createBitmap.getByteCount();
                }
                createBitmap.eraseColor(mBlurBitmapEraseColor);
                this.mCanvas.setBitmap(createBitmap);
                int[] iArr = new int[2];
                this.mBlurView.getLocationInWindow(iArr);
                int[] iArr2 = new int[2];
                this.mTargetView.getLocationInWindow(iArr2);
                this.mCanvas.save();
                this.mCanvas.translate(((float) (-(iArr[0] - iArr2[0]))) / f, ((float) (-(iArr[1] - iArr2[1]))) / f2);
                this.mCanvas.scale(1.0f / f, 1.0f / f2);
                StackBlurManager stackBlurManager = new StackBlurManager(createBitmap);
                stackBlurManager.setDbg(true);
                stackBlurManager.setExecutorThreads(stackBlurManager.getExecutorThreads());
                this.isDrawCanvas = true;
                if (VERSION.SDK_INT <= 27 || this.mBlurView.getContext().getApplicationInfo().targetSdkVersion <= 27) {
                    Rect clipBounds = this.mCanvas.getClipBounds();
                    clipBounds.inset(-createBitmap.getWidth(), -createBitmap.getHeight());
                    if (this.mCanvas.clipRect(clipBounds, Op.REPLACE)) {
                        this.mTargetView.draw(this.mCanvas);
                    } else {
                        Log.e("QQBlur", "prepareBlurBitmap: canvas clip rect empty. Cannot draw!!!");
                    }
                } else {
                    this.mTargetView.draw(this.mCanvas);
                }
                this.mCanvas.restore();
                clearViewVisible();
                Log.i("高斯模糊", "创建bitmap" + createBitmap);
                this.isDrawCanvas = false;
                this.mHandler.post(new QQBlur(this, stackBlurManager));
            } else {
                return;
            }
        }
        long elapsedRealtime2 = SystemClock.elapsedRealtime();
        this.mPreViewCount++;
        this.mPreViewTime = (elapsedRealtime2 - elapsedRealtime) + this.mPreViewTime;
    }

    public QQblurManager setTargetView(View view) {
        this.mTargetView = view;
        return this;
    }

    public QQblurManager setBlurView(View view) {
        this.mBlurView = view;
        return this;
    }

    public QQblurManager onCreate() {
        Log.d("QQBlur", "onCreate() called");
        this.mContext = this.mBlurView.getContext();
        this.mCanvas = new Canvas();
        this.mHandler = new Handler(mHandlerThread.getLooper());
        this.isDrawing = true;
        f();
        return this;
    }

    private void f() {
        if (this.mContext != null && this.mTargetView != null && this.mBlurView != null) {
        }
    }

    public void onDraw(View view, Canvas canvas) {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        Bitmap bitmap = this.mBitmap;
        if (bitmap != null) {
            canvas.save();
            canvas.scale((((float) view.getWidth()) * 1.0f) / ((float) bitmap.getWidth()), (((float) view.getHeight()) * 1.0f) / ((float) bitmap.getHeight()));
            if (this.mPaint == null) {
                this.mPaint = new Paint(1);
            }
            this.mPaint.setShader(new BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP));
            this.mRectF.set(0.0f, 0.0f, (float) bitmap.getWidth(), (float) bitmap.getHeight());
            canvas.drawRoundRect(this.mRectF, this.mRoundRectRadiusX, this.mRoundRectRadiusY, this.mPaint);
            if (this.mDrawable != null) {
                this.mDrawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                this.mDrawable.draw(canvas);
            }
            Log.i("高斯模糊", "绘制bitmap" + this.mBitmap);
            canvas.restore();
        }
        long elapsedRealtime2 = SystemClock.elapsedRealtime();
        this.mDrawCount++;
        this.mDrawTime = (elapsedRealtime2 - elapsedRealtime) + this.mDrawTime;
        Log.i("输出内容", "" + logOutData());
    }


    public void onResume() {
        this.isDetachToWindow = false;
    }

    public void onPause() {
        this.isDetachToWindow = true;
    }


    public void onDestroy() {
        Log.d("QQBlur", "onDestroy() called");
        if (this.isDrawing) {
            this.isDrawing = false;
            this.mHandler.removeCallbacksAndMessages(null);
            this.mHandler = null;
            this.mTargetView = null;
            this.mBlurView = null;
            this.mCanvas.setBitmap(null);
            this.mCanvas = null;
            this.mPaint = null;
            this.mContext = null;
        }
    }

    public boolean onPreDraw() {
        boolean isDirty = false;
        if (this.mTargetView != null) {
            isDirty = this.mTargetView.isDirty();
        }
        View view = this.mBlurView;
        if (!this.isDetachToWindow && isDirty && view != null && view.getVisibility() == View.VISIBLE) {
            preDrawCanvas();
            view.invalidate();
        }
        return true;
    }


    public void setScale(float i) {
        this.mScale = i;
    }

    public void setRadius(int i) {
        this.mRadius = i;
    }


    public void setBlurType(int i) {
        mBlurType = i;
    }

    public void onPolicyChange(int i, int i2) {
        Log.d("QQBlur", "onPolicyChange() called with: from = [" + i + "], to = [" + i2 + "]");
        this.mPreViewCount = 0;
        this.mPreViewTime = 0;
        this.mBlurThreadCount = 0;
        this.mBlurThreadTime = 0;
    }


    public String logOutData() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("方案=").append(selectBlurType(mBlurType)).append(",");
        stringBuilder.append("缩放倍数=").append(this.mScale).append(",");
        stringBuilder.append("模糊半径=").append(this.mRadius).append(",");
        stringBuilder.append("尺寸=" + this.mBlurBitmapWidth + "x" + this.mBlurBitmapHeight).append(",");
        stringBuilder.append("空间=" + (mBlurBitmapByteCount / 1000) + "KB").append(",");
        stringBuilder.append("并发数=" + this.mThreadCount).append(",");
        stringBuilder.append("主线程采样=[" + String.format("%.2f", new Object[]{Float.valueOf(((float) this.mPreViewTime) / ((float) this.mPreViewCount))}) + "]ms").append(",");
        stringBuilder.append("后台线程处理=[" + String.format("%.2f", new Object[]{Float.valueOf(((float) this.mBlurThreadTime) / ((float) this.mBlurThreadCount))}) + "]ms");
        return stringBuilder.toString();
    }

    private CharSequence selectBlurType(int i) {
        switch (i) {
            case 1:
                return "StackBlur.Native";
            case 2:
                return "StackBlur.RS";
            case 3:
                return "GaussBlur.RS";
            default:
                return "StackBlur.Java";
        }
    }

    public void setEraseColor(int color) {
        this.mBlurBitmapEraseColor = color;
    }


    public boolean isDrawCanvas() {
        return this.isDrawCanvas;
    }

    private void clearViewVisible() {
        for (View view : this.mViewList) {
            if (view != null) {
                setViewVisible(view, 0);
            }
        }
    }

    private static int ceil(float f, float f2) {
        return (int) Math.ceil((double) (f / f2));
    }

    public static int fixBy16(int i) {
        return i % 16 == 0 ? i : (i - (i % 16)) + 16;
    }

    /**
     * 下面这个方法每台看明白具体用意，就没改名
     */
    public void clear() {
        this.mViewList.clear();
        listView(this.mTargetView.getRootView(), this.mViewList);
    }


    private void listView(View view, List<View> list) {
        if (view != null && view.getVisibility() == View.VISIBLE) {
            list.add(view);
            setViewVisible(view, 4);
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                int childCount = viewGroup.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    listView(viewGroup.getChildAt(i), (List) list);
                }
            }
        }
    }

    /**
     * 这里是采用反射的方式手动为view设置VISIBLE和INVISIBLE
     * 具体为什么这么做原因未知
     *
     * @param view
     * @param i
     */
    private void setViewVisible(View view, int i) {
        long uptimeMillis = SystemClock.uptimeMillis();
        try {
            if (mField == null) {
                mField = View.class.getDeclaredField("mViewFlags");
                mField.setAccessible(true);
            }
            mField.setInt(view, (mField.getInt(view) & -13) | (i & 12));
        } catch (Throwable e) {
            Log.e("QQBlur", "setViewInvisible: ");
        }
        long uptimeMillis2 = SystemClock.uptimeMillis() - uptimeMillis;
        if (this.j >= 100000) {
            this.j = 0;
            this.k = 0;
        }
        this.j++;
        this.k = uptimeMillis2 + this.k;
        if (this.j % 2000 != 0) {
        }
    }
}
