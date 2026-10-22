package com.kenz.almuslim.ui.category;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.OvershootInterpolator;

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
import com.kenz.almuslim.data.adapter.AdapterCategories;
import com.kenz.almuslim.data.base.BasePresenter;
import com.kenz.almuslim.data.base.EndlessRecyclerViewScrollListener;
import com.kenz.almuslim.data.model.ModelCategories;
import com.kenz.almuslim.data.utils.Constant;
import com.kenz.almuslim.data.utils.Helpers;
import com.kenz.almuslim.data.widgets.ProgressLoader;

import java.util.ArrayList;

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class CategoriesPresenter extends BasePresenter<CategoriesView> {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Activity activity;
    private View rootView;
    private int pageCount = 1;
    private int totalPost;
    private ArrayList<ModelCategories> arrayList;
    public ProgressLoader progressLoader;
    private AdapterCategories adapterCategories;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    protected void initView(Activity activity, View rootView) {
        this.activity = activity;
        this.rootView = rootView;
        progressLoader = new ProgressLoader(activity);
        initRecyclerView();
        initSwipeRefresh();
    }

    private void initSwipeRefresh() {
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                pageCount = 1;
                if (adapterCategories != null && adapterCategories.arrayList != null) {
                    adapterCategories.arrayList.clear();
                    adapterCategories.notifyDataSetChanged();
                }
                initRecyclerView();
            });
        }
    }

    private void initRecyclerView() {
        recyclerView = rootView.findViewById(R.id.recycler_view);
        final GridLayoutManager grid = new GridLayoutManager(activity, 1);
        recyclerView.setLayoutManager(grid);
        adapterCategories = new AdapterCategories(activity, new ArrayList<>());
        ScaleInAnimationAdapter alphaAdapter = new ScaleInAnimationAdapter(adapterCategories);
        alphaAdapter.setFirstOnly(false);
        alphaAdapter.setDuration(1000);
        alphaAdapter.setInterpolator(new OvershootInterpolator(.5f));
        recyclerView.setAdapter(alphaAdapter);

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(grid) {
            @Override
            public void onScrolled(int dy) {
            }

            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (adapterCategories != null && adapterCategories.arrayList != null && adapterCategories.arrayList.size() <= totalPost) {
                    handler.postDelayed(() -> {
                        adapterCategories.removeLoading();
                        loadNow();
                    }, 1000);
                } else if (adapterCategories != null) {
                    adapterCategories.removeLoading();
                }
            }
        });

        if (Helpers.isConnected(activity)) {
            recyclerView.setVisibility(View.VISIBLE);
            View noConn = rootView.findViewById(R.id.layout_no_connection);
            if (noConn != null) {
                noConn.setVisibility(View.GONE);
            }
            loadNow();
        } else {
            recyclerView.setVisibility(View.GONE);
            View noConn = rootView.findViewById(R.id.layout_no_connection);
            if (noConn != null) {
                noConn.setVisibility(View.VISIBLE);
            }
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
        bsObject.addProperty("method_name", "category_all");
        bsObject.addProperty("page", pageCount);
        bsObject.addProperty("limit", Constant.CATEGORY_LOADING_POSITION);

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
                                    if (Constant.banner_options) {
                                        if (i == (Constant.CATEGORY_LOADING_POSITION / 2)) {
                                            arrayList.add(new ModelCategories(AdapterCategories.VIEW_BANNER));
                                        }
                                    }
                                    totalPost = objJson.get("post_total").getAsInt();
                                    int id = objJson.get("id").getAsInt();
                                    String category_name = objJson.get("category_name").getAsString();
                                    String category_image_thumb = objJson.get("category_image_thumb").getAsString();
                                    String category_image_original = objJson.get("category_image_original").getAsString();
                                    String total_wallpaper = objJson.get("total_wallpaper").getAsString();
                                    arrayList.add(new ModelCategories(id, category_name, category_image_thumb, category_image_original, total_wallpaper, AdapterCategories.VIEW_ITEM));
                                }
                                if (adapterCategories != null) {
                                    adapterCategories.insertData(arrayList);
                                }
                                pageCount++;
                            } catch (JsonSyntaxException e) {
                                e.printStackTrace();
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
