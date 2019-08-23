package com.verzqli.blurview.thread;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * <pre>
 *     author: XuPei
 *     time  : 2019/8/19
 *     desc  :
 * </pre>
 */
public class TouchImageView extends android.support.v7.widget.AppCompatImageView {
    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    float moveX;
    float moveY;
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                moveX = event.getX();
                moveY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                setTranslationX(getX() + (event.getX() - moveX));
                setTranslationY(getY() + (event.getY() - moveY));
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }

        return true;
    }
}
