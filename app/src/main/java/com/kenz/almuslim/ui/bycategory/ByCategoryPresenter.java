package com.kenz.almuslim.ui.bycategory;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.benkkstudio.bsjson.BSJson;
import com.benkkstudio.bsjson.BSObject;
import com.benkkstudio.bsjson.Interface.BSJsonV2Listener;
import com.kenz.almuslim.R;
import com.kenz.almuslim.data.adapter.AdapterWallpaper;
import com.kenz.almuslim.data.base.BasePresenter;
import com.kenz.almuslim.data.base.EndlessRecyclerViewScrollListener;
import com.kenz.almuslim.data.model.ModelWallpaper;
import com.kenz.almuslim.data.utils.Constant;
import com.kenz.almuslim.data.utils.Helpers;
import com.kenz.almuslim.data.widgets.ProgressLoader;
import com.kenz.almuslim.databinding.ActivityByCategoryBinding;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;

import androidx.recyclerview.widget.GridLayoutManager;

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;


public class ByCategoryPresenter extends BasePresenter<ByCategoryView> {
    private Activity activity;
    private ActivityByCategoryBinding binding;
    private AdapterWallpaper adapterWallpaper;
    private int pageCount = 1, totalPost;
    private ArrayList<ModelWallpaper> arrayList;
    private ProgressLoader progressLoader;

    protected void initView(final Activity activity, ActivityByCategoryBinding binding) {
        this.activity = activity;
        this.binding = binding;
        progressLoader = new ProgressLoader(activity);
        binding.toolbarTitle.setText(Constant.cat_name);
        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
                activity.overridePendingTransition(R.anim.no_change, R.anim.slide_down);
            }
        });
        initRecyclerViewCategory();
    }

    private void initRecyclerViewCategory() {
        final GridLayoutManager grid = new GridLayoutManager(activity, 3);
        binding.recyclerView.setLayoutManager(grid);
        adapterWallpaper = new AdapterWallpaper(activity, new ArrayList<ModelWallpaper>());

        ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(adapterWallpaper);
        scaleInAnimationAdapter.setFirstOnly(false);
        scaleInAnimationAdapter.setDuration(2000);
        scaleInAnimationAdapter.setInterpolator(new OvershootInterpolator(.5f));
        binding.recyclerView.setAdapter(scaleInAnimationAdapter);

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
        binding.recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(grid) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (adapterWallpaper.arrayList.size() <= totalPost) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            adapterWallpaper.removeLoading();
                            loadByCategory();
                        }
                    }, 1000);
                } else {
                    adapterWallpaper.removeLoading();
                }
            }

            @Override
            public void onScrolled(int dy) {
            }
        });
        if (Helpers.isConnected(activity)) {
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.layoutNoConnection.setVisibility(View.GONE);
            loadByCategory();
        } else {
            binding.recyclerView.setVisibility(View.GONE);
            binding.layoutNoConnection.setVisibility(View.VISIBLE);
            if (binding.swipeRefresh != null && binding.swipeRefresh.isRefreshing()) {
                binding.swipeRefresh.setRefreshing(false);
            }
        }
    }

    private void loadByCategory() {
        progressLoader.show();
        arrayList = new ArrayList<>();
        BSObject bsObject = new BSObject();
        bsObject.addProperty("method_name", "wallpaper_by_cat");
        bsObject.addProperty("cat_id", Constant.cat_id);
        bsObject.addProperty("page", pageCount);
        bsObject.addProperty("limit", Constant.WALLPAPER_LOADING_POSITION);
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
                                        AdapterWallpaper.VIEW_ITEM, numLikes, numViews));
                            }
                            adapterWallpaper.insertData(arrayList);
                            pageCount++;
                        } catch (JsonSyntaxException e) {
                            e.printStackTrace();
                        }
                        progressLoader.stopLoader();
                    }

                    @Override
                    public void onError(String error) {
                        progressLoader.stopLoader();
                    }
                })
                .load();
    }
}
