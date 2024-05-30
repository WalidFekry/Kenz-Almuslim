package com.kenz.almuslim.data.local.helper;

import com.kenz.almuslim.data.local.model.LocalWallpaper;

import java.util.List;

import io.reactivex.Observable;

public interface IDbHelper {

    Observable<Boolean> storeLocalWallpaper(LocalWallpaper localWallpaper);

    Observable<List<LocalWallpaper>> getListOfLocalWallpaper();


    Observable<Boolean> resetDatabase();

}
