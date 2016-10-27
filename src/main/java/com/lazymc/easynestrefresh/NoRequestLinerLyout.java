package com.lazymc.easynestrefresh;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by huacangliang on 2016/5/21.
 */
public class NoRequestLinerLyout extends LinearLayout {
    private boolean init;

    public NoRequestLinerLyout(Context context) {
        super(context);
    }

    public NoRequestLinerLyout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoRequestLinerLyout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NoRequestLinerLyout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void requestLayout() {
        if (init)
            return;
        init=true;
        super.requestLayout();
    }

    @Override
    public void offsetTopAndBottom(int offset) {
        super.offsetTopAndBottom(offset);
    }

    public void setInit(boolean pInit) {
        init = pInit;
    }
}
