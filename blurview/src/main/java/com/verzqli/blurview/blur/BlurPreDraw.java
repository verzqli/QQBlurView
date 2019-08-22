package com.verzqli.blurview.blur;

import android.view.ViewTreeObserver;

/**
 * <pre>
 *     author: XuPei
 *     time  : 2019/8/15
 *     desc  :
 * </pre>
 */
public class BlurPreDraw implements ViewTreeObserver.OnPreDrawListener {
    final  QQBlurView blurView;
    public BlurPreDraw(QQBlurView qQBlurView) {
        this.blurView = qQBlurView;
    }

    public boolean onPreDraw() {
        if (this.blurView.mManager!=null) {
             return this.blurView.mManager.onPreDraw();
        }
        return true;
    }
}