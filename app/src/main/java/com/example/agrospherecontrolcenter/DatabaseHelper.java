package com.example.agrospherecontrolcenter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;
import androidx.room.util.DBUtil;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME="device.db";
    private static final String TABLE_NAME="devices";
    private static final String COLUMN_ID="id";
    private static final int COLUMN_ID_NUM =0;
    private static final String COLUMN_NAME="name";
    private static final int COLUMN_NAME_NUM=1;
    private static final String COLUMN_TYPE="type";
    private static final int COLUMN_TYPE_NUM=2;
    private static final String COLUMN_PIN="pin";
    private static final int COLUMN_PIN_NUM=3;
    private static final int VERSION=1;
    private static DatabaseHelper instance = null;
    public static DatabaseHelper newInstance(Context context){
        if(instance==null){
            instance = new DatabaseHelper(context);
        }
        return instance;
    }
    private SQLiteDatabase sqldataHelper;
    private DatabaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, VERSION);
        sqldataHelper = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_TYPE + " TEXT, " +
                COLUMN_PIN + " TEXT);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void add(Device device){
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME,device.getName());
        contentValues.put(COLUMN_TYPE,device.getType());
        contentValues.put(COLUMN_PIN,device.getPin());
        sqldataHelper.insert(TABLE_NAME,null,contentValues);
    }

    public List<Device> getAllDevices(){
        List<Device> result = new ArrayList<>();
        Cursor cursor = sqldataHelper.query(TABLE_NAME,null,null,
                null,null,null,null);
        if(cursor.isAfterLast()) return new ArrayList<>();
        cursor.moveToFirst();
        do{
            result.add(new Device(
                    cursor.getString(COLUMN_NAME_NUM),
                    cursor.getString(COLUMN_TYPE_NUM),
                    cursor.getString(COLUMN_PIN_NUM)));
        } while(cursor.moveToNext());
        cursor.close();
        return result;
    }
    public void remove(Device device){
        sqldataHelper.delete(TABLE_NAME,COLUMN_ID + " =? ", new String[]{String.valueOf(device.getId())});
    }
    @Override
    public synchronized void close() {
        super.close();
        sqldataHelper.close();
    }
}
