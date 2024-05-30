package com.kenz.almuslim.ui.favourite;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.kenz.almuslim.R;
import com.kenz.almuslim.data.adapter.AdapterWallpaper;
import com.kenz.almuslim.ui.main.MainActivity;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;


import com.kenz.almuslim.data.base.BasePresenter;
import com.kenz.almuslim.data.base.EndlessRecyclerViewScrollListener;
import com.kenz.almuslim.data.utils.DatabaseHandler;
import com.kenz.almuslim.data.widgets.AnimationHelpers;
import com.kenz.almuslim.data.widgets.ProgressLoader;

public class FavouritePresenter extends BasePresenter<FavouriteView> {
    private Activity activity;
    private View rootView;
    public ProgressLoader progressLoader;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DatabaseHandler databaseHandler;
    private AdapterWallpaper adapterWallpaper;
    private RecyclerView recyclerView;
    protected void initView(Activity activity, View rootView) {
        this.activity = activity;
        this.rootView = rootView;

    }

    private void initSwipeRefresh(){
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initRecyclerViewHome();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    protected void onResume(){
        progressLoader = new ProgressLoader(activity);
        databaseHandler = new DatabaseHandler(activity);
        initRecyclerViewHome();
        initSwipeRefresh();
    }

    private void initRecyclerViewHome() {
        progressLoader.show();
        recyclerView = rootView.findViewById(R.id.recycler_view);
        final GridLayoutManager grid = new GridLayoutManager(activity, 3);
        recyclerView.setLayoutManager(grid);
        AdapterWallpaper adapterWallpaper = new AdapterWallpaper(activity, databaseHandler.getFavouriteWallpapers());

        ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(adapterWallpaper);
        scaleInAnimationAdapter.setFirstOnly(false);
        scaleInAnimationAdapter.setDuration(2000);
        scaleInAnimationAdapter.setInterpolator(new OvershootInterpolator(.5f));
        recyclerView.setAdapter(scaleInAnimationAdapter);
        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(grid) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {

            }

            @Override
            public void onScrolled(int dy) {
                if (grid.findFirstVisibleItemPosition() != 0 && databaseHandler.getFavouriteWallpapers().size() > 20) {
                    if (rootView.findViewById(R.id.button_to_top).getVisibility() != View.VISIBLE) {
                        rootView.findViewById(R.id.button_to_top).setVisibility(View.VISIBLE);
                        AnimationHelpers.startAnimationTopOrBottom(rootView.findViewById(R.id.button_to_top), -200, 2000);
                    }
                } else {
                    rootView.findViewById(R.id.button_to_top).setVisibility(View.GONE);
                }
            }
        });
        rootView.findViewById(R.id.button_to_top).setVisibility(databaseHandler.getFavouriteWallpapers().size() < 20 ? View.GONE : View.VISIBLE);
        rootView.findViewById(R.id.layout_no_favourite).setVisibility(databaseHandler.getFavouriteWallpapers().size() == 0 ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(databaseHandler.getFavouriteWallpapers().size() == 0 ? View.GONE : View.VISIBLE);
        rootView.findViewById(R.id.button_to_top).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator animY = ObjectAnimator.ofFloat(rootView.findViewById(R.id.button_to_top), "translationY", -800);
                animY.setDuration(3000);
                animY.setInterpolator(new OvershootInterpolator());
                animY.start();
                recyclerView.smoothScrollToPosition(0);
                ((MainActivity) activity).exitCollapsed();
            }
        });
        progressLoader.stopLoader();
    }

}
