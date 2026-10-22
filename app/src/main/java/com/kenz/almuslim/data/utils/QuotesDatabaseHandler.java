package com.kenz.almuslim.data.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kenz.almuslim.data.model.ModelQuote;

import java.util.ArrayList;
import java.util.List;

public class QuotesDatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "KenzQuotesDB";

    // Saved Quotes Table
    private static final String TABLE_SAVED = "saved_quotes";
    private static final String KEY_ID = "id";
    private static final String KEY_QUOTE_ID = "quote_id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_IMAGE_URL = "image_url";
    private static final String KEY_SAVED_AT = "saved_at";

    // Cached Quotes Table for Offline Mode
    private static final String TABLE_CACHED = "cached_quotes";

    public QuotesDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SAVED_TABLE = "CREATE TABLE " + TABLE_SAVED + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_QUOTE_ID + " INTEGER UNIQUE,"
                + KEY_TITLE + " TEXT,"
                + KEY_CREATED_AT + " TEXT,"
                + KEY_IMAGE_URL + " TEXT,"
                + KEY_SAVED_AT + " INTEGER"
                + ")";

        String CREATE_CACHED_TABLE = "CREATE TABLE " + TABLE_CACHED + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_QUOTE_ID + " INTEGER UNIQUE,"
                + KEY_TITLE + " TEXT,"
                + KEY_CREATED_AT + " TEXT,"
                + KEY_IMAGE_URL + " TEXT"
                + ")";

        db.execSQL(CREATE_SAVED_TABLE);
        db.execSQL(CREATE_CACHED_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVED);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CACHED);
        onCreate(db);
    }

    // --- SAVED QUOTES METHODS ---

    public boolean saveQuote(ModelQuote quote) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_QUOTE_ID, quote.id);
        values.put(KEY_TITLE, quote.title);
        values.put(KEY_CREATED_AT, quote.createdAt);
        values.put(KEY_IMAGE_URL, quote.imageUrl);
        values.put(KEY_SAVED_AT, System.currentTimeMillis());

        long result = db.insertWithOnConflict(TABLE_SAVED, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
        return result != -1;
    }

    public void removeSavedQuote(int quoteId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SAVED, KEY_QUOTE_ID + " = ?", new String[]{String.valueOf(quoteId)});
        db.close();
    }

    public boolean isQuoteSaved(int quoteId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SAVED, new String[]{KEY_QUOTE_ID},
                KEY_QUOTE_ID + " = ?", new String[]{String.valueOf(quoteId)},
                null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return exists;
    }

    public ArrayList<ModelQuote> getAllSavedQuotes() {
        ArrayList<ModelQuote> list = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_SAVED + " ORDER BY " + KEY_SAVED_AT + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int quoteId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_QUOTE_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE));
                String createdAt = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREATED_AT));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE_URL));
                list.add(new ModelQuote(quoteId, title, createdAt, imageUrl, true));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return list;
    }

    // --- CACHED QUOTES METHODS (FOR OFFLINE MODE) ---

    public void cacheQuotes(List<ModelQuote> quotes) {
        if (quotes == null || quotes.isEmpty()) return;
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_CACHED, null, null);
            for (ModelQuote quote : quotes) {
                ContentValues values = new ContentValues();
                values.put(KEY_QUOTE_ID, quote.id);
                values.put(KEY_TITLE, quote.title);
                values.put(KEY_CREATED_AT, quote.createdAt);
                values.put(KEY_IMAGE_URL, quote.imageUrl);
                db.insertWithOnConflict(TABLE_CACHED, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public ArrayList<ModelQuote> getCachedQuotes() {
        ArrayList<ModelQuote> list = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CACHED + " ORDER BY " + KEY_ID + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int quoteId = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_QUOTE_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE));
                String createdAt = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREATED_AT));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(KEY_IMAGE_URL));
                boolean isSaved = isQuoteSaved(quoteId);
                list.add(new ModelQuote(quoteId, title, createdAt, imageUrl, isSaved));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return list;
    }
}
