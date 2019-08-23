package com.verzqli.blurview.handler;


import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.util.Printer;

import java.util.concurrent.atomic.AtomicInteger;

public class MqqHandler implements IMqqMessageCallback {
    private static boolean isEnableInited;
    private static boolean isMqqHandlerEnable;
    public static AtomicInteger sInjectCount = new AtomicInteger(0);
    public static MqqRegulatorCallback sRegulatorCallback;
    boolean beInjected;
    boolean beRegulatoring;
    Callback mCallback;
    final Looper mLooper;
    private NativeHandler mNativeHandler;
    MessageQueue mQueue;
    MqqMessageQueue mSubQueue;

    private static class NativeHandler extends Handler {
        MqqHandler mParentHandler;

        public NativeHandler(Looper looper, Callback callback) {
            super(looper, callback);
        }

        public void dispatchMessage(Message msg) {
            this.mParentHandler.dispatchMessage(msg);
        }
    }

    public static boolean isMqqHandlerEnable() {
        return true;
    }

    public static void setMqqHandlerEnable(boolean enable) {
    }

    public MqqHandler() {
        this(Looper.myLooper(), null);
    }

    public MqqHandler(Callback callback) {
        this(Looper.myLooper(), callback);
    }

    public MqqHandler(Looper looper) {
        this(looper, null);
    }

    public MqqHandler(Looper looper, Callback callback) {
        this(looper, callback, false);
    }

    public MqqHandler(Looper looper, Callback callback, boolean highPriority) {
        this.beInjected = false;
        this.beRegulatoring = false;
        this.mLooper = looper;
        if (this.mLooper == null) {
            throw new RuntimeException("Can't create handler inside thread that has not called Looper.prepare()");
        }
        this.mCallback = callback;
        try {
            this.mQueue = looper.getQueue();
            if (this.mQueue == null || this.mLooper != Looper.getMainLooper() || !isMqqHandlerEnable() || highPriority) {
                if (!(this.mLooper == Looper.getMainLooper() || sRegulatorCallback == null)) {
                    this.beRegulatoring = sRegulatorCallback.regulatorThread(looper.getThread());
                }
                this.mNativeHandler = new NativeHandler(looper, callback);
                this.mNativeHandler.mParentHandler = this;
            }
            this.beInjected = true;
            sInjectCount.incrementAndGet();
            this.mSubQueue = MqqMessageQueue.getSubMainThreadQueue();
            this.mNativeHandler = new NativeHandler(looper, callback);
            this.mNativeHandler.mParentHandler = this;
        } catch (Throwable th) {
            this.beInjected = false;
        }
    }

    public final Looper getLooper() {
        return this.mLooper;
    }

    public void handleMessage(Message msg) {
    }

    public void dispatchMessage(Message msg) {
        if (this.beRegulatoring) {
            sRegulatorCallback.checkInRegulatorMsg();
        }
        if (msg.getCallback() != null) {
            handleCallback(msg);
        } else if (this.mCallback == null || !this.mCallback.handleMessage(msg)) {
            handleMessage(msg);
        }
    }

    public String getMessageName(Message message) {
        if (message.getCallback() != null) {
            return message.getCallback().getClass().getName();
        }
        return "0x" + Integer.toHexString(message.what);
    }

    public final Message obtainMessage() {
        return Message.obtain(this.mNativeHandler);
    }

    public Message obtainMessage(int what, Runnable runnable) {
        Message message = Message.obtain(this.mNativeHandler, runnable);
        message.what = what;
        return message;
    }

    public final Message obtainMessage(int what) {
        return Message.obtain(this.mNativeHandler, what);
    }

    public final Message obtainMessage(int what, Object obj) {
        return Message.obtain(this.mNativeHandler, what, obj);
    }

    public final Message obtainMessage(int what, int arg1, int arg2) {
        return Message.obtain(this.mNativeHandler, what, arg1, arg2);
    }

    public final Message obtainMessage(int what, int arg1, int arg2, Object obj) {
        return Message.obtain(this.mNativeHandler, what, arg1, arg2, obj);
    }

    public final boolean post(Runnable r) {
        return sendMessageDelayed(getPostMessage(r), 0);
    }

    public final boolean postAtTime(Runnable r, long uptimeMillis) {
        return sendMessageAtTime(getPostMessage(r), uptimeMillis);
    }

    public final boolean postAtTime(Runnable r, Object token, long uptimeMillis) {
        return sendMessageAtTime(getPostMessage(r, token), uptimeMillis);
    }

    public final boolean postDelayed(Runnable r, long delayMillis) {
        return sendMessageDelayed(getPostMessage(r), delayMillis);
    }

    public final boolean postAtFrontOfQueue(Runnable r) {
        return sendMessageAtFrontOfQueue(getPostMessage(r));
    }

    public final void removeCallbacks(Runnable r) {
        if (this.beInjected) {
            this.mSubQueue.removeMessages(this, r, null);
        } else {
            this.mNativeHandler.removeCallbacks(r);
        }
    }

    public final void removeCallbacks(Runnable r, Object token) {
        if (this.beInjected) {
            this.mSubQueue.removeMessages(this, r, token);
        } else {
            this.mNativeHandler.removeCallbacks(r, token);
        }
    }

    public final boolean sendMessage(Message msg) {
        return sendMessageDelayed(msg, 0);
    }

    public final boolean sendEmptyMessage(int what) {
        return sendEmptyMessageDelayed(what, 0);
    }

    public final boolean sendEmptyMessageDelayed(int what, long delayMillis) {
        Message msg = Message.obtain();
        msg.what = what;
        return sendMessageDelayed(msg, delayMillis);
    }

    public final boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
        Message msg = Message.obtain();
        msg.what = what;
        return sendMessageAtTime(msg, uptimeMillis);
    }

    public final boolean sendMessageDelayed(Message msg, long delayMillis) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        return sendMessageAtTime(msg, SystemClock.uptimeMillis() + delayMillis);
    }

    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        if (!this.beInjected) {
            return this.mNativeHandler.sendMessageAtTime(msg, uptimeMillis);
        }
        MqqMessageQueue queue = this.mSubQueue;
        if (queue != null) {
            MqqMessage m = MqqMessage.obtain(msg);
            m.target = this;
            return queue.enqueueMessage(m, uptimeMillis);
        }
        throw new RuntimeException(this + " sendMessageAtTime() called with no mSubQueue");
    }

    public final boolean sendMessageAtFrontOfQueue(Message msg) {
        if (!this.beInjected) {
            return this.mNativeHandler.sendMessageAtFrontOfQueue(msg);
        }
        MqqMessageQueue queue = this.mSubQueue;
        if (queue != null) {
            MqqMessage m = MqqMessage.obtain(msg);
            m.target = this;
            return queue.enqueueMessage(m, 0);
        }
        throw new RuntimeException(this + " sendMessageAtTime() called with no mSubQueue");
    }

    public final void removeMessages(int what) {
        if (this.beInjected) {
            this.mSubQueue.removeMessages(this, what, null, true);
        } else {
            this.mNativeHandler.removeMessages(what);
        }
    }

    public final void removeMessages(int what, Object object) {
        if (this.beInjected) {
            this.mSubQueue.removeMessages(this, what, object, true);
        } else {
            this.mNativeHandler.removeMessages(what, object);
        }
    }

    public void removeCallbacksAndMessages(Object token) {
        if (this.beInjected) {
            this.mSubQueue.removeCallbacksAndMessages(this, token);
        } else {
            this.mNativeHandler.removeCallbacksAndMessages(token);
        }
    }

    public final boolean hasMessages(int what) {
        return this.beInjected ? this.mSubQueue.removeMessages(this, what, null, false) : this.mNativeHandler.hasMessages(what);
    }

    public final boolean hasMessages(int what, Object object) {
        return this.beInjected ? this.mSubQueue.removeMessages(this, what, object, false) : this.mNativeHandler.hasMessages(what, object);
    }

    public final void dump(Printer pw, String prefix) {
        if (!this.beInjected) {
            this.mNativeHandler.dump(pw, prefix);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(128);
        sb.append("Handler (");
        sb.append(getClass().getName());
        sb.append(") {");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append("}");
        return sb.toString();
    }

    private final Message getPostMessage(Runnable r) {
        return Message.obtain(this.mNativeHandler, r);
    }

    private final Message getPostMessage(Runnable r, Object token) {
        Message m = Message.obtain(this.mNativeHandler, r);
        m.obj = token;
        return m;
    }

    private final void handleCallback(Message message) {
        message.getCallback().run();
    }
}