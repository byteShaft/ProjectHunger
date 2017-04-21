package com.byteshaft.projecthunger.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by fi8er1 on 23/11/2016.
 */

public class DatabaseHelpers extends SQLiteOpenHelper {

    private ArrayList<OnDatabaseChangedListener> mListeners = new ArrayList<>();

    public DatabaseHelpers(Context context) {
        super(context, DatabaseConstants.DATABASE_NAME, null, DatabaseConstants.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseConstants.TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + DatabaseConstants.TABLE_NAME);
        onCreate(db);
    }

    public void createNewEntry(String id, String type, String name, String location, String formattedAddress, String timings
            , String rating, String numberOfRatings, String contactNumber, String foodMenu) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseConstants.ID, id);
        values.put(DatabaseConstants.TYPE, type);
        values.put(DatabaseConstants.NAME, name);
        values.put(DatabaseConstants.LOCATION, location);
        values.put(DatabaseConstants.FORMATTED_ADDRESS, formattedAddress);
        values.put(DatabaseConstants.TIMINGS, timings);
        values.put(DatabaseConstants.RATING, rating);
        values.put(DatabaseConstants.NUMBER_OF_RATINGS, numberOfRatings);
        values.put(DatabaseConstants.CONTACT_NUMBER, contactNumber);
        values.put(DatabaseConstants.FOOD_MENU, foodMenu);
        db.insert(DatabaseConstants.TABLE_NAME, null, values);
        db.close();
    }

    public ArrayList<HashMap> getAllRecords() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseConstants.TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        ArrayList<HashMap> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("id", cursor.getString(
                    cursor.getColumnIndex(DatabaseConstants.ID)));
            hashMap.put("type", cursor.getString(
                    cursor.getColumnIndex(DatabaseConstants.TYPE)));
            hashMap.put("name", cursor.getString(
                    cursor.getColumnIndex(DatabaseConstants.NAME)));
            hashMap.put("location", cursor.getString(
                    cursor.getColumnIndex(DatabaseConstants.LOCATION)));
            hashMap.put("formatted_address", cursor.getString(
                    cursor.getColumnIndex(DatabaseConstants.FORMATTED_ADDRESS)));
            hashMap.put("timings", cursor.getString(
                    cursor.getColumnIndex(DatabaseConstants.TIMINGS)));
            hashMap.put("rating", cursor.getString(
                    cursor.getColumnIndex(DatabaseConstants.RATING)));
            hashMap.put("number_of_ratings", cursor.getString(
                    cursor.getColumnIndex(DatabaseConstants.NUMBER_OF_RATINGS)));
            hashMap.put("contact", cursor.getString(
                    cursor.getColumnIndex(DatabaseConstants.CONTACT_NUMBER)));
            hashMap.put("food_menu", cursor.getString(
                    cursor.getColumnIndex(DatabaseConstants.FOOD_MENU)));
            list.add(hashMap);
        }
        db.close();
        cursor.close();
        return list;
    }

    public void deleteEntry(String id) {
        SQLiteDatabase db = getWritableDatabase();
        String query = "DELETE FROM "
                + DatabaseConstants.TABLE_NAME
                + " WHERE "
                + DatabaseConstants.ID
                + "="
                + "'" + id + "'";
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();
        db.close();
        cursor.close();
    }

    public void clearTable() {
        SQLiteDatabase db = getWritableDatabase();
        String query = "DELETE FROM " + DatabaseConstants.TABLE_NAME;
        db.execSQL(query);
        db.close();
    }

    public boolean isEmpty() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseConstants.TABLE_NAME, null);
        boolean isEmpty;
        isEmpty = !cursor.moveToNext();
        cursor.close();
        return isEmpty;
    }

    public boolean entryExists(String id) {
        SQLiteDatabase db = getWritableDatabase();
        String selectString = "SELECT * FROM " + DatabaseConstants.TABLE_NAME + " WHERE " + DatabaseConstants.ID + " =?";
        Cursor cursor = db.rawQuery(selectString, new String[] {id});
        boolean entryExists = false;
        if(cursor.moveToFirst()){
            entryExists = true;
            while(cursor.moveToNext()){
                entryExists = true;
            }
        }
        cursor.close();
        db.close();
        return entryExists;
    }

    public void setOnDatabaseChangedListener(OnDatabaseChangedListener listener) {
        mListeners.add(listener);
    }

    private void dispatchEventOnNewEntryCreated() {
        for (OnDatabaseChangedListener listener : mListeners) {
            listener.onNewEntryCreated();
        }
    }

    public interface OnDatabaseChangedListener {
        void onNewEntryCreated();
    }
}
