package com.verzqli.blurview.handler;


import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class MqqInterceptHandler extends Handler implements IMqqMessageCallback {
    Handler mParentHandler = null;
    MqqMessageQueue mSubUIQueue;

    /**
     * getQueue() 方法最少api为23
     *
     * @param handler
     * @return
     */
    public static Handler createMqqHandler(Handler handler) {
        if (handler.getLooper() != Looper.getMainLooper() || (handler instanceof MqqInterceptHandler)) {
            return null;
        }
        try {
            if (Looper.getMainLooper().getQueue() != null) {
                return new MqqInterceptHandler(handler);
            }
            return null;
        } catch (Throwable th) {
            return null;
        }
    }

    private MqqInterceptHandler(Handler handler) {
        super(Looper.getMainLooper());
        this.mParentHandler = handler;
        this.mSubUIQueue = MqqMessageQueue.getSubMainThreadQueue();
    }

    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        MqqMessage m = MqqMessage.obtain(msg);
        m.target = this;
        boolean rs = this.mSubUIQueue.enqueueMessage(m, uptimeMillis);
        if (rs) {
            return rs;
        }
        return this.mParentHandler.sendMessageAtTime(msg, uptimeMillis);
    }

    public void dispatchMessage(Message msg) {
        this.mParentHandler.dispatchMessage(msg);
    }
}