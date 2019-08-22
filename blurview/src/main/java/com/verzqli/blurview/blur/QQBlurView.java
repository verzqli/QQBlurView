package com.verzqli.blurview.blur;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;


/**
 * <pre>
 *     author: XuPei
 *     time  : 2019/8/15
 *     desc  :
 * </pre>
 */
public class QQBlurView extends View {
    private Drawable a;

    private BlurPreDraw mBlurPreDraw = new BlurPreDraw(this);

    public QQblurManager mManager = new QQblurManager();

    private boolean mEnableBlur = true;

    public QQBlurView(Context context) {
        super(context);
        init();
    }

    public QQBlurView(Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    Paint paint;

    public QQBlurView(Context context, @Nullable AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();

    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(100);
    }

    protected void onDraw(Canvas canvas) {
        if (!isDrawCanvas()) {
            if (this.mEnableBlur) {
                setBackgroundDrawable(null);
                this.mManager.onDraw((View) this, canvas);
                super.onDraw(canvas);
                return;
            }
            setBackgroundDrawable(this.a);
            super.onDraw(canvas);
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mManager != null) {
            onAttached();
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mManager != null) {
            onDetached();
        }
    }

    public void onAttached() {
        Log.d("QQBlurView", "onResume() called");
        this.mManager.onResume();
    }

    public void onDetached() {
        Log.d("QQBlurView", "onPause() called");

        this.mManager.onPause();
    }

    public void onDestroy() {
        getViewTreeObserver().removeOnPreDrawListener(this.mBlurPreDraw);
        this.mManager.onDestroy();
    }

    public void setTargetView(View view) {
        this.mManager.setTargetView(view);
    }

    public void setBlurView(View view) {
        this.mManager.setBlurView(view);
    }

    public void onCreate() {
        getViewTreeObserver().addOnPreDrawListener(this.mBlurPreDraw);
        this.mManager.onCreate();
    }

    public void setBlurScale(float f) {
        this.mManager.setScale(f);
    }

    public void setBlurRadius(int i) {
        this.mManager.setRadius(i);
    }

    public void draw(Canvas canvas) {
        if (isDrawCanvas()) {
            this.mManager.clear();
        } else {
            super.draw(canvas);
        }
    }

    protected void dispatchDraw(Canvas canvas) {
        if (!isDrawCanvas()) {
            super.dispatchDraw(canvas);
        }
    }

    public void setBlurType(int i) {
        this.mManager.setBlurType(i);
    }

    public void setEnableBlur(boolean z) {
        this.mEnableBlur = z;
    }

    public void setDisableBlurDrawableRes(int i) {
        this.a = getResources().getDrawable(i);
    }

    public void setEraseColor(int i) {
        this.mManager.setEraseColor(i);
    }

    public boolean isDrawCanvas() {
        return this.mManager.isDrawCanvas();
    }
}