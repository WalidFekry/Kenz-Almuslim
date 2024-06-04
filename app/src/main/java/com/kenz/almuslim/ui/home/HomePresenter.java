package com.kenz.almuslim.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import com.benkkstudio.bsjson.BSJson;
import com.benkkstudio.bsjson.BSObject;
import com.benkkstudio.bsjson.Interface.BSJsonV2Listener;
import com.kenz.almuslim.App;
import com.kenz.almuslim.R;
import com.kenz.almuslim.data.adapter.AdapterWallpaper;
import com.kenz.almuslim.data.local.model.LocalWallpaper;
import com.kenz.almuslim.ui.detail.vertical.VerticalAdapter;
import com.kenz.almuslim.ui.main.MainActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import io.reactivex.disposables.CompositeDisposable;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;


import com.kenz.almuslim.data.base.BasePresenter;
import com.kenz.almuslim.data.base.EndlessRecyclerViewScrollListener;
import com.kenz.almuslim.data.model.ModelWallpaper;
import com.kenz.almuslim.data.utils.Constant;
import com.kenz.almuslim.data.widgets.ProgressLoader;
import com.kenz.almuslim.utils.CommonUtils;
import com.kenz.almuslim.utils.rx.SchedulerProvider;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class HomePresenter extends BasePresenter<HomeView> {
    public static final String FILTER_POPULAR = "FILTER_POPULAR";
    public static final String FILTER_LATEST = "FILTER_LATEST";
    public static final String FILTER_PREMIUM = "FILTER_PREMIUM";
    public static final String FILTER_FREE = "FILTER_FREE";
    public static final String FILTER_GIF = "FILTER_GIF";
    public static final String FILTER_IMAGE = "FILTER_IMAGE";
    private AppCompatActivity activity;
    private View rootView;
    private int pageCount = 1, totalPost;
    private ArrayList<ModelWallpaper> arrayList;
    private AdapterWallpaper adapterWallpaper;
    public ProgressLoader progressLoader;
    private SwipeRefreshLayout swipeRefreshLayout;


    private RecyclerView recyclerView;

    private RecyclerView verticalRecycler;
    private VerticalAdapter verticalAdapter;

    private final GridLayoutManager grid = new GridLayoutManager(activity, 3);
    private final LinearLayoutManager verticalManager = new LinearLayoutManager(activity);

    private final EndlessRecyclerViewScrollListener gridListener = new EndlessRecyclerViewScrollListener(grid) {
        @Override
        public void onLoadMore(int page, int totalItemsCount) {
            if (adapterWallpaper.arrayList.size() <= totalPost) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapterWallpaper.removeLoading();
                        loadNow();
                    }
                }, 1000);
            } else {
                adapterWallpaper.removeLoading();
            }
        }

        @Override
        public void onScrolled(int dy) {
        }
    };

    private final EndlessRecyclerViewScrollListener linearListener = new EndlessRecyclerViewScrollListener(verticalManager) {
        @Override
        public void onLoadMore(int page, int totalItemsCount) {
            if (verticalAdapter.arrayList.size() <= totalPost) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        verticalAdapter.removeLoading();
                        loadNow();
                    }
                }, 1000);
            } else {
                verticalAdapter.removeLoading();
            }
        }

        @Override
        public void onScrolled(int dy) {
        }
    };

    protected void initView(AppCompatActivity activity, View rootView) {
        this.activity = activity;
        this.rootView = rootView;
        progressLoader = new ProgressLoader(activity);
        if (CommonUtils.isNetworkConnected(activity))
        initRecyclerViewHome();
        initSwipeRefresh();
    }

    private void initSwipeRefresh() {
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pageCount = 1;
                initRecyclerViewHome();
            }
        });
        rootView.findViewById(R.id.button_filter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });
    }


    private boolean isVertical = false;

    private void initRecyclerViewHome() {
        recyclerView = rootView.findViewById(R.id.recycler_view);
        verticalRecycler = rootView.findViewById(R.id.vertical_recycler_view);

        SharedPreferences preferences = activity.getSharedPreferences("DEFAULT", Context.MODE_PRIVATE);

        if (preferences.getString("scroll_type", "H").equals("H")) {
            isVertical = false;
            ((MainActivity) activity).onChangeCheckBox(true);
        } else if (preferences.getString("scroll_type", "H").equals("V")) {
            isVertical = true;
            ((MainActivity) activity).onChangeCheckBox(false);
        }

        adapterWallpaper = new AdapterWallpaper(activity, new ArrayList<ModelWallpaper>());
        verticalAdapter = new VerticalAdapter(activity);
        if (!isVertical) {
            try {
                recyclerView.setLayoutManager(grid);
            } catch (Exception e) {
                e.printStackTrace();
            }

            ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(adapterWallpaper);
            scaleInAnimationAdapter.setFirstOnly(false);
            scaleInAnimationAdapter.setDuration(1000);
            scaleInAnimationAdapter.setInterpolator(new OvershootInterpolator(.5f));
            recyclerView.setAdapter(scaleInAnimationAdapter);

            grid.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
//                    switch (adapterWallpaper.getItemViewType(position)) {
//                        case AdapterWallpaper.VIEW_ITEM:
                    return 1;
//                        case AdapterWallpaper.VIEW_PROGRESS:
//                        case AdapterWallpaper.VIEW_NATIVE:
//                            return 3;
//                        default:
//                            return -1;
//                    }
                }
            });
            if (CommonUtils.isNetworkConnected(activity)) {
                recyclerView.addOnScrollListener(gridListener);
            }
        } else {
            verticalRecycler.setLayoutManager(verticalManager);
            verticalRecycler.setAdapter(verticalAdapter);
            ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(verticalAdapter);
            scaleInAnimationAdapter.setFirstOnly(false);
            scaleInAnimationAdapter.setDuration(1000);
            scaleInAnimationAdapter.setInterpolator(new OvershootInterpolator(.5f));
            if (CommonUtils.isNetworkConnected(activity)) {
                verticalRecycler.addOnScrollListener(linearListener);
            }
        }

        arrayList = new ArrayList<>();
        progressLoader.show();
        if (isVertical) {
            verticalRecycler.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            verticalRecycler.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        if (CommonUtils.isNetworkConnected(activity)) {
            rootView.findViewById(R.id.layout_no_connection).setVisibility(View.GONE);
            loadNow();
        } else {
            getListOfLocalImages();
//            recyclerView.setVisibility(View.GONE);
//            rootView.findViewById(R.id.layout_no_connection).setVisibility(View.VISIBLE);
//            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
//                swipeRefreshLayout.setRefreshing(false);
//            }
        }

    }





    public void loadNow() {
        if (!CommonUtils.isNetworkConnected(activity))
            return;
        BSObject bsObject = new BSObject();
        bsObject.addProperty("method_name", "wallpaper_all");
        bsObject.addProperty("page", pageCount);
        bsObject.addProperty("limit", Constant.WALLPAPER_LOADING_POSITION);
        bsObject.addProperty("filter", Constant.filter_value);
        new BSJson.Builder(activity)
                .setServer(Constant.SERVER_URL)
                .setObject(bsObject.getProperty())
                .setMethod(BSJson.METHOD_POST)
                .setListener(new BSJsonV2Listener() {
                    @Override
                    public void onLoaded(String response) {
                        handler.post(() -> {
                            arrayList.clear();
                            try {
                                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                                JsonArray jsonArray = jsonObject.getAsJsonArray(Constant.TAG_ROOT);
                                for (int i = 0; i < jsonArray.size(); i++) {
                                    JsonObject objJson = jsonArray.get(i).getAsJsonObject();
//                                    if (Constant.native_options) {
//                                        if (i == (Constant.WALLPAPER_LOADING_POSITION / 2)) {
//                                            if (Constant.AdsOptions.IDENTIFIER.equals(Constant.AdsOptions.FACEBOOK)) {
//                                                arrayList.add(new ModelWallpaper(AdapterWallpaper.VIEW_NATIVE, FacebookNativeLoader.nativeAd));
//                                                FacebookNativeLoader.loadNext(activity);
//                                            } else {
//                                                arrayList.add(new ModelWallpaper(AdapterWallpaper.VIEW_NATIVE));
//                                            }
//                                        }
//                                    }
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
                                    Long num_likes = objJson.get("num_likes").getAsLong();
                                    Long num_views = objJson.get("num_views").getAsLong();
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
                                            num_likes,
                                            num_views));
                                }
                                if (isVertical) {
                                    verticalAdapter.insertData(arrayList);
                                } else {
                                    adapterWallpaper.insertData(arrayList);
                                }
                                Constant.arrayListDetail = arrayList;
                                pageCount++;
                                if (!isLocalSet) {
                                    storeFirstRange(arrayList);
                                }
                                if (arrayList.isEmpty()){
                                    recyclerView.setVisibility(View.GONE);
                                    rootView.findViewById(R.id.layout_no_favourite).setVisibility(View.VISIBLE);
                                }else {
                                    recyclerView.setVisibility(View.VISIBLE);
                                    rootView.findViewById(R.id.layout_no_favourite).setVisibility(View.GONE);
                                }
                            } catch (JsonSyntaxException e) {
                                e.printStackTrace();
                            }
                            swipeRefreshLayout.setRefreshing(false);
                            progressLoader.stopLoader();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        swipeRefreshLayout.setRefreshing(false);
                        progressLoader.stopLoader();
                    }
                })
                .load();
    }

    public void clearAll() {
        arrayList.clear();
        pageCount = 1;
        if (adapterWallpaper != null) {
            adapterWallpaper.arrayList.clear();
            adapterWallpaper.notifyDataSetChanged();
        }
        if (adapterWallpaper != null) {
            adapterWallpaper.arrayList.clear();
            adapterWallpaper.notifyDataSetChanged();
        }
        initRecyclerViewHome();
    }


    private void showFilterDialog() {
        @SuppressLint("InflateParams")
        View view = activity.getLayoutInflater().inflate(R.layout.dialog_filter, null);
        final BottomSheetDialog dialog_desc = new BottomSheetDialog(activity, R.style.SheetDialog);
        dialog_desc.setContentView(view);
        dialog_desc.show();
        final String[] filter_id = {FILTER_POPULAR, FILTER_LATEST, FILTER_PREMIUM, FILTER_FREE, FILTER_GIF, FILTER_IMAGE};
        final int[] card_view_id = {R.id.popular_button, R.id.latest_button, R.id.premium_button, R.id.free_button, R.id.gif_button, R.id.image_button};
        final int[] text_view_id = {R.id.text_popular, R.id.text_latest, R.id.text_premium, R.id.text_free, R.id.text_gif, R.id.text_image};
        for (int i = 0; i < card_view_id.length; i++) {
            CardView cardView = dialog_desc.findViewById(card_view_id[i]);
            final TextView textView = dialog_desc.findViewById(text_view_id[i]);
            assert cardView != null;
            assert textView != null;
            if (card_view_id[i] == Constant.selected_filter) {
                cardView.setCardBackgroundColor(activity.getResources().getColor(R.color.colorAccent));
                textView.setTextColor(activity.getResources().getColor(android.R.color.white));
            }
            final int finalI = i;
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog_desc.dismiss();
                    Constant.selected_filter = card_view_id[finalI];
                    Constant.filter_value = filter_id[finalI];
                    ((MainActivity) activity).binding.toolbarTitle.setText(textView.getText().toString());
                    pageCount = 1;
                    initRecyclerViewHome();
                }
            });
        }
    }


    private boolean isLocalSet = false;

    public CompositeDisposable compositeDisposable = new CompositeDisposable();
    SchedulerProvider schedulerProvider = new SchedulerProvider();

    Handler handler = new Handler(Looper.getMainLooper());

    public static final String TAG = "HomePresenter";

    private void storeFirstRange(List<ModelWallpaper> items) {
        handler.post(() -> {
            isLocalSet = true;
            compositeDisposable.add(
                    App.localDataManager.resetDatabase()
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .subscribe(aBoolean -> {
                                        if (aBoolean)
                                            startStoring(items);
                                    }, throwable -> Log.d(TAG, "storeFirstRange: => " + throwable.getMessage())
                            )
            );
        });
    }

    private void startStoring(List<ModelWallpaper> items) {
        handler.post(() -> {
            for (ModelWallpaper item : items) {
                Picasso.get().load(item.wallpaper_image_thumb).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        LocalWallpaper localWallpaper = new LocalWallpaper();
                        localWallpaper.setWallpaper_image_thumb(bitmap);
                        localWallpaper.setWallpaper_image_original(bitmap);
                        localWallpaper.setWall_id(item.wall_id);
                        localWallpaper.setCat_id(item.cat_id);
                        localWallpaper.setWallpaper_title(item.wallpaper_title);
                        localWallpaper.setView_type(item.view_type);
                        localWallpaper.setWallpaper_type(item.wallpaper_type);
                        localWallpaper.setWallpaper_image_detail(item.wallpaper_image_detail);
                        localWallpaper.setWallpaper_premium(item.wallpaper_premium);
                        localWallpaper.setWallpaper_tags(item.wallpaper_tags);
                        storeImageLocally(localWallpaper);
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        Log.d(TAG, "onBitmapFailed: First => " + e.getMessage());
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
            }
        });
    }


    private void storeImageLocally(LocalWallpaper localWallpaper) {
        handler.post(() -> compositeDisposable.add(
                App.localDataManager.storeLocalWallpaper(localWallpaper)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(aBoolean -> Log.d(TAG, "storeImageLocally: => image stored >><<"),
                                throwable -> Log.d("storeImageLocally", "storeImageLocally: " + throwable.getMessage()))
        ));
    }


    private void getListOfLocalImages() {
        compositeDisposable.add(
                App.localDataManager.getListOfLocalWallpaper()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(aList -> handler.post(() -> {
                            for (LocalWallpaper wallpaper : aList) {
                                arrayList.add(new ModelWallpaper(wallpaper.wall_id,
                                        wallpaper.cat_id,
                                        wallpaper.wallpaper_title,
                                        "",
                                        "",
                                        "",
                                        wallpaper.wallpaper_tags,
                                        wallpaper.wallpaper_tags,
                                        "NO",
                                        AdapterWallpaper.VIEW_ITEM,
                                        wallpaper.wallpaper_image_thumb,
                                        wallpaper.wallpaper_image_original
                                ));
                            }
                            if (isVertical) {
                                verticalAdapter.insertData(arrayList);
                            } else {
                                adapterWallpaper.insertData(arrayList);
                            }
                            Constant.arrayListDetail = arrayList;
                            swipeRefreshLayout.setRefreshing(false);
                            progressLoader.stopLoader();
                        }), throwable -> Log.d(TAG, "getListOfLocalImages: " + throwable.getMessage()))
        );
    }


    @Override
    public void onPresenterDestroy() {
        super.onPresenterDestroy();
        compositeDisposable.dispose();
    }
}