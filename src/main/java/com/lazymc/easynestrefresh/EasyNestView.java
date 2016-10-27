package com.lazymc.easynestrefresh;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huacangliang on 2016/4/18.
 */
public class EasyNestView extends FrameLayout {

    EasyNestRefreshView refreshView = null;
    EasyNestScrollView nestScrollView = null;
    List<EasyNestScrollCallback> callbacks = new ArrayList<>();

    public EasyNestView(Context context) {
        super(context);
    }

    public EasyNestView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EasyNestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EasyNestView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onViewAdded(View child) {
        if (child instanceof EasyNestRefreshView) {
            refreshView = (EasyNestRefreshView) child;
        } else if (child instanceof EasyNestScrollView) {
            nestScrollView = (EasyNestScrollView) child;
        } else if (child instanceof EasyNestScrollCallback) {
            callbacks.add((EasyNestScrollCallback) child);
        }
        init();
        super.onViewAdded(child);
    }

    private void init() {

        if (refreshView != null && nestScrollView != null) {
            refreshView.setRefreshListener(nestScrollView);
            nestScrollView.setScrollCallBack(refreshView);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
