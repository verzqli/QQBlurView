package com.verzqli.blurview.stackblur;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

/**
 * <pre>
 *     author: XuPei
 *     time  : 2019/8/23
 *     desc  :
 * </pre>
 */
class SdkRSBlurProcess implements BlurProcess {
    private final RenderScript _rs;
    private final Context context;

    public SdkRSBlurProcess(Context context) {
        this.context = context.getApplicationContext();
        _rs = RenderScript.create(this.context);
    }

    public Bitmap blur(Bitmap original, float radius) {
        Bitmap blurred = original.copy(Bitmap.Config.ARGB_8888, true);
        Allocation inAllocation = Allocation.createFromBitmap(this._rs, blurred, Allocation.MipmapControl.MIPMAP_NONE, 1);
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(this._rs, Element.U8_4(this._rs));
        blurScript.setInput(inAllocation);
        blurScript.setRadius(radius);
        Allocation outAllocation = Allocation.createTyped(this._rs, inAllocation.getType());
        blurScript.forEach(outAllocation);
        outAllocation.copyTo(blurred);
        return blurred;
    }

    public String toString() {
        return getClass().getSimpleName();
    }
}