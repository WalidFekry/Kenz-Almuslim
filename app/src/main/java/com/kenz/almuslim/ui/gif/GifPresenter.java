package com.kenz.almuslim.ui.gif;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.benkkstudio.bsjson.BSJson;
import com.benkkstudio.bsjson.BSObject;
import com.benkkstudio.bsjson.Interface.BSJsonV2Listener;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.kenz.almuslim.R;
import com.kenz.almuslim.data.adapter.AdapterWallpaper;
import com.kenz.almuslim.data.base.BasePresenter;
import com.kenz.almuslim.data.base.EndlessRecyclerViewScrollListener;
import com.kenz.almuslim.data.model.ModelWallpaper;
import com.kenz.almuslim.data.utils.Constant;
import com.kenz.almuslim.data.utils.Helpers;
import com.kenz.almuslim.data.widgets.ProgressLoader;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class GifPresenter extends BasePresenter<GifView> {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Activity activity;
    private View rootView;
    private int pageCount = 1;
    private int totalPost;
    private ArrayList<ModelWallpaper> arrayList;
    private AdapterWallpaper adapterWallpaper;
    public ProgressLoader progressLoader;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    protected void initView(Activity activity, View rootView) {
        this.activity = activity;
        this.rootView = rootView;
        progressLoader = new ProgressLoader(activity);
        View btnFilter = rootView.findViewById(R.id.button_filter);
        if (btnFilter != null) {
            btnFilter.setVisibility(View.GONE);
        }
        initRecyclerViewHome();
        initSwipeRefresh();
    }

    private void initSwipeRefresh() {
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                pageCount = 1;
                initRecyclerViewHome();
            });
        }
    }

    private void initRecyclerViewHome() {
        recyclerView = rootView.findViewById(R.id.recycler_view);
        final GridLayoutManager grid = new GridLayoutManager(activity, 3);
        recyclerView.setLayoutManager(grid);
        adapterWallpaper = new AdapterWallpaper(activity, new ArrayList<>());

        ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(adapterWallpaper);
        scaleInAnimationAdapter.setFirstOnly(false);
        scaleInAnimationAdapter.setDuration(1000);
        scaleInAnimationAdapter.setInterpolator(new OvershootInterpolator(.5f));
        recyclerView.setAdapter(scaleInAnimationAdapter);

        grid.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (adapterWallpaper.getItemViewType(position)) {
                    case AdapterWallpaper.VIEW_ITEM:
                        return 1;
                    case AdapterWallpaper.VIEW_PROGRESS:
                    case AdapterWallpaper.VIEW_NATIVE:
                        return 3;
                    default:
                        return -1;
                }
            }
        });
        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(grid) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (adapterWallpaper != null && adapterWallpaper.arrayList != null && adapterWallpaper.arrayList.size() <= totalPost) {
                    handler.postDelayed(() -> {
                        if (adapterWallpaper != null) {
                            adapterWallpaper.removeLoading();
                        }
                        loadNow();
                    }, 1000);
                } else if (adapterWallpaper != null) {
                    adapterWallpaper.removeLoading();
                }
            }

            @Override
            public void onScrolled(int dy) {
            }
        });
        if (Helpers.isConnected(activity)) {
            recyclerView.setVisibility(View.VISIBLE);
            View noFav = rootView.findViewById(R.id.layout_no_favourite);
            if (noFav != null) noFav.setVisibility(View.GONE);
            View noConn = rootView.findViewById(R.id.layout_no_connection);
            if (noConn != null) noConn.setVisibility(View.GONE);
            loadNow();
        } else {
            recyclerView.setVisibility(View.GONE);
            View noConn = rootView.findViewById(R.id.layout_no_connection);
            if (noConn != null) noConn.setVisibility(View.VISIBLE);
            View noFav = rootView.findViewById(R.id.layout_no_favourite);
            if (noFav != null) noFav.setVisibility(View.GONE);
            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private void loadNow() {
        if (progressLoader != null) {
            progressLoader.show();
        }
        arrayList = new ArrayList<>();
        BSObject bsObject = new BSObject();
        bsObject.addProperty("method_name", "wallpaper_gif");
        bsObject.addProperty("page", pageCount);
        bsObject.addProperty("limit", Constant.WALLPAPER_LOADING_POSITION);

        new BSJson.Builder(activity)
                .setServer(Constant.SERVER_URL)
                .setObject(bsObject.getProperty())
                .setMethod(BSJson.METHOD_POST)
                .setListener(new BSJsonV2Listener() {
                    @Override
                    public void onLoaded(String response) {
                        handler.post(() -> {
                            try {
                                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                                JsonArray jsonArray = jsonObject.getAsJsonArray(Constant.TAG_ROOT);
                                for (int i = 0; i < jsonArray.size(); i++) {
                                    JsonObject objJson = jsonArray.get(i).getAsJsonObject();
                                    if (Constant.native_options && Constant.native_loaded) {
                                        if (i == (Constant.WALLPAPER_LOADING_POSITION / 2)) {
                                            arrayList.add(new ModelWallpaper(AdapterWallpaper.VIEW_NATIVE));
                                        }
                                    }
                                    totalPost = objJson.get("post_total").getAsInt();
                                    int wall_id = objJson.get("wall_id").getAsInt();
                                    int cat_id = objJson.get("cat_id").getAsInt();
                                    String wallpaper_title = objJson.get("wallpaper_title").getAsString();
                                    String wallpaper_image_thumb = objJson.get("wallpaper_image_thumb").getAsString();
                                    String wallpaper_image_detail = objJson.get("wallpaper_image_detail").getAsString();
                                    String wallpaper_image_original = objJson.get("wallpaper_image_original").getAsString();
                                    String wallpaper_tags = objJson.get("wallpaper_tags").getAsString();
                                    String wallpaper_type = objJson.get("wallpaper_type").getAsString();
                                    String wallpaper_premium = objJson.get("wallpaper_premium").getAsString();
                                    Long numLikes = objJson.get("num_likes").getAsLong();
                                    Long numViews = objJson.get("num_views").getAsLong();
                                    arrayList.add(new ModelWallpaper(wall_id,
                                            cat_id,
                                            wallpaper_title,
                                            wallpaper_image_thumb,
                                            wallpaper_image_detail,
                                            wallpaper_image_original,
                                            wallpaper_tags,
                                            wallpaper_type,
                                            wallpaper_premium,
                                            AdapterWallpaper.VIEW_ITEM,
                                            numLikes,
                                            numViews));
                                }
                                if (adapterWallpaper != null) {
                                    adapterWallpaper.insertData(arrayList);
                                }
                                pageCount++;
                            } catch (JsonSyntaxException e) {
                                e.printStackTrace();
                            }
                            if (arrayList.isEmpty()) {
                                recyclerView.setVisibility(View.GONE);
                                View noFav = rootView.findViewById(R.id.layout_no_favourite);
                                if (noFav != null) noFav.setVisibility(View.VISIBLE);
                            } else {
                                recyclerView.setVisibility(View.VISIBLE);
                                View noFav = rootView.findViewById(R.id.layout_no_favourite);
                                if (noFav != null) noFav.setVisibility(View.GONE);
                            }
                            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                            if (progressLoader != null) {
                                progressLoader.stopLoader();
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        handler.post(() -> {
                            Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                            if (progressLoader != null) {
                                progressLoader.stopLoader();
                            }
                        });
                    }
                })
                .load();
    }
}
