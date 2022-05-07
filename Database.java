package com.csci3397.linhmatt.routefacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
    public Database(Context context) {
        super(context, "history.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create Table history(city Text, state Text, date Integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop Table if exists history");
    }

    public void updateDB(String city, String state, Integer date) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = getDB();
        cursor.moveToFirst();
        boolean unique = true;
        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.getString(0).equals(city) && cursor.getString(1).equals(state)) {
                unique = false;
            }
            else {
                cursor.moveToNext();
            }
        }
        if (unique) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("city", city);
            contentValues.put("state", state);
            contentValues.put("date", date);
            db.insert("history", null, contentValues);
        }
    }

    public Cursor getDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("history", null, null, null, null, null, "date", null);
        return cursor;
    }
}
