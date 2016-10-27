package com.lazymc.easynestrefresh;

import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ListView;

/**
 * Created by huacangliang on 2016/1/8.
 */
public class ListViewUtils {

    private static final String TAG = "ListViewUtils";
    private boolean canLoad;

    public int count = 10;

    private boolean enable = true;

    private int page;

    public void enable() {
        enable = true;
    }

    public void disable() {
        enable = false;
    }

    public void setLoadMoreListener(ListView listView, final CallBack callBack) {
        if (listView == null || callBack == null)
            return;

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (!enable)
                    return;
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (canLoad) {
                        if (callBack != null) {
                            callBack.onLoadMore();
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!enable)
                    return;
                if (firstVisibleItem + visibleItemCount + visibleItemCount >= totalItemCount - 1 && totalItemCount >= count) {
                    canLoad = true;
                } else {
                    canLoad = false;
                }

            }
        });
    }

    public void setLoadMoreListener(NestedScrollView listView, final CallBack callBack) {
        if (listView == null || callBack == null)
            return;
        listView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                View child = v.getChildAt(0);
                boolean res = child.getMeasuredHeight() <= v.getScrollY() + v.getHeight();
                if (res) {
                    callBack.onLoadMore();
                }
            }
        });
    }

    public void setLoadMoreListener(GridView listView, final CallBack callBack) {
        if (listView == null || callBack == null)
            return;

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (!enable)
                    return;
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (canLoad) {
                        if (callBack != null) {
                            page++;
                            callBack.onLoadMore();
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!enable)
                    return;
                if (firstVisibleItem + visibleItemCount + visibleItemCount >= totalItemCount - 1 && totalItemCount >= count) {
                    canLoad = true;
                } else {
                    canLoad = false;
                }

            }
        });
    }

    public void setLoadMoreListener(RecyclerView listView, final CallBack callBack) {
        if (listView == null || callBack == null)
            return;

        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int lastCount = 0;
            RecyclerView.LayoutManager manager;
            boolean isUp;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!isUp)
                    return;
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (lastCount + 2 >= recyclerView.getAdapter().getItemCount() && lastCount > 0) {
                        callBack.onLoadMore();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                isUp = dy > 0;
                if (!isUp)
                    return;
                if (manager == null) {
                    manager = recyclerView.getLayoutManager();
                }

                if (manager instanceof GridLayoutManager) {
                    lastCount = ((GridLayoutManager) manager).findLastVisibleItemPosition();
                } else if (manager instanceof LinearLayoutManager) {
                    lastCount = ((LinearLayoutManager) manager).findLastVisibleItemPosition();
                }
            }
        });
    }

    /**
     * @param linearLayoutManager
     * @return 返回所有item所需高度，如果高度=-1那么说明没有内容
     */
    public static int getListItemTotalHeight(RecyclerView.LayoutManager linearLayoutManager, boolean hasHead, boolean hasFooter) {
        int count = linearLayoutManager.getChildCount();
        if (count < 1)
            return -1;
        int height = 0;
        View view = null;
        for (int i = 0; i < 1; i++) {
            view = linearLayoutManager.getChildAt(i);
            height += view.getHeight();
            height += linearLayoutManager.getBottomDecorationHeight(view);
            height += linearLayoutManager.getTopDecorationHeight(view);
        }
        count = linearLayoutManager.getItemCount();
        if (hasHead)
            count -= 1;
        if (hasFooter)
            count -= 1;

        height *= count;
        if (height == 0)
            return -1;
        return height;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public interface CallBack {
        void onLoadMore();
    }
}
