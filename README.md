# androidRefresh

该刷新框架可定制性超高，没有基本开发水平无法使用
头部采用了view容器方式，具体内容自己决定，可将容器类view替换成你想要的任意view
刷新框架回调的数据也很多，常用回调为：
onRefreshing()刷新回调，开发者自己维护刷新动画
doLoadMore()加载更多回调，需要配合工具
onOverlyRefreshHeight()手指下拉距离超出可刷新高度时，开发者可任意设置内容
scaleValue()滑动过程不断回调，传回当前滑动的距离比例，即当前手指滑动距离/可刷新的高度 的关系
刷新框架可用于listview（未测试）和recycleview，以及scrollview、nestScrollView
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
              //刷新动画需要自己做，动画设置方式为
                NoRequestImageView iv_refreshing_progress=EasyNestRefreshView.findViewById(R.id.iv_refreshing_progress);
                iv_refreshing_progress.setAnimal....
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
