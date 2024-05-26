package com.example.agrospherecontrolcenter;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY name")
    LiveData<List<Device>> getDevices();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void add(Device product);

    @Query("DELETE FROM devices WHERE id =:id")
    void remove(int id);

    @Query("UPDATE devices SET type=:type, name =:name, pin =:pin WHERE id =:id")
    void update( String name,String type,String pin, int id);
}
