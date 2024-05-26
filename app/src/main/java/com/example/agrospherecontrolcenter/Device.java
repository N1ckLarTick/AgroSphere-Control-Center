package com.example.agrospherecontrolcenter;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Objects;

@Entity(tableName = "devices")
public class Device implements Serializable {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    @ColumnInfo(name = "name")
    public final String name;
    @ColumnInfo(name = "type")
    public final String type;
    @ColumnInfo(name = "pin")
    public final String pin;

    public Device(String name, String type, String pin) {
        this(0,name,type,pin);
    }
    @Ignore
    public Device(int id, String name, String type, String pin) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.pin = pin;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getPin() {
        return pin;
    }

    @Override
    public String toString() {
        return "Device{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", pin=" + pin +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return id == device.id && Objects.equals(name, device.name) && Objects.equals(type, device.type) && Objects.equals(pin, device.pin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, pin);
    }
}