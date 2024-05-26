package com.example.agrospherecontrolcenter;

import android.app.Application;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
@Database(entities = {Device.class},version = 1)
public abstract class DeviceDatabase extends RoomDatabase {
    private static final String DB_NAME = "database_room.db";
    private static DeviceDatabase instance = null;

    public static DeviceDatabase newInstance(Application application) {
        if (instance == null) {
            instance = Room.databaseBuilder(application, DeviceDatabase.class, DB_NAME)
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    public abstract DeviceDao deviceDao();
}
