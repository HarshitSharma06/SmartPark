package com.smartpark.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.smartpark.app.model.ParkingLot;
import com.smartpark.app.util.DateTimeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SmartParkDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smartpark.db";
    private static final int DATABASE_VERSION = 1;

    // Favourites Table
    public static final String TABLE_FAVOURITES = "favourites";
    public static final String COL_FAV_ID = "id";
    public static final String COL_FAV_LOT_ID = "lot_id";
    public static final String COL_FAV_NAME = "name";
    public static final String COL_FAV_ADDRESS = "address";
    public static final String COL_FAV_PRICE = "price";
    public static final String COL_FAV_LAT = "lat";
    public static final String COL_FAV_LNG = "lng";
    public static final String COL_FAV_NOTE = "note";
    public static final String COL_FAV_SAVED_AT = "saved_at";

    // History Table
    public static final String TABLE_HISTORY = "history";
    public static final String COL_HIST_ID = "id";
    public static final String COL_HIST_LOT_ID = "lot_id";
    public static final String COL_HIST_NAME = "name";
    public static final String COL_HIST_PRICE = "price";
    public static final String COL_HIST_VIEWED_AT = "viewed_at";

    // Cached Lots Table (for offline cache)
    public static final String TABLE_CACHED_LOTS = "cached_lots";
    public static final String COL_CACHE_LOT_ID = "lot_id";
    public static final String COL_CACHE_NAME = "name";
    public static final String COL_CACHE_ADDRESS = "address";
    public static final String COL_CACHE_PRICE = "price";
    public static final String COL_CACHE_LAT = "lat";
    public static final String COL_CACHE_LNG = "lng";

    public SmartParkDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createFav = "CREATE TABLE " + TABLE_FAVOURITES + " (" +
                COL_FAV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_FAV_LOT_ID + " TEXT UNIQUE, " +
                COL_FAV_NAME + " TEXT, " +
                COL_FAV_ADDRESS + " TEXT, " +
                COL_FAV_PRICE + " INTEGER, " +
                COL_FAV_LAT + " REAL, " +
                COL_FAV_LNG + " REAL, " +
                COL_FAV_NOTE + " TEXT, " +
                COL_FAV_SAVED_AT + " TEXT);";

        String createHist = "CREATE TABLE " + TABLE_HISTORY + " (" +
                COL_HIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_HIST_LOT_ID + " TEXT, " +
                COL_HIST_NAME + " TEXT, " +
                COL_HIST_PRICE + " INTEGER, " +
                COL_HIST_VIEWED_AT + " TEXT);";

        String createCache = "CREATE TABLE " + TABLE_CACHED_LOTS + " (" +
                COL_CACHE_LOT_ID + " TEXT PRIMARY KEY, " +
                COL_CACHE_NAME + " TEXT, " +
                COL_CACHE_ADDRESS + " TEXT, " +
                COL_CACHE_PRICE + " INTEGER, " +
                COL_CACHE_LAT + " REAL, " +
                COL_CACHE_LNG + " REAL);";

        db.execSQL(createFav);
        db.execSQL(createHist);
        db.execSQL(createCache);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVOURITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CACHED_LOTS);
        onCreate(db);
    }

    // --- FAVOURITES DAO ---
    public boolean insertFavourite(ParkingLot lot, String note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_FAV_LOT_ID, lot.getLotId());
        cv.put(COL_FAV_NAME, lot.getName());
        cv.put(COL_FAV_ADDRESS, lot.getAddress());
        cv.put(COL_FAV_PRICE, lot.getPricePerHour());
        cv.put(COL_FAV_LAT, lot.getLat());
        cv.put(COL_FAV_LNG, lot.getLng());
        cv.put(COL_FAV_NOTE, note != null ? note : "");
        cv.put(COL_FAV_SAVED_AT, DateTimeUtil.getCurrentDateTimeString());

        long result = db.insertWithOnConflict(TABLE_FAVOURITES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public boolean isFavourite(String lotId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVOURITES, new String[]{COL_FAV_ID},
                COL_FAV_LOT_ID + "=?", new String[]{lotId}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean deleteFavourite(String lotId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_FAVOURITES, COL_FAV_LOT_ID + "=?", new String[]{lotId});
        return rows > 0;
    }

    public boolean updateFavouriteNote(String lotId, String newNote) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_FAV_NOTE, newNote);
        int rows = db.update(TABLE_FAVOURITES, cv, COL_FAV_LOT_ID + "=?", new String[]{lotId});
        return rows > 0;
    }

    public List<HashMap<String, String>> getFavourites() {
        List<HashMap<String, String>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FAVOURITES + " ORDER BY id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<>();
                map.put("lot_id", cursor.getString(cursor.getColumnIndexOrThrow(COL_FAV_LOT_ID)));
                map.put("name", cursor.getString(cursor.getColumnIndexOrThrow(COL_FAV_NAME)));
                map.put("address", cursor.getString(cursor.getColumnIndexOrThrow(COL_FAV_ADDRESS)));
                map.put("price", "₹" + cursor.getInt(cursor.getColumnIndexOrThrow(COL_FAV_PRICE)) + "/hr");
                map.put("note", cursor.getString(cursor.getColumnIndexOrThrow(COL_FAV_NOTE)));
                map.put("saved_at", cursor.getString(cursor.getColumnIndexOrThrow(COL_FAV_SAVED_AT)));
                list.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // --- HISTORY DAO ---
    public void addHistory(ParkingLot lot) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_HIST_LOT_ID, lot.getLotId());
        cv.put(COL_HIST_NAME, lot.getName());
        cv.put(COL_HIST_PRICE, lot.getPricePerHour());
        cv.put(COL_HIST_VIEWED_AT, DateTimeUtil.getCurrentDateTimeString());
        db.insert(TABLE_HISTORY, null, cv);
    }

    public List<HashMap<String, String>> getHistory() {
        List<HashMap<String, String>> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_HISTORY + " ORDER BY id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<>();
                map.put("id", String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COL_HIST_ID))));
                map.put("lot_id", cursor.getString(cursor.getColumnIndexOrThrow(COL_HIST_LOT_ID)));
                map.put("name", cursor.getString(cursor.getColumnIndexOrThrow(COL_HIST_NAME)));
                map.put("price", "₹" + cursor.getInt(cursor.getColumnIndexOrThrow(COL_HIST_PRICE)) + "/hr");
                map.put("viewed_at", cursor.getString(cursor.getColumnIndexOrThrow(COL_HIST_VIEWED_AT)));
                list.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public void clearHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORY, null, null);
    }

    public void deleteHistoryEntry(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_HISTORY, COL_HIST_ID + "=?", new String[]{id});
    }

    // --- CACHED LOTS DAO ---
    public void cacheLots(List<ParkingLot> lots) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_CACHED_LOTS, null, null);
            for (ParkingLot lot : lots) {
                ContentValues cv = new ContentValues();
                cv.put(COL_CACHE_LOT_ID, lot.getLotId());
                cv.put(COL_CACHE_NAME, lot.getName());
                cv.put(COL_CACHE_ADDRESS, lot.getAddress());
                cv.put(COL_CACHE_PRICE, lot.getPricePerHour());
                cv.put(COL_CACHE_LAT, lot.getLat());
                cv.put(COL_CACHE_LNG, lot.getLng());
                db.insert(TABLE_CACHED_LOTS, null, cv);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<ParkingLot> getCachedLots() {
        List<ParkingLot> lots = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CACHED_LOTS, null);
        if (cursor.moveToFirst()) {
            do {
                ParkingLot lot = new ParkingLot();
                lot.setLotId(cursor.getString(cursor.getColumnIndexOrThrow(COL_CACHE_LOT_ID)));
                lot.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_CACHE_NAME)));
                lot.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(COL_CACHE_ADDRESS)));
                lot.setPricePerHour(cursor.getInt(cursor.getColumnIndexOrThrow(COL_CACHE_PRICE)));
                lot.setLat(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_CACHE_LAT)));
                lot.setLng(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_CACHE_LNG)));
                lot.setStatus("AVAILABLE");
                lots.add(lot);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lots;
    }
}
