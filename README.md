# androidRefresh
使用方法：直接在xml中配置
<include
        layout="@layout/refresh_recycleview_commo_layout"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        />
 在代码中这样用
EasyNestRefreshView.addOnNestFreshListenerList(new EasyOnNestReFreshListener() {
            @Override
            public void onRefreshing() {
              //触发刷新动作
            }

            @Override
            public void scaleValue(float value) {
              //下拉距离顶部的比例
            }

            @Override
            public void onInvlidate() {
             //无论是否触发刷新动作放开手指都会走这里
            }

            @Override
            public boolean doLoadMore() {
             //加载更多，需要配合下面的工具使用
                return false;
            }

            @Override
            public void onOverlyRefreshHeight(boolean overly) {
               //下拉距离超过刷新预设值
            }
        });
        /**
        加载更多辅助工具
        */
         new ListViewUtils().setLoadMoreListener(recyclerView, new ListViewUtils.CallBack() {
            @Override
            public void onLoadMore() {
                L.d(TAG, "onLoadMore: ");
                root.doLoadMore();
            }
        });
