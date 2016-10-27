package com.lazymc.easynestrefresh;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by LongYu on 2016/5/21.
 */
public class NoRequestImageView extends ImageView {
    private boolean init;

    public NoRequestImageView(Context context) {
        super(context);
    }

    public NoRequestImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoRequestImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public NoRequestImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void requestLayout() {
        if (init)
            return;
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
