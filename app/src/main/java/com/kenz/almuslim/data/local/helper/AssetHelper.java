package com.kenz.almuslim.data.local.helper;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class AssetHelper extends SQLiteAssetHelper {


    public AssetHelper(Context context) {
        super(context, "local_wallpaper.db", null, 1);
    }
}
