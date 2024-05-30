package com.kenz.almuslim.data.utils;

import com.kenz.almuslim.R;
import com.kenz.almuslim.ui.home.HomePresenter;

import java.io.Serializable;
import java.util.ArrayList;


import com.kenz.almuslim.data.model.ModelWallpaper;

public class Constant implements Serializable {
    public static final String SERVER_URL = "https://post.walid-fekry.com/bs_cp/api";
    public static final String TAG_ROOT = "BENKKSTUDIO";
    public static final int WALLPAPER_LOADING_POSITION = 30;
    public static final int CATEGORY_LOADING_POSITION = 10;
    public static final int VIEW_PAGER_LIMIT = 20;
    public static int cat_id;
    public static String cat_name;
    public static ArrayList<ModelWallpaper> arrayListDetail;
    public static String app_name;
    public static String app_email;
    public static boolean banner_options  = true;
    public static int interstitial_click;
    public static boolean native_options = true;
    public static String privacy_police;
    public static boolean native_loaded;
    public static int ads_count = 0;
    public static int selected_filter = R.id.latest_button;
    public static String filter_value = HomePresenter.FILTER_LATEST;

}
