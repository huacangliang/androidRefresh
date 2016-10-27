package com.lazymc.easynestrefresh;

/**
 * Created by huacangliang on 2016/4/18.
 */
public interface EasyOnNestReFreshListener extends EasyOnRefreshingStateListener{
    void onRefreshing();
    void scaleValue(float value);
    void onInvlidate();
    boolean doLoadMore();
}
