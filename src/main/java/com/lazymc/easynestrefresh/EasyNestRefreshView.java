package com.lazymc.easynestrefresh;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by longyu on 2016/4/18.
 * 刷新控件，该控件主要用于包裹刷新指示器容器，要跟{@link EasyNestScrollView} 搭配使用
 */
public class EasyNestRefreshView extends RelativeLayout implements EasyNestScrollCallback {

    private static final String TAG = "EasyNestRefreshView";
    private List<EasyOnNestReFreshListener> onNestFreshListenerList = new ArrayList<>();

    private RefreshListener refreshListener;

    private boolean refreshing;

    private int scrollY;

    private int needRefreshHeight = 0;
    private ValueAnimator resetAnimation;

    private Action action = Action.REFRESHING;

    Rect rects = new Rect();

    /**
     * 滑动到刷新所需的最大倍率，就是刷新控件高度的百分比
     */
    private float refreshMaxRate = 1f;
    private int from = 0;
    private int lastLocation = 0;

    private EasyNestScrollCallback mScrollCallback;

    public EasyNestRefreshView(Context context) {
        super(context);
    }

    public EasyNestRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EasyNestRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EasyNestRefreshView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void offsetTopAndBottom(int offset) {
        layout(getLeft(), getTop() - offset, getRight(), getBottom());
        super.offsetTopAndBottom(offset);
    }

    public boolean isVisiable() {
        if (getLocalVisibleRect(rects)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean scrollY(int y) {
        if (!isEnabled()) return true;
        if (mScrollCallback != null) {
            if (mScrollCallback.scrollY(y)) {
                return true;
            }
        }
        scrollY += y;
        if (scrollY < 0) {
            if (getHeight() != needRefreshHeight) {
                offsetTopAndBottom(-(getHeight() - needRefreshHeight));
            }
            getChildAt(0).setVisibility(INVISIBLE);
            return false;
        } else {
            getChildAt(0).setVisibility(VISIBLE);
        }
        offsetTopAndBottom(y);
        float refreshHeight = needRefreshHeight * refreshMaxRate;
        if (scrollY >= refreshHeight) {
            float calcY = scrollY;
            float pre = calcY / refreshHeight;
            for (int i = 0; i < onNestFreshListenerList.size(); i++) {
                onNestFreshListenerList.get(i).scaleValue(pre);
                onNestFreshListenerList.get(i).onOverlyRefreshHeight(true);
            }
        } else {
            float calcY = scrollY;
            float pre = calcY / refreshHeight;
            for (int i = 0; i < onNestFreshListenerList.size(); i++) {
                onNestFreshListenerList.get(i).onOverlyRefreshHeight(false);
                onNestFreshListenerList.get(i).scaleValue(pre);
            }
        }

        return true;
    }

    private boolean isSelfController;

    @Override
    public boolean up() {
        if (!isEnabled())
            return false;
        if (mScrollCallback != null) {
            if (mScrollCallback.up()) {
                return true;
            }
        }
        if (scrollY == 0) {

            return false;
        }

        if (!isVisiable()) {
            from = scrollY;
            scrollY = 0;
            isSelfController = false;
            offsetTopAndBottom(-from);
            return false;
        }

        if (scrollY >= needRefreshHeight * refreshMaxRate && getHeight() > needRefreshHeight * refreshMaxRate) {
            isSelfController = true;
            int y = (int) (scrollY - needRefreshHeight * refreshMaxRate);
            from = y;
            scrollY -= y;
            if (refreshListener != null) {
                refreshListener.onRefreshing();
                refreshing = true;
            }
            resetAnimation = ValueAnimator.ofInt(0, -from);
            resetAnimation.setDuration(200);
        } else {
            from = scrollY;
            scrollY = 0;
            isSelfController = false;
            offsetTopAndBottom(-from);
            return false;
        }

        if (getAnimation() != null)
            getAnimation().reset();

        clearAnimation();
        lastLocation = 0;
        resetAnimation.removeAllUpdateListeners();
        resetAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int scroll = (int) animation.getAnimatedValue();
                int difScroll = scroll - lastLocation;
                lastLocation = scroll;
                offsetTopAndBottom(difScroll);
                if (isSelfController)
                    refreshListener.onResetLocation(difScroll);
            }
        });
        resetAnimation.removeAllListeners();
        resetAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                refreshListener.setEnableScroll(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isRefreshing()) {
                    setAction(Action.REFRESHING);
                    for (int i = 0; i < onNestFreshListenerList.size(); i++) {
                        onNestFreshListenerList.get(i).onRefreshing();
                    }
                } else {
                    for (int i = 0; i < onNestFreshListenerList.size(); i++) {
                        onNestFreshListenerList.get(i).onInvlidate();
                    }
                }
                if (!refreshing)
                    refreshListener.setEnableScroll(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                refreshListener.setEnableScroll(true);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        resetAnimation.setInterpolator(new DecelerateInterpolator());
        resetAnimation.start();
        return isSelfController;
    }

    @Override
    public boolean release() {
        for (int i = 0; i < onNestFreshListenerList.size(); i++) {
            onNestFreshListenerList.get(i).onInvlidate();
        }
        if (scrollY != 0)
            up();
        return false;
    }

    public boolean isRefreshing() {
        return refreshing;
    }

    public void onRefreshed() {
        setAction(Action.NOLMER);
        if (refreshing) {
            refreshing = false;
            showRefreshedAnimation(null);
        } else {
            refreshListener.setEnableScroll(true);
            refreshListener.onRefreshed();
        }
    }

    public void onRefreshed(final OnRefreshedListener pOnRefreshedListener) {
        setAction(Action.NOLMER);
        if (refreshing) {
            refreshing = false;
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    showRefreshedAnimation(pOnRefreshedListener);
                }
            }, 520);
        } else {
            refreshListener.setEnableScroll(true);
            refreshListener.onRefreshed();
        }
    }

    public void doLoadMore() {
        if (action != Action.NOLMER)
            return;
        setAction(Action.LOADMORE);
        for (int i = 0; i < onNestFreshListenerList.size(); i++) {
            onNestFreshListenerList.get(i).doLoadMore();
        }
    }

    private void showRefreshedAnimation(final OnRefreshedListener pOnRefreshedListener) {

        if (scrollY == 0||getHeight()==needRefreshHeight) {
            refreshListener.setEnableScroll(true);
            return;
        }
        if (getAnimation() != null) {
            getAnimation().cancel();
            getAnimation().reset();
        }

        clearAnimation();
        lastLocation = 0;
        scrollY = 0;
        from = getHeight()-needRefreshHeight;
        resetAnimation = ValueAnimator.ofInt(0, -from);
        resetAnimation.removeAllUpdateListeners();
        resetAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int scroll = (int) animation.getAnimatedValue();
                int difScroll = scroll - lastLocation;
                lastLocation = scroll;
                offsetTopAndBottom(difScroll);
                refreshListener.onResetLocation(difScroll);
            }
        });
        resetAnimation.removeAllListeners();
        resetAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                refreshListener.setEnableScroll(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                refreshListener.setEnableScroll(true);
                refreshListener.onRefreshed();
                if (pOnRefreshedListener != null)
                    pOnRefreshedListener.onRefreshed();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        resetAnimation.setDuration(500);
        resetAnimation.setInterpolator(new DecelerateInterpolator());
        resetAnimation.start();
    }

    public void setRefreshMaxRate(float pRefreshMaxRate) {
        refreshMaxRate = pRefreshMaxRate;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (needRefreshHeight == 0) {
            needRefreshHeight = getHeight();
        }
        if (scrollY < 0)
            return;
        if (needRefreshHeight != getHeight() && getHeight() >= needRefreshHeight) {
            final int count = getChildCount();
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                if (child.getVisibility() != GONE) {
                    child.layout(child.getLeft(), child.getTop() + needRefreshHeight, child.getRight(), child.getBottom() + needRefreshHeight);
                }
            }
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ViewGroup viewGroup = (ViewGroup) getParent();
        viewGroup.getLocalVisibleRect(rects);
    }

    void setRefreshListener(RefreshListener refreshListener) {
        this.refreshListener = refreshListener;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
    }

    @Override
    public void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
    }

    public void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
    }

    public void addOnNestFreshListenerList(EasyOnNestReFreshListener listener) {
        onNestFreshListenerList.add(listener);
    }

    public void setScrollCallback(EasyNestScrollCallback scrollCallback) {
        mScrollCallback = scrollCallback;
    }

    interface RefreshListener {

        void onRefreshed();

        void onRefreshing();

        void onResetLocation(int y);

        void setEnableScroll(boolean enable);
    }

    public interface OnRefreshedListener {
        void onRefreshed();
    }

    public enum Action {
        REFRESHING, LOADMORE, NOLMER;
    }

    public static abstract class DefaultRefreshListener implements EasyOnNestReFreshListener {

        @Override
        public void onRefreshing() {
            if (getStatuText() == null)
                return;
            getStatuText().setText("正在刷新……");
        }

        @Override
        public void scaleValue(float progress) {

        }

        @Override
        public void onOverlyRefreshHeight(boolean overly) {
            if (getStatuText() == null)
                return;
            if (overly) {
                if (!"释放立即刷新".equals(getStatuText().getText().toString()))
                    getStatuText().setText("释放立即刷新");
            } else if (!"下拉刷新".equals(getStatuText().getText().toString())) {
                getStatuText().setText("下拉刷新");
            }
        }

        public abstract TextView getStatuText();
    }
}
