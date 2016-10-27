package com.lazymc.easynestrefresh;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by huacangliang on 2016/5/31.
 * 可监听滚动状态的scrollview
 * 使用NestedScrollView有个bug,滑动的时候内容的z轴会向上,造成不可预估的问题,
 * 可使用原生scrollview代替,但是因为原生的被第三方修改过,影响体验
 */
public class LSScrollveiw extends NestedScrollView{
    private OnScrollChangeListener mOnScrollChangeListener;
    public LSScrollveiw(Context context) {
        super(context);
    }

    public LSScrollveiw(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LSScrollveiw(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChangeListener!=null){
            mOnScrollChangeListener.onScrollChange(null,l,t,oldl,oldt);
        }
    }

    public void setOnScrollChangeListener(OnScrollChangeListener onScrollChangeListener) {
        mOnScrollChangeListener = onScrollChangeListener;
        setNestedScrollingEnabled(false);
    }

    public interface OnScrollChangeListener {
        /**
         * Called when the scroll position of a view changes.
         *
         * @param v The view whose scroll position has changed.
         * @param scrollX Current horizontal scroll origin.
         * @param scrollY Current vertical scroll origin.
         * @param oldScrollX Previous horizontal scroll origin.
         * @param oldScrollY Previous vertical scroll origin.
         */
        void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY);
    }
}
