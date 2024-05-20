package com.example.agrospherecontrolcenter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;


public class ConnectThread extends Thread {
    private BluetoothSocket mmSocket;
    private static final String TAG = "FrugalLogs";
    public static Handler handler;
    private final static int ERROR_READ = 0;

    @SuppressLint("MissingPermission")
    public ConnectThread(BluetoothDevice device, UUID MY_UUID, Handler handler) {
        BluetoothSocket tmp = null;
        this.handler=handler;

        try {
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
    }

    @SuppressLint("MissingPermission")
    public void run(BluetoothDevice device) {

        try {
            mmSocket.connect();
        } catch (IOException connectException) {
            //handler.obtainMessage(ERROR_READ, "Ошибка подключения").sendToTarget();
            Log.e(TAG, "connectException: " + connectException);
            try {
                Log.e(TAG,"trying fallback...");

                mmSocket =(BluetoothSocket) device.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(device,1);
                mmSocket.connect();

                Log.e(TAG,"Connected");
            }
            catch (Exception e2) {
                Log.e(TAG, "Couldn't establish Bluetooth connection!");
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
            }
            return;
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }

    public BluetoothSocket getMmSocket(){
        return mmSocket;
    }
}
