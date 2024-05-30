package com.kenz.almuslim.data.local;

import android.content.Context;

import com.kenz.almuslim.data.local.helper.DbHelper;
import com.kenz.almuslim.data.local.model.LocalWallpaper;

import java.util.List;

import io.reactivex.Observable;

public class LocalDataManager implements ILocalDataManager {

    private final DbHelper dbHelper;

    public LocalDataManager(Context context) {
        dbHelper = DbHelper.getInstance(context);
    }

    @Override
    public Observable<Boolean> storeLocalWallpaper(LocalWallpaper localWallpaper) {
        return dbHelper.storeLocalWallpaper(localWallpaper);
    }

    @Override
    public Observable<List<LocalWallpaper>> getListOfLocalWallpaper() {
        return dbHelper.getListOfLocalWallpaper();
    }

    @Override
    public Observable<Boolean> resetDatabase() {
        return dbHelper.resetDatabase();
    }
}
