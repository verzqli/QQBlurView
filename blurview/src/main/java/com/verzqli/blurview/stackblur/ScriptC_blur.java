package com.verzqli.blurview.stackblur;

import android.content.res.Resources;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.BaseObj;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.FieldPacker;
import android.support.v8.renderscript.RSRuntimeException;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptC;

/**
 * <pre>
 *     author: XuPei
 *     time  : 2019/8/15
 *     desc  :
 * </pre>
 */

public class ScriptC_blur extends ScriptC {
    private static final String __rs_resource_name = "blur";
    private static final int mExportForEachIdx_blur_h = 2;
    private static final int mExportForEachIdx_blur_v = 1;
    private static final int mExportVarIdx_gIn = 0;
    private static final int mExportVarIdx_height = 2;
    private static final int mExportVarIdx_radius = 3;
    private static final int mExportVarIdx_width = 1;
    private Element __ALLOCATION;
    private Element __U32;
    private FieldPacker __rs_fp_ALLOCATION;
    private FieldPacker __rs_fp_U32;
    private Allocation mExportVar_gIn;
    private long mExportVar_height;
    private long mExportVar_radius;
    private long mExportVar_width;

    public ScriptC_blur(RenderScript rs) {
        this(rs, rs.getApplicationContext().getResources(), rs.getApplicationContext().getResources().getIdentifier("blur", "raw", rs.getApplicationContext().getPackageName()));
    }

    public ScriptC_blur(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        this.__ALLOCATION = Element.ALLOCATION(rs);
        this.__U32 = Element.U32(rs);
    }

    public synchronized void set_gIn(Allocation v) {
        setVar(0, (BaseObj) v);
        this.mExportVar_gIn = v;
    }

    public Allocation get_gIn() {
        return this.mExportVar_gIn;
    }

    public FieldID getFieldID_gIn() {
        return createFieldID(0, null);
    }

    public synchronized void set_width(long v) {
        if (this.__rs_fp_U32 != null) {
            this.__rs_fp_U32.reset();
        } else {
            this.__rs_fp_U32 = new FieldPacker(4);
        }
        this.__rs_fp_U32.addU32(v);
        setVar(1, this.__rs_fp_U32);
        this.mExportVar_width = v;
    }

    public long get_width() {
        return this.mExportVar_width;
    }

    public FieldID getFieldID_width() {
        return createFieldID(1, null);
    }

    public synchronized void set_height(long v) {
        if (this.__rs_fp_U32 != null) {
            this.__rs_fp_U32.reset();
        } else {
            this.__rs_fp_U32 = new FieldPacker(4);
        }
        this.__rs_fp_U32.addU32(v);
        setVar(2, this.__rs_fp_U32);
        this.mExportVar_height = v;
    }

    public long get_height() {
        return this.mExportVar_height;
    }

    public FieldID getFieldID_height() {
        return createFieldID(2, null);
    }

    public synchronized void set_radius(long v) {
        if (this.__rs_fp_U32 != null) {
            this.__rs_fp_U32.reset();
        } else {
            this.__rs_fp_U32 = new FieldPacker(4);
        }
        this.__rs_fp_U32.addU32(v);
        setVar(3, this.__rs_fp_U32);
        this.mExportVar_radius = v;
    }

    public long get_radius() {
        return this.mExportVar_radius;
    }

    public FieldID getFieldID_radius() {
        return createFieldID(3, null);
    }

    public KernelID getKernelID_blur_v() {
        return createKernelID(1, 33, null, null);
    }

    public void forEach_blur_v(Allocation ain) {
        forEach_blur_v(ain, null);
    }

    public void forEach_blur_v(Allocation ain, LaunchOptions sc) {
        if (ain.getType().getElement().isCompatible(this.__U32)) {
            forEach(1, ain, null, null, sc);
            return;
        }
        throw new RSRuntimeException("Type mismatch with U32!");
    }

    public KernelID getKernelID_blur_h() {
        return createKernelID(2, 33, null, null);
    }

    public void forEach_blur_h(Allocation ain) {
        forEach_blur_h(ain, null);
    }

    public void forEach_blur_h(Allocation ain, LaunchOptions sc) {
        if (ain.getType().getElement().isCompatible(this.__U32)) {
            forEach(2, ain, null, null, sc);
            return;
        }
        throw new RSRuntimeException("Type mismatch with U32!");
    }
}