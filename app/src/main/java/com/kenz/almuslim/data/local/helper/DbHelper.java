package com.kenz.almuslim.data.local.helper;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.kenz.almuslim.data.local.model.LocalWallpaper;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;

public class DbHelper implements IDbHelper {
    private final SQLiteOpenHelper sqLiteOpenHelper;
    private SQLiteDatabase database;


    private static DbHelper dbHelper;


    public static synchronized DbHelper getInstance(Context context) {
        if (dbHelper == null) {
            dbHelper = new DbHelper(context);
        }
        return dbHelper;
    }

    private DbHelper(Context context) {
        sqLiteOpenHelper = new AssetHelper(context);
    }


    @Override
    public Observable<Boolean> storeLocalWallpaper(LocalWallpaper localWallpaper) {
        return Observable.fromCallable(() -> {
            database = sqLiteOpenHelper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("id", localWallpaper.wall_id);
            contentValues.put("cat_id", localWallpaper.cat_id);
            contentValues.put("wallpaper_title", localWallpaper.wallpaper_title);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            localWallpaper.wallpaper_image_thumb.compress(Bitmap.CompressFormat.PNG, 90, stream);
            byte[] bytes = stream.toByteArray();
            contentValues.put("wallpaper_image_thumb", bytes);
            stream.flush();

            ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
            localWallpaper.wallpaper_image_original.compress(Bitmap.CompressFormat.PNG, 90, imageStream);
            byte[] imageBytes = imageStream.toByteArray();
            contentValues.put("wallpaper_image_original", imageBytes);
            contentValues.put("wallpaper_image_detail", localWallpaper.wallpaper_image_detail);
            contentValues.put("wallpaper_tags", localWallpaper.wallpaper_tags);
            contentValues.put("wallpaper_type", localWallpaper.wallpaper_type);
            contentValues.put("wallpaper_premium", "no");
            contentValues.put("view_type", localWallpaper.view_type);
            database.insert("wallpapers_table", null, contentValues);
            return true;
        });
    }

    @Override
    public Observable<List<LocalWallpaper>> getListOfLocalWallpaper() {
        return Observable.fromCallable(() -> {
            database = sqLiteOpenHelper.getWritableDatabase();
            Cursor cursor = database.rawQuery("SELECT * FROM wallpapers_table", null);
            List<LocalWallpaper> localWallpapers = new ArrayList<>();
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    LocalWallpaper localWallpaper = new LocalWallpaper(cursor.getInt(0),
                            cursor.getInt(1),
                            cursor.getString(2),
                            BitmapFactory.decodeByteArray(cursor.getBlob(3), 0, cursor.getBlob(3).length),
                            BitmapFactory.decodeByteArray(cursor.getBlob(4), 0, cursor.getBlob(4).length),
                            cursor.getString(5),
                            cursor.getString(6),
                            cursor.getString(7),
                            cursor.getString(8),
                            cursor.getInt(9));
                    localWallpapers.add(localWallpaper);
                    cursor.moveToNext();
                }
            }
            cursor.close();
            return localWallpapers;
        });
    }


    @Override
    public Observable<Boolean> resetDatabase() {
        return Observable.fromCallable(() -> {
            database = sqLiteOpenHelper.getWritableDatabase();
            database.execSQL("DELETE FROM wallpapers_table");
            return true;
        });
    }


    @NotNull
    private static String getDbPath(@NotNull Context context) {
        return "data/data/" + context.getPackageName() + "/databases/";
    }

}
