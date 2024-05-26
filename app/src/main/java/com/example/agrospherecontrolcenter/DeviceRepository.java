//package com.example.agrospherecontrolcenter;
//
//import android.app.Application;
//import android.content.Context;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class DeviceRepository {
//    private static DeviceRepository instance = null;
//    private DeviceDatabase deviceDatabase;
//    public static DeviceRepository newInstance(Application application){
//        if(instance == null){
//            instance = new DeviceRepository(application);
//        }
//        return instance;
//    }
//    private DeviceRepository(Application application) {
//        if(deviceDatabase == null){
//            deviceDatabase = deviceDatabase.newInstance(application);
//        }
//    }
//    public void add(Device device){
//        deviceDatabase.deviceDao().add(device);
//    }
//    public void remove(Device device){
//        deviceDatabase.deviceDao().remove(device.getId());
//        }
//
//
//    public void update(Device device,int position){
//        deviceDatabase.deviceDao().update(device.getName(),device.getType(), device.getPin(), device.getId());
//    }
//
//    public List<Device> getDevices() {
//        List<Device> devices = deviceDatabase.deviceDao().getDevices();
//        return new ArrayList<>(devices);
//    }
//}
