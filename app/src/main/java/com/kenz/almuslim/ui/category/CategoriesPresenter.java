package com.kenz.almuslim.ui.category;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.benkkstudio.bsjson.BSJson;
import com.benkkstudio.bsjson.BSObject;
import com.benkkstudio.bsjson.Interface.BSJsonV2Listener;
import com.kenz.almuslim.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

import com.kenz.almuslim.data.adapter.AdapterCategories;
import com.kenz.almuslim.data.adapter.AdapterWallpaper;
import com.kenz.almuslim.data.base.BasePresenter;
import com.kenz.almuslim.data.base.EndlessRecyclerViewScrollListener;
import com.kenz.almuslim.data.model.ModelCategories;
import com.kenz.almuslim.data.utils.Constant;
import com.kenz.almuslim.data.utils.Helpers;
import com.kenz.almuslim.data.widgets.ProgressLoader;

public class CategoriesPresenter extends BasePresenter<CategoriesView> {
    private Activity activity;
    private View rootView;
    private int pageCount = 1, totalPost;
    private ArrayList<ModelCategories> arrayList;
    private AdapterWallpaper adapterWallpaper;
    public ProgressLoader progressLoader;
    private AdapterCategories adapterCategories;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    protected void initView(Activity activity, View rootView){
        this.activity = activity;
        this.rootView = rootView;
        progressLoader = new ProgressLoader(activity);
        initRecyclerView();
        initSwipeRefresh();
    }

    private void initSwipeRefresh(){
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageCount = 1;
                initRecyclerView();
            }
        });
    }

    private void initRecyclerView() {
        recyclerView = rootView.findViewById(R.id.recycler_view);
        final GridLayoutManager grid = new GridLayoutManager(activity, 1);
        recyclerView.setLayoutManager(grid);
        adapterCategories = new AdapterCategories(activity, new ArrayList<ModelCategories>());
        ScaleInAnimationAdapter alphaAdapter = new ScaleInAnimationAdapter(adapterCategories);
        alphaAdapter.setFirstOnly(false);
        alphaAdapter.setDuration(2000);
        alphaAdapter.setInterpolator(new OvershootInterpolator(.5f));
        recyclerView.setAdapter(alphaAdapter);

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(grid) {
            @Override
            public void onScrolled(int dy) {

            }

            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (adapterCategories.arrayList.size() <= totalPost) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            adapterCategories.removeLoading();
                            loadNow();
                        }
                    }, 1000);
                } else {
                    adapterCategories.removeLoading();
                }
            }
        });

        if(Helpers.isConnected(activity)){
            recyclerView.setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.layout_no_connection).setVisibility(View.GONE);
            loadNow();
        } else {
            recyclerView.setVisibility(View.GONE);
            rootView.findViewById(R.id.layout_no_connection).setVisibility(View.VISIBLE);
            if(swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()){
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private void loadNow() {
        progressLoader.show();
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
                        try {
                            JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                            JsonArray jsonArray = jsonObject.getAsJsonArray(Constant.TAG_ROOT);
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JsonObject objJson = jsonArray.get(i).getAsJsonObject();
                                if(Constant.banner_options){
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
                            adapterCategories.insertData(arrayList);
                            pageCount++;
                        } catch (JsonSyntaxException e) {
                            e.printStackTrace();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                        progressLoader.stopLoader();
                    }

                    @Override
                    public void onError(String error) {
                        swipeRefreshLayout.setRefreshing(false);
                        progressLoader.stopLoader();
                    }
                })
                .load();
    }
}
