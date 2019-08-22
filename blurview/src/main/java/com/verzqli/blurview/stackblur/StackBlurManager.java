/**
 * StackBlur v1.0 for Android
 *
 * @Author: Enrique López Mañas <eenriquelopez@gmail.com>
 * http://www.lopez-manas.com
 * <p>
 * Author of the original algorithm: Mario Klingemann <mario.quasimondo.com>
 * <p>
 * This is a compromise between Gaussian Blur and Box blur
 * It creates much better looking blurs than Box Blur, but is
 * 7x faster than my Gaussian Blur implementation.
 * <p>
 * I called it Stack Blur because this describes best how this
 * filter works internally: it creates a kind of moving stack
 * of colors whilst scanning through the image. Thereby it
 * just has to add one new block of color to the right side
 * of the stack and remove the leftmost color. The remaining
 * colors on the topmost layer of the stack are either added on
 * or reduced by one, depending on if they are on the right or
 * on the left side of the stack.
 * @copyright: Enrique López Mañas
 * @license: Apache License 2.0
 */


package com.verzqli.blurview.stackblur;

import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class StackBlurManager {
    static int EXECUTOR_THREADS = Runtime.getRuntime().availableProcessors();
    static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(EXECUTOR_THREADS);
    private static final String TAG = "StackBlurManager";
    private static volatile boolean hasRS = true;
    private final BlurProcess _blurProcess;
    private final Bitmap _image;
    private Bitmap _result;
    private boolean mDbg = true;

    public StackBlurManager(Bitmap image) {
        this._image = image;
        this._blurProcess = new JavaBlurProcess();
    }

    public Bitmap process(int radius) {
        long start = SystemClock.uptimeMillis();
        this._result = this._blurProcess.blur(this._image, 8);
        Log.i("高斯模糊","模糊bitmap"+this._image);
        Log.i(TAG, "process: " + this._blurProcess + "=" + (SystemClock.uptimeMillis() - start) + " ms");
        return this._result;
    }

    public Bitmap returnBlurredImage() {
        return this._result;
    }

//	public void saveIntoFile(String path) {
//		try {
//			this._result.compress(CompressFormat.PNG, 90, new FileOutputStream(path));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

    public Bitmap getImage() {
        return this._image;
    }

    public Bitmap processNatively(int radius) {
        long start = SystemClock.uptimeMillis();
        NativeBlurProcess blur = new NativeBlurProcess();
        this._result = blur.blur(this._image, (float) radius);
        if (this.mDbg) {
            Log.i(TAG, "processNatively: " + blur + "=" + (SystemClock.uptimeMillis() - start) + " ms");
        }
        return this._result;
    }

//	public Bitmap processRenderScript(Context context, float radius, int blurResId) {
//		BlurProcess blurProcess;
//		long start = SystemClock.uptimeMillis();
//		if (hasRS) {
//			try {
//				blurProcess = new RSBlurProcess(context, blurResId);
//			} catch (RSRuntimeException e) {
//				blurProcess = new NativeBlurProcess();
//				hasRS = false;
//			}
//		} else {
//			blurProcess = new NativeBlurProcess();
//		}
//		this._result = blurProcess.blur(this._image, radius);
//		if (this.mDbg) {
//			Log.i(TAG, "processRenderScript: " + blurProcess + "=" + (SystemClock.uptimeMillis() - start) + " ms");
//		}
//		return this._result;
//	}

//	public Bitmap processSdkRenderScript(Context context, float radius) {
//		long start = SystemClock.uptimeMillis();
//		BlurProcess blurProcess = new SdkRSBlurProcess(context);
//		this._result = blurProcess.blur(this._image, radius);
//		if (this.mDbg) {
//			Log.i(TAG, "processSdkRenderScript: " + blurProcess + "=" + (SystemClock.uptimeMillis() - start) + " ms");
//		}
//		return this._result;
//	}

    public void setDbg(boolean debug) {
        this.mDbg = debug;
    }

    public void setExecutorThreads(int cores) {
        EXECUTOR_THREADS = cores;
    }

    public int getExecutorThreads() {
        return EXECUTOR_THREADS;
    }
}