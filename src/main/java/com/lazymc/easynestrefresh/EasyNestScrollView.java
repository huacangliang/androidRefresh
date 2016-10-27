package com.lazymc.easynestrefresh;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 * Created by huacangliang on 2016/4/16.
 */
public class EasyNestScrollView extends LinearLayout implements EasyNestRefreshView.RefreshListener {
    private static final String TAG = "EasyNestScrollView";
    private EasyNestScrollCallback scrollCallBack;
    private boolean isScrollAble = true;
    private float mTouchY;
    private float mMoveY;
    private float mTouchX;
    private float mMoveX;
    private float mInitTouchY;
    private boolean scrolling = false;
    private View mTarget;
    private float rate = 1f;
    private int mScrollDis;
    private int mScreenHeight;
    private float mTouchSlop;
    private boolean isToTopAndBottom;//是否允许移动到顶部及底部
    ValueAnimator contentAnimaltion = new ValueAnimator();
    int from = 0;
    private boolean animStop;
    int scrollOffset = 0;
    int lastLocation = 0;//最后移动动画的位置
    private boolean isFling;
    private boolean isScrollHori;
    private boolean childScrolling;
    private static final boolean debug = false;
    private boolean isChildController = true;
    private Rect mRect = new Rect();
    private boolean isTop;//
    private boolean isBottom = false;
    private boolean notRequestLayout;
    private boolean hasData;
    private boolean enableNestScroll;
    private boolean beforeUp;
    /**
     * 恢复原始状态动画时间
     */
    private final static long RESETANIMALDRUAN = 300;
    private boolean enableScroll = true;

    public EasyNestScrollView(Context context) {
        super(context);
        init();
    }

    public EasyNestScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EasyNestScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public EasyNestScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    GestureDetectorCompat gestureDetectorCompat;
    GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (isScrollHori) {
                scrolling = true;
                return false;
            }
            if (isFling || childScrolling) {
                if (debug) L.d(TAG, "onScroll: " + childScrolling + "--" + isFling);
                return true;
            }

            if (Math.abs(distanceX) > Math.abs(distanceY) && scrollOffset == 0) {
                isScrollHori = true;
                scrolling = true;
                return false;
            } else {
                isScrollHori = false;
            }

            if (e2.getHistorySize() > 0) {
                float oldY = e2.getHistoricalY(0);
                float newY = e2.getY(0);
                float scroll = newY - oldY;
                distanceY = -scroll;
            }

            int offset = (int) (-distanceY * rate);

            if (!isToTopAndBottom && offset < 0) {
                if (Math.abs(mScrollDis + offset) >= mScreenHeight) {

                    return true;
                }
                if (offset > 0 && mScrollDis >= 0) {
                    if (mScrollDis > mScreenHeight / 2) {
                        offset = (int) (offset * 0.1);
                    } else if (mScrollDis > mScreenHeight / 3) {
                        offset = (int) (offset * 0.2);
                    }
                }
            }

            if (!isScrollView()) {
                if (!isTop || !isBottom) {
                    if (isBottom && offset > 0) {
                        if (getTop() + offset > 0 || getTop() == mRect.top) {
                            scrolling = false;
                            if (scrollOffset != 0) {
                                startScroll(-scrollOffset);
                            }
                            return false;
                        }
                    } else if (!isBottom && offset < 0) {
                        if (getBottom() + offset < mRect.bottom) {
                            scrolling = false;
                            if (scrollOffset != 0) {
                                startScroll(-scrollOffset);
                            }
                            return false;
                        }
                    }
                } else {
                    if (enableNestScroll) {
                        if (offset <= 0 && scrollOffset + offset <= 0 && scrollOffset > 0
                                ) {
                            if (debug)
                                L.d(TAG, "onScroll: " + offset + "||" + scrollOffset);
                            scrolling = false;
                            startScroll(-scrollOffset);
                            return false;
                        } else if (offset > 0 && beforeUp) {
                            if (offset + getBottom() > mRect.bottom) {
                                if (debug)
                                    L.d(TAG, "onScroll__||: " + offset + "||" + scrollOffset);
                                beforeUp = scrolling = false;
                                startScroll(-scrollOffset);
                                return false;
                            }

                        } else {
                            startScroll(offset);
                            mScrollDis += offset;
                            scrolling = true;
                            return true;
                        }
                    }
                }
            } else if (getBottom() != mRect.bottom && !mRect.isEmpty() && mTarget.getScrollY() != 0) {
                if (getBottom() + offset > mRect.bottom) {
                    scrolling = false;
                    if (scrollOffset != 0) {
                        startScroll(-scrollOffset);
                    }
                    return false;
                }
            }
            startScroll(offset);
            mScrollDis += offset;
            scrolling = true;
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            isFling = true;
            if (childScrolling) {
                if (debug) L.d(TAG, "onFling: " + scrollOffset);
                startScroll(-scrollOffset);
                scrollOffset = 0;
            }
            return true;
        }
    };

    protected void startScroll(int offset) {

        if ((!isBottom || isTop) && scrollCallBack != null) {
            isChildController = scrollCallBack.scrollY(offset);
            if (isChildController && isScrollView()) {
                return;
            }
        }
        offsetTopAndBottom(offset);
    }

    private boolean animalReset() {
        if (debug) L.d(TAG, "animalReset: " + scrollOffset);
        if (scrollOffset == 0) {
            return false;
        }

        if (scrollOffset > 0) {
            from = -scrollOffset;
        } else {
            from = (getHeight() - getBottom());
            if (from < -scrollOffset)
                from = -scrollOffset;
        }

        if (getAnimation() != null)
            getAnimation().reset();

        clearAnimation();
        setEnabled(false);
        contentAnimaltion.setIntValues(0, from);
        contentAnimaltion.cancel();
        contentAnimaltion.setDuration(RESETANIMALDRUAN);
        Interpolator interpolator = new DecelerateInterpolator(1.0f);
        contentAnimaltion.setInterpolator(interpolator);
        animStop = false;
        lastLocation = 0;
        contentAnimaltion.removeAllUpdateListeners();
        contentAnimaltion.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int age = (int) animation.getAnimatedValue();
                if (!animStop)
                    animStop = contentAnimal(age);
                if (animStop) {
                    contentAnimaltion.cancel();
                }
            }
        });
        contentAnimaltion.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isScrollAble = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isScrollAble = true;
                scrolling = false;
                scrollOffset = 0;
                setEnabled(true);
                requestLayout();
                if (scrollCallBack != null)
                    scrollCallBack.release();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                setEnabled(true);
                isScrollAble = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        contentAnimaltion.start();
        return true;
    }

    /**
     * 内容主体动画
     *
     * @param interpolatedTime
     * @return
     */
    private boolean contentAnimal(int interpolatedTime) {
        offsetTopAndBottom(interpolatedTime - lastLocation);
        lastLocation = interpolatedTime;
        return animStop;
    }

    @Override
    public void offsetTopAndBottom(int offset) {
        if (mRect.isEmpty()) {
            mRect.set(getLeft(), getTop(), getRight(), getBottom());
        }
        super.offsetTopAndBottom(offset);
        scrollOffset += offset;
        if (!beforeUp)
            beforeUp = getBottom() < mRect.bottom;
    }

    private void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setNestedScrollingEnabled(false);
        }
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        setOrientation(VERTICAL);
        gestureDetectorCompat = new GestureDetectorCompat(getContext(), gestureListener);
        final ViewConfiguration vc = ViewConfiguration.get(getContext());
        mTouchSlop = vc.getScaledWindowTouchSlop();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void addView(View child) {
        super.addView(child);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            if (debug) L.d(TAG, "dispatchTouchEvent: " + isEnabled());
            return super.dispatchTouchEvent(ev);
        }
        if (!enableScroll) {
            return true;
        }
        if (!hasData) {
            getRecycleScroll();
        }

        //取消多手指控制
        if (MotionEventCompat.getPointerCount(ev) > 1) {
            ev.setAction(MotionEvent.ACTION_CANCEL);

            if (!releaseDrager())
                callSuperDispatch(ev);
            return true;
        }
        calcChildScroll(ev);
        int actionIndex = MotionEventCompat.getActionIndex(ev);
        if (actionIndex == MotionEvent.INVALID_POINTER_ID) {
            if (debug)
                L.d(TAG, "dispatchTouchEvent: actionIndex == MotionEvent.INVALID_POINTER_ID");
            return super.dispatchTouchEvent(ev);
        }
        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                gestureDetectorCompat.onTouchEvent(ev);
                mInitTouchY = (int) (MotionEventCompat.getY(ev, 0) + 0.5f);
                isChildController = true;
                callSuperDispatch(ev);
                return true;
            case MotionEvent.ACTION_MOVE:
                //但凡有移动迹象都不以横向通过
                if (isScrollHori && scrollOffset == 0) {
                    scrolling = false;
                    return isScrollHori = super.dispatchTouchEvent(ev);
                } else {
                    isScrollHori = false;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (enableNestScroll) {
                    if (ev.getAction() == MotionEvent.ACTION_CANCEL) {
                        if (scrollOffset > 0)
                            startScroll(-scrollOffset);
                        isScrollHori = false;
                        childScrolling = false;
                        isFling = false;
                        return false;
                    }
                }
                if (debug) L.d(TAG, "dispatchTouchEvent: ACTION_CANCEL");
                //计算点击事件用的,获取当前坐标,+0.5的作用是加大点击事件灵敏度
                final int y = (int) (MotionEventCompat.getY(ev, 0) + 0.5f);
                mInitTouchY = y - mInitTouchY;
                //计算手指移动距离,如果小于点击范围,将事件传递出去,做点击等功能用,
                // scrollOffset!=0说明已经移动了,要消费该功能,scrolling也是一样的道理
                if (Math.abs(mInitTouchY) <= mTouchSlop && scrollOffset == 0 && !scrolling) {
                    if (debug) L.d(TAG, "dispatchTouchEvent: 9");
                    childScrolling = false;
                    isFling = false;
                    isScrollHori = false;
                    return super.dispatchTouchEvent(ev);
                }

                gestureDetectorCompat.onTouchEvent(ev);

                boolean handler = false;
                if (debug) L.d(TAG, "dispatchTouchEvent 13: " + scrollOffset);

                handler = releaseDrager();
                if (!handler && !scrolling) {
                    callSuperDispatch(ev);
                } else {
                    if (!scrolling) {
                        callSuperDispatch(ev);
                    }
                }

                childScrolling = false;
                isFling = false;
                if (isScrollHori) {
                    isScrollHori = false;
                    return super.dispatchTouchEvent(ev);
                }
                if (scrollOffset == 0) {
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    super.dispatchTouchEvent(ev);
                    ev.setAction(MotionEvent.ACTION_UP);
                    return super.dispatchTouchEvent(ev);
                }

                return false;
            }
        }

        if (isScrollHori) {
            scrolling = false;
            return isScrollHori = super.dispatchTouchEvent(ev);
        }

        if (!isScrollAble || childScrolling || isFling) {
            if (childScrolling && canChildScroll(mTarget)) {
                if (debug) L.d(TAG, "dispatchTouchEvent: 1");
                return super.dispatchTouchEvent(ev);
            } else {
                isFling = false;
                childScrolling = false;
                isChildController = false;
                gestureDetectorCompat.onTouchEvent(ev);
                if (debug) L.d(TAG, "dispatchTouchEvent: 2" + isScrollHori);
                return true;
            }
        }

        if (scrolling) {
            if (canChildScroll(mTarget)) {
                //从拉动状态变成子控件滚动事件
                //需要恢复原始尺寸
                if (getBottom() != mRect.bottom || getTop() != mRect.top) {
                    startScroll(-scrollOffset);
                    if (debug) L.d(TAG, "onScroll: " + scrollOffset);
                    scrollOffset = 0;
                }
                childScrolling = true;
                scrolling = false;
                return true;
            }
            childScrolling = false;
            if (debug) L.d(TAG, "onScroll: childScrolling = false");
            gestureDetectorCompat.onTouchEvent(ev);
        }

        if (canChildScroll(mTarget)) {
            if (debug) L.d(TAG, "dispatchTouchEvent: 5");
            super.dispatchTouchEvent(ev);
            return true;
        }

        if (gestureDetectorCompat.onTouchEvent(ev)) {
            if (debug) L.d(TAG, "dispatchTouchEvent: 8");
            return true;
        }

        if (isScrollHori) {
            if (debug) L.d(TAG, "dispatchTouchEvent: 12");
            return isScrollHori = super.dispatchTouchEvent(ev);
        }
        if (debug) L.d(TAG, "dispatchTouchEvent: 13" + scrolling);
        return scrolling;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return super.canScrollVertically(direction) && !scrolling;
    }

    /**
     * 取消横向滑动功能
     *
     * @param ev
     * @return
     */
    protected boolean callSuperDispatch(MotionEvent ev) {
        if (isScrollHori) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 获取子布局能否向上滑动能力
     *
     * @return
     */
    public boolean up() {
        if (scrollCallBack != null && (!isBottom || isTop)) {
            if (debug) L.d(TAG, "up: " + getTop());
            return scrollCallBack.up();
        }
        return false;
    }

    /**
     * 释放手指,做回退动画
     *
     * @return
     */
    protected boolean releaseDrager() {
        boolean handler = false;
        if (isChildController && scrollCallBack != null && scrolling) {
            handler = false;
            mScrollDis = 0;
            isChildController = up();
            if (!isChildController)//获取控制权限,true 是子布局控制
                releaseDrager();
        } else if (isScrollAble && !childScrolling && scrolling) {
            if (debug) L.d(TAG, "releaseDrager: 1");
            if (scrollCallBack != null) {
                if (!up()) {
                    //外面插件没有处理放开手指动作，由这里控制
                    if (debug) L.d(TAG, "releaseDrager: 2");
                    handler = animalReset();
                }
            } else {
                if (debug) L.d(TAG, "releaseDrager: 3");
                handler = animalReset();
            }
            mScrollDis = 0;
        } else {
            return up();
        }
        return handler;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    /**
     * 判断第一个子布局是滚动view还是列表view
     *
     * @return
     */
    protected boolean isScrollView() {
        return mTarget instanceof ScrollView || mTarget instanceof LSScrollveiw
                || mTarget instanceof NestedScrollView;
    }

    /**
     * 计算子布局能否继续滚动
     */
    private boolean canChildScroll(View mTarget) {
        //1方向向上
        boolean up = ViewCompat.canScrollVertically(mTarget,1);
        //<=-1方向向下
        boolean dwon = ViewCompat.canScrollVertically(mTarget,-1);
        //如果能上和下滚动,说明子布局居于中间位置,始终可以滚动
        if (up && dwon) {
            return childScrolling = true;
        }

        boolean handler = false;
        //当mMoveY == 0 && mThuochY == 0时,状态处于初始化,将控制权交给子布局
        if (mMoveY == 0 && mTouchY == 0) {
            if (scrollOffset == 0)
                handler = true;
            if (debug) L.d(TAG, "canChildScroll:_1_ " + handler);
        } else if (mMoveY > mTouchY) {
            //当前移动坐标大于上次触摸坐标,方向向下
            handler = canChildScroll(mTarget, -1);
            if (debug) L.d(TAG, "canChildScroll:_2_ " + handler);
        } else if (mMoveY < mTouchY) {
            //当前移动坐标小于上次触摸坐标,方向向上
            if (getTop() > mRect.top) {
                handler = false;
                if (debug) L.d(TAG, "canChildScroll:_3_ " + handler);
            } else {
                handler = canChildScroll(mTarget, 1);
                if (debug) L.d(TAG, "canChildScroll:_4_ " + handler);
            }
        } else {
            //mMoveY == mThuochY 即当前点为初始坐标,不做处理
            handler = true;
            if (debug) L.d(TAG, "canChildScroll:_5_ " + handler);
        }
        childScrolling = handler;

        if (Math.abs(mMoveX - mTouchX) > Math.abs(mMoveY - mTouchY))
            isScrollHori = true;

        return handler;
    }

    private boolean isRecycleView(View mTarget) {
        return mTarget instanceof RecyclerView;
    }

    /**
     * 实时计算当前滚动状态
     * 用于滚动方向判断
     *
     * @param ev
     */
    private void calcChildScroll(MotionEvent ev) {

        int actionIndex = MotionEventCompat.getActionIndex(ev);
        if (actionIndex == MotionEvent.INVALID_POINTER_ID) {
            mTouchY = 0;
            mMoveY = 0;
            mMoveX = 0;
            mTouchX = 0;
            return;
        }
        final float focusY = ev.getRawY();
        final float focusX = ev.getRawX();
        final int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mTouchY = focusY;
                mMoveY = 0;
                mMoveX = 0;
                mTouchX = focusX;
                break;
            case MotionEvent.ACTION_MOVE:
                mMoveY = focusY;
                mMoveX = focusX;
                if (debug) L.d(TAG, "calcChildScroll--mMoveY:" + mMoveY + "||mThuochY" + mTouchY);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTouchY = 0;
                mMoveY = 0;
                mMoveX = 0;
                mTouchX = 0;
                break;
        }
    }

    public boolean canChildScroll(View mTarget, int direction) {
        if (mTouchY == 0 || mMoveY == 0)
            return false;
        return ViewCompat.canScrollVertically(mTarget, direction);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //3
        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mTarget == null) {
            mTarget = getChildAt(0);
            ViewGroup parent = (ViewGroup) getParent();
            setBackgroundDrawable(parent.getBackground());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mTarget.setNestedScrollingEnabled(false);
            }
        }
        int childHeight = mTarget.getMeasuredHeight();
        setMeasuredDimension(getMeasuredWidth(), childHeight);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onRefreshed() {
        isScrollAble = true;
        mScrollDis = 0;
        scrollOffset = 0;
        childScrolling = false;
        scrolling = false;
        requestLayout();
    }

    @Override
    public void onRefreshing() {
        isScrollAble = false;
        mScrollDis = 0;
        childScrolling = false;
        scrollOffset = 0;
    }

    @Override
    public void onResetLocation(final int y) {
        offsetTopAndBottom(y);
    }

    public void setEnableScroll(boolean enable) {
        enableScroll = enable;
    }

    public void setScrollCallBack(EasyNestScrollCallback scrollCallBack) {
        this.scrollCallBack = scrollCallBack;
    }

    private void computeTopOrBottom() {
        if (isRecycleView(mTarget)) {
            RecyclerView mRecyclerView = (RecyclerView) mTarget;
            //先测量没有滚动的情况，主要原因是item不超过一页
            {
                View lastChildView = mRecyclerView.getLayoutManager().getChildAt(mRecyclerView.getLayoutManager().getChildCount() - 1);
                if (lastChildView == null)
                    return;

                hasData = true;
                //得到lastChildView的bottom坐标值
                int lastChildBottom = lastChildView.getBottom();
                //得到Recyclerview的底部坐标减去底部padding值，也就是显示内容最底部的坐标
                int recyclerBottom = mRecyclerView.getBottom() - mRecyclerView.getPaddingBottom();
                //通过这个lastChildView得到这个view当前的position值
                int lastPosition = mRecyclerView.getLayoutManager().getPosition(lastChildView);

                //判断lastChildView的bottom值跟recyclerBottom
                //判断lastPosition是不是最后一个position
                //如果两个条件都满足则说明是真正的滑动到了底部
                if (lastChildBottom <= recyclerBottom && lastPosition == mRecyclerView.getLayoutManager().getItemCount() - 1
                        ) {
                    isBottom = true;
                } else {
                    isBottom = false;
                }
                int firstTop = mRecyclerView.getLayoutManager().getChildAt(0).getTop();
                //判断是否在顶部
                isTop = firstTop >= mRecyclerView.getTop();
            }
        }
    }

    protected void getRecycleScroll() {
        if (isRecycleView(mTarget)) {
            RecyclerView mRecyclerView = (RecyclerView) mTarget;
            //先测量没有滚动的情况，主要原因是item不超过一页
            {
                hasData = true;
                if (mRecyclerView.getLayoutManager() == null || mRecyclerView.getLayoutManager().getChildCount() == 0) {
                    return;
                }
                View lastChildView = mRecyclerView.getLayoutManager().getChildAt(mRecyclerView.getLayoutManager().getChildCount() - 1);
                if (lastChildView == null)
                    return;

                //得到lastChildView的bottom坐标值
                int lastChildBottom = lastChildView.getBottom();
                //得到Recyclerview的底部坐标减去底部padding值，也就是显示内容最底部的坐标
                int recyclerBottom = mRecyclerView.getBottom() - mRecyclerView.getPaddingBottom();
                //通过这个lastChildView得到这个view当前的position值
                int lastPosition = mRecyclerView.getLayoutManager().getPosition(lastChildView);

                //判断lastChildView的bottom值跟recyclerBottom
                //判断lastPosition是不是最后一个position
                //如果两个条件都满足则说明是真正的滑动到了底部
                if (lastChildBottom <= recyclerBottom && lastPosition == mRecyclerView.getLayoutManager().getItemCount() - 1
                        ) {
                    isBottom = true;
                } else {
                    isBottom = false;
                }
                int firstTop = mRecyclerView.getLayoutManager().getChildAt(0).getTop();
                //判断是否在顶部
                isTop = firstTop >= mRecyclerView.getTop();
            }
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    // L.d(TAG, "onScrollStateChanged: ");
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                    //得到当前显示的最后一个item的view
                    View lastChildView = recyclerView.getLayoutManager().getChildAt(recyclerView.getLayoutManager().getChildCount() - 1);
                    if (lastChildView == null)
                        return;
                    //得到lastChildView的bottom坐标值
                    int lastChildBottom = lastChildView.getBottom();
                    //得到Recyclerview的底部坐标减去底部padding值，也就是显示内容最底部的坐标
                    int recyclerBottom = recyclerView.getBottom() - recyclerView.getPaddingBottom();
                    //通过这个lastChildView得到这个view当前的position值
                    int lastPosition = recyclerView.getLayoutManager().getPosition(lastChildView);

                    //判断lastChildView的bottom值跟recyclerBottom
                    //判断lastPosition是不是最后一个position
                    //如果两个条件都满足则说明是真正的滑动到了底部
                    if (lastChildBottom <= recyclerBottom && lastPosition == recyclerView.getLayoutManager().getItemCount() - 1
                            ) {
                        isBottom = true;
                    } else {
                        isBottom = false;
                    }
                    int firstTop = recyclerView.getLayoutManager().getChildAt(0).getTop();
                    //判断是否在顶部
                    isTop = firstTop >= recyclerView.getTop();
                    // L.d(TAG, "onScrolled: " + isBottom);
                }

            });
        } else {
            hasData = true;//不是列表，不需要监听
        }
    }

    public boolean isToTopAndBottom() {
        return isToTopAndBottom;
    }

    public void setToTopAndBottom(boolean toTopAndBottom) {
        isToTopAndBottom = toTopAndBottom;
    }

    public boolean isScrolling() {
        return scrolling;
    }


    public boolean isChildController() {
        return isChildController;
    }

    public void setChildController(boolean childController) {
        isChildController = childController;
    }

    public void setScrolling(boolean scrolling) {
        this.scrolling = scrolling;
    }

    @Override
    public void requestLayout() {
        if (scrollOffset != 0 || notRequestLayout) return;
        super.requestLayout();
    }

    public void setNotRequestLayout(boolean notRequestLayout) {
        this.notRequestLayout = notRequestLayout;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public boolean isEnableNestScroll() {
        return enableNestScroll;
    }

    public void setEnableNestScroll(boolean enableNestScroll) {
        this.enableNestScroll = enableNestScroll;
    }
}
