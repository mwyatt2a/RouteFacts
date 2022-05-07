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
        db.execSQL("create Table settings(settingOn Text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop Table if exists history");
        db.execSQL("drop Table if exists settings");
    }

    public void updateHistory(String city, String state, Integer date) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = getHistory();
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

    public void updateSettings(String setting) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = getSettings();
        cursor.moveToFirst();
        boolean notExists = true;
        for (int i = 0; i < cursor.getCount(); i++) {
            if (cursor.getString(0).equals(setting)) {
                notExists = false;
            }
            else {
                cursor.moveToNext();
            }
        }
        if (notExists) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("settingOn", setting);
            db.insert("settings", null, contentValues);
        }
        else {
            db.delete("settings","settingOn = ?", new String[]{setting});
        }
    }

    public Cursor getSettings() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("settings", null, null, null, null, null, null, null);
        return cursor;
    }

    public Cursor getHistory() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("history", null, null, null, null, null, "date", null);
        return cursor;
    }
}
