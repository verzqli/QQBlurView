package com.verzqli.blurview.blur;

import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import com.verzqli.blurview.stackblur.StackBlurManager;


/**
 * <pre>
 *     author: XuPei
 *     time  : 2019/8/15
 *     desc  :
 * </pre>
 */
public class QQBlur implements Runnable {
    private int a = -1;
    final StackBlurManager mStackBlurManager;
    final QQblurManager mQQblurManager;

    public QQBlur(QQblurManager QQblurManager, StackBlurManager stackBlurManager) {
        this.mQQblurManager = QQblurManager;
        this.mStackBlurManager = stackBlurManager;
    }

    public void run() {
        if (!this.mQQblurManager.isDrawCanvas()) {
            long elapsedRealtime = SystemClock.elapsedRealtime();
            if (!(this.a == -1 || this.a == QQblurManager.mBlurType)) {
                this.mQQblurManager.onPolicyChange(this.a, QQblurManager.mBlurType);
            }
            this.a = QQblurManager.mBlurType;
            int i = QQblurManager.mBlurType;
            Bitmap process = this.mStackBlurManager.process(this.mQQblurManager.mRadius);
            if (process != null) {
                this.mQQblurManager.mBitmap = process;
            } else {
                Log.e("QQBlur", "run: outBitmap is null. OOM ?");
            }
            long elapsedRealtime2 = SystemClock.elapsedRealtime();
            this.mQQblurManager.mBlurThreadCount++;
            this.mQQblurManager.mBlurThreadTime = (elapsedRealtime2 - elapsedRealtime) + this.mQQblurManager.mBlurThreadTime;
            View blurView = this.mQQblurManager.mBlurView;

            if (blurView != null && this.mQQblurManager.isDrawing) {
                blurView.postInvalidate();
            }
        }
    }
}