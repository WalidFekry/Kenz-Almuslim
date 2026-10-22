package com.kenz.almuslim.ui.favourite;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kenz.almuslim.R;
import com.kenz.almuslim.data.adapter.AdapterWallpaper;
import com.kenz.almuslim.data.base.BasePresenter;
import com.kenz.almuslim.data.base.EndlessRecyclerViewScrollListener;
import com.kenz.almuslim.data.model.ModelWallpaper;
import com.kenz.almuslim.data.utils.DatabaseHandler;
import com.kenz.almuslim.data.widgets.AnimationHelpers;
import com.kenz.almuslim.data.widgets.ProgressLoader;
import com.kenz.almuslim.ui.main.MainActivity;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class FavouritePresenter extends BasePresenter<FavouriteView> {
    private Activity activity;
    private View rootView;
    public ProgressLoader progressLoader;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DatabaseHandler databaseHandler;
    private RecyclerView recyclerView;

    protected void initView(Activity activity, View rootView) {
        this.activity = activity;
        this.rootView = rootView;
    }

    private void initSwipeRefresh() {
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                initRecyclerViewHome();
                swipeRefreshLayout.setRefreshing(false);
            });
        }
    }

    protected void onResume() {
        progressLoader = new ProgressLoader(activity);
        databaseHandler = new DatabaseHandler(activity);
        initRecyclerViewHome();
        initSwipeRefresh();
    }

    private void initRecyclerViewHome() {
        if (progressLoader != null) {
            progressLoader.show();
        }
        recyclerView = rootView.findViewById(R.id.recycler_view);
        final GridLayoutManager grid = new GridLayoutManager(activity, 3);
        recyclerView.setLayoutManager(grid);

        ArrayList<ModelWallpaper> favList = databaseHandler.getFavouriteWallpapers();
        AdapterWallpaper adapterWallpaper = new AdapterWallpaper(activity, favList);

        ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(adapterWallpaper);
        scaleInAnimationAdapter.setFirstOnly(false);
        scaleInAnimationAdapter.setDuration(1000);
        scaleInAnimationAdapter.setInterpolator(new OvershootInterpolator(.5f));
        recyclerView.setAdapter(scaleInAnimationAdapter);

        int favSize = favList.size();
        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(grid) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
            }

            @Override
            public void onScrolled(int dy) {
                View btnToTop = rootView.findViewById(R.id.button_to_top);
                if (btnToTop != null) {
                    if (grid.findFirstVisibleItemPosition() != 0 && favSize > 20) {
                        if (btnToTop.getVisibility() != View.VISIBLE) {
                            btnToTop.setVisibility(View.VISIBLE);
                            AnimationHelpers.startAnimationTopOrBottom(btnToTop, -200, 2000);
                        }
                    } else {
                        btnToTop.setVisibility(View.GONE);
                    }
                }
            }
        });

        View btnToTop = rootView.findViewById(R.id.button_to_top);
        if (btnToTop != null) {
            btnToTop.setVisibility(favSize < 20 ? View.GONE : View.VISIBLE);
            btnToTop.setOnClickListener(v -> {
                ObjectAnimator animY = ObjectAnimator.ofFloat(btnToTop, "translationY", -800);
                animY.setDuration(1000);
                animY.setInterpolator(new OvershootInterpolator());
                animY.start();
                recyclerView.smoothScrollToPosition(0);
                if (activity instanceof MainActivity) {
                    ((MainActivity) activity).exitCollapsed();
                }
            });
        }

        View noFav = rootView.findViewById(R.id.layout_no_favourite);
        if (noFav != null) {
            noFav.setVisibility(favSize == 0 ? View.VISIBLE : View.GONE);
        }
        recyclerView.setVisibility(favSize == 0 ? View.GONE : View.VISIBLE);

        if (progressLoader != null) {
            progressLoader.stopLoader();
        }
    }
}
