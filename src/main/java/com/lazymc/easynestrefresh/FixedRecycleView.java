package com.lazymc.easynestrefresh;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by youyunkeji on 16/10/14.
 */

public class FixedRecycleView extends RecyclerView {

    public FixedRecycleView(Context context) {
        super(context);
    }

    public FixedRecycleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FixedRecycleView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public View focusSearch(int direction) {
        return super.focusSearch(direction);
    }

    /**
     * 因为重写了本类的滚动函数，所以要提供给外面调用的机会
     * @param dx
     * @param dy
     */
    public void smoothScrollByUser(int dx, int dy) {
            super.smoothScrollBy(dx, dy);
    }

    /**
     * 为解决嵌套viewpage的异常滚动，重写该函数
     * @param dx
     * @param dy
     */
    @Override
    public void smoothScrollBy(int dx, int dy) {
        //super.smoothScrollBy(dx, dy);
    }

    /**
     * 直接替换
     * @param recyclerView
     * @return
     */
    public static RecyclerView replaceRecycleView(RecyclerView recyclerView) {
        FixedRecycleView fixFocusRecycleView = new FixedRecycleView(recyclerView.getContext());
        ViewGroup viewGroup = (ViewGroup) recyclerView.getParent();
        viewGroup.removeView(recyclerView);
        fixFocusRecycleView.setLayoutParams(recyclerView.getLayoutParams());
        recyclerView = fixFocusRecycleView;
        viewGroup.addView(fixFocusRecycleView);
        return recyclerView;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        // check if scrolling dwon
        if (direction < 1) {
            boolean original = super.canScrollVertically(direction);
            if (!original || getChildAt(0) != null && getChildAt(0).getTop() >= 0) {
                return false;
            }else {
                return true;
            }
        }
        return super.canScrollVertically(direction);

    }
}
