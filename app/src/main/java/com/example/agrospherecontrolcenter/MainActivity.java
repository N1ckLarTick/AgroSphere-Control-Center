package com.example.agrospherecontrolcenter;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.agrospherecontrolcenter.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Set;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    FirebaseUser user;
    private static final String TAG = "FrugalLogs";
    private static final int REQUEST_ENABLE_BT = 1;
    public static Handler handler;
    private final static int ERROR_READ = 0;
    BluetoothDevice arduinoBTModule = null;
    UUID arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };
    private static String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };
    private void checkPermissions(){
        int permission1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    1
            );
        } else if (permission2 != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_LOCATION,
                    1
            );
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        TextView btReadings = findViewById(R.id.btReadings);
        TextView btDevices = findViewById(R.id.btDevices);
        Button connectToDevice = (Button) findViewById(R.id.connectToDevice);
        Button seachDevices = (Button) findViewById(R.id.seachDevices);
        Button clearValues = (Button) findViewById(R.id.refresh);
        Log.d(TAG, "Begin Execution");
        checkPermissions();

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {

                    case ERROR_READ:
                        String arduinoMsg = msg.obj.toString();
                        btReadings.setText(arduinoMsg);
                        break;
                }
            }
        };

        clearValues.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btDevices.setText("");
                btReadings.setText("");
            }
        });

        final Observable<String> connectToBTObservable = Observable.create(emitter -> {
            Log.d(TAG, "Calling connectThread class");
            ConnectThread connectThread = new ConnectThread(arduinoBTModule, arduinoUUID, handler);
            connectThread.run();

            if (connectThread.getMmSocket().isConnected()) {
                Log.d(TAG, "Calling ConnectedThread class");

                ConnectedThread connectedThread = new ConnectedThread(connectThread.getMmSocket());
                connectedThread.run();
                if(connectedThread.getValueRead()!=null)
                {

                    emitter.onNext(connectedThread.getValueRead());
                }

                connectedThread.cancel();
            }

            connectThread.cancel();

            emitter.onComplete();

        });

        connectToDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (arduinoBTModule != null) {

                    connectToBTObservable.
                            observeOn(AndroidSchedulers.mainThread()).
                            subscribeOn(Schedulers.io()).
                            subscribe(valueRead -> {
                                btReadings.setText(valueRead);

                            });

                }
            }
        });

        seachDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (bluetoothAdapter == null) {
                    Log.d(TAG, "Device doesn't support Bluetooth");
                } else {
                    Log.d(TAG, "Device support Bluetooth");

                    if (!bluetoothAdapter.isEnabled()) {
                        Log.d(TAG, "Bluetooth is disabled");
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, "We don't BT Permissions");
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                            Log.d(TAG, "Bluetooth is enabled now");
                        } else {
                            Log.d(TAG, "We have BT Permissions");
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                            Log.d(TAG, "Bluetooth is enabled now");
                        }

                    } else {
                        Log.d(TAG, "Bluetooth is enabled");
                    }
                    String btDevicesString="";
                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                    if (pairedDevices.size() > 0) {

                        for (BluetoothDevice device: pairedDevices) {
                            String deviceName = device.getName();
                            String deviceHardwareAddress = device.getAddress();
                            Log.d(TAG, "deviceName:" + deviceName);
                            Log.d(TAG, "deviceHardwareAddress:" + deviceHardwareAddress);
                            btDevicesString=btDevicesString+deviceName+" || "+deviceHardwareAddress+"\n";

                            if (deviceName.equals("HC-05 ")) {
                                Log.d(TAG, "HC-05 found");
                                btDevices.setText("Датчик успешно найден");
                                arduinoUUID = device.getUuids()[0].getUuid();
                                arduinoBTModule = device;
                                connectToDevice.setEnabled(true);
                            }
                        }
                    }
                }
                Log.d(TAG, "Button Pressed");
            }
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user==null){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }
        else{
            binding.emailText.setText(user.getEmail());
        }
        binding.logoutbutton.setOnClickListener(view->{
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

}