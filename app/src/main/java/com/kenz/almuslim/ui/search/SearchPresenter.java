package com.kenz.almuslim.ui.search;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.benkkstudio.bsjson.BSJson;
import com.benkkstudio.bsjson.BSObject;
import com.benkkstudio.bsjson.Interface.BSJsonV2Listener;
import com.kenz.almuslim.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.Locale;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

import com.kenz.almuslim.data.adapter.AdapterSearch;
import com.kenz.almuslim.data.adapter.AdapterWallpaper;
import com.kenz.almuslim.data.base.BasePresenter;
import com.kenz.almuslim.data.model.ModelWallpaper;
import com.kenz.almuslim.data.utils.Constant;
import com.kenz.almuslim.data.utils.Helpers;
import com.kenz.almuslim.data.widgets.ProgressLoader;

public class SearchPresenter extends BasePresenter<SearchView> {
    private Activity activity;
    private View rootView;
    private AdapterSearch adapterSearch;
    private ArrayList<ModelWallpaper> arrayList;
    private ArrayList<String> arrayListSuggestion;
    public ProgressLoader progressLoader;
    private boolean isFullyLoaded = false;
    private RecyclerView recyclerView;

    protected void initView(Activity activity, View rootView) {
        this.activity = activity;
        this.rootView = rootView;
    }

    protected void onResume() {
        if (!isFullyLoaded) {
            arrayList = new ArrayList<>();
            arrayListSuggestion = new ArrayList<>();
            progressLoader = new ProgressLoader(activity);
            if (Helpers.isConnected(activity)) {
                rootView.findViewById(R.id.recycler_view).setVisibility(View.VISIBLE);
                rootView.findViewById(R.id.layout_no_connection).setVisibility(View.GONE);
                loadSuggestion();
            } else {
                rootView.findViewById(R.id.recycler_view).setVisibility(View.GONE);
                rootView.findViewById(R.id.layout_no_connection).setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadSuggestion() {
        progressLoader.show();
        arrayList = new ArrayList<>();
        BSObject bsObject = new BSObject();
        bsObject.addProperty("method_name", "search_suggestion");
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
                                String suggestion = objJson.get("suggestion").getAsString();
                                arrayListSuggestion.add(suggestion);
                            }
                        } catch (JsonSyntaxException e) {
                            e.printStackTrace();
                        }
                        initSuggestion();
                        loadWallpaper();
                    }

                    @Override
                    public void onError(String error) {
                        progressLoader.stopLoader();
                    }
                })
                .load();
    }

    private void initSuggestion() {
        AutoCompleteTextView autoCompleteTextView = rootView.findViewById(R.id.search_bar);
        String[] suggestions = new String[arrayListSuggestion.size()];
        suggestions = arrayListSuggestion.toArray(suggestions);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, R.layout.suggestion_list, suggestions);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setThreshold(1);
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapterSearch.search(s.toString().toLowerCase(Locale.getDefault()));
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void loadWallpaper() {
        arrayList = new ArrayList<>();
        BSObject bsObject = new BSObject();
        bsObject.addProperty("method_name", "wallpaper_search");
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
                        } catch (JsonSyntaxException e) {
                            e.printStackTrace();
                        }
                        initRecycler();
                        progressLoader.stopLoader();
                    }

                    @Override
                    public void onError(String error) {
                        progressLoader.stopLoader();
                    }
                })
                .load();
    }

    private void initRecycler() {
        recyclerView = rootView.findViewById(R.id.recycler_view);
        final GridLayoutManager grid = new GridLayoutManager(activity, 3);
        recyclerView.setLayoutManager(grid);
        adapterSearch = new AdapterSearch(activity, arrayList);
        ScaleInAnimationAdapter scaleInAnimationAdapter = new ScaleInAnimationAdapter(adapterSearch);
        scaleInAnimationAdapter.setFirstOnly(false);
        scaleInAnimationAdapter.setDuration(2000);
        scaleInAnimationAdapter.setInterpolator(new OvershootInterpolator(.5f));
        recyclerView.setAdapter(scaleInAnimationAdapter);
        isFullyLoaded = true;
    }

}
