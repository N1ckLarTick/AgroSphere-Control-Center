package com.example.agrospherecontrolcenter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.agrospherecontrolcenter.databinding.ActivityDeviceManagementBinding;
import com.example.agrospherecontrolcenter.databinding.ActivityMainBinding;

import java.util.Set;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DeviceManagement extends AppCompatActivity {
    private static final String TAG = "FrugalLogs";
    private static final int REQUEST_ENABLE_BT = 1;
    public static Handler handler;
    private final static int ERROR_READ = 0;
    BluetoothDevice arduinoBTModule = null;
    private BluetoothSocket mBTSocket;
    UUID arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private ActivityDeviceManagementBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeviceManagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        Intent intent = getIntent();
        Device device = (Device) intent.getSerializableExtra("device");
        binding.deviceName.setText("Имя: " + device.getName());
        binding.deviceType.setText("Тип: " + device.getType());
        binding.devicePin.setText("Пин: " + device.getPin());


        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {

                    case ERROR_READ:
                        String arduinoMsg = msg.obj.toString();
                        binding.btReadings.setText(arduinoMsg);
                        break;
                }
            }
        };
        final Observable<String> connectToBTObservable = Observable.create(emitter -> {
            Log.d(TAG, "Calling connectThread class");
            ConnectThread connectThread = new ConnectThread(arduinoBTModule, arduinoUUID, handler);
            connectThread.run(arduinoBTModule);

            if (connectThread.getMmSocket().isConnected()) {
                Log.d(TAG, "Calling ConnectedThread class");

                ConnectedThread connectedThread = new ConnectedThread(connectThread.getMmSocket());
                connectedThread.run();
                if (connectedThread.getValueRead() != null) {

                    emitter.onNext(connectedThread.getValueRead());
                }

                connectedThread.cancel();
            }

            connectThread.cancel();

            emitter.onComplete();

        });
        final Observable<String> sendDataToBTObservableOn = Observable.create(emitter -> {
            Log.d(TAG, "Calling connectThread class");
            ConnectThread connectThread = new ConnectThread(arduinoBTModule, arduinoUUID, handler);
            connectThread.run(arduinoBTModule);

            if (connectThread.getMmSocket().isConnected()) {
                Log.d(TAG, "Calling ConnectedThread class");

                ConnectedThread connectedThread = new ConnectedThread(connectThread.getMmSocket());
                connectedThread.run();

                String dataToSend = "SET_PIN_MODE:" + device.getPin() + ":INPUT";
                connectedThread.write(dataToSend);

                connectedThread.cancel();
            }

            connectThread.cancel();

            emitter.onComplete();

        });
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                    if (pairedDevices.size() > 0) {

                        for (BluetoothDevice dev : pairedDevices) {
                            String deviceName = dev.getName();
                            String deviceHardwareAddress = dev.getAddress();
                            Log.d(TAG, "deviceName:" + deviceName);
                            Log.d(TAG, "deviceHardwareAddress:" + deviceHardwareAddress);

                            if (deviceName.equals("HC-05") || deviceName.equals("HC-05 ")) {
                                Log.d(TAG, "HC-05 found");
                                arduinoUUID = dev.getUuids()[0].getUuid();
                                arduinoBTModule = dev;
                            }
                        }
                    }
        binding.btnConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (arduinoBTModule != null) {
                    Toast.makeText(DeviceManagement.this, "Ошибка входа", Toast.LENGTH_SHORT).show();
                    sendDataToBTObservableOn.
                            observeOn(AndroidSchedulers.mainThread()).
                            subscribeOn(Schedulers.io()).
                            subscribe();
                }
            }
        });

        binding.btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (arduinoBTModule != null) {
                    Log.d(TAG, "Button Pressed");
                    connectToBTObservable.
                            observeOn(AndroidSchedulers.mainThread()).
                            subscribeOn(Schedulers.io()).
                            subscribe(valueRead -> {
                                binding.btReadings.setText(valueRead);

                            });

                }
            }
        });
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}