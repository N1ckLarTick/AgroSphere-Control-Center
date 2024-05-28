package com.example.agrospherecontrolcenter;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agrospherecontrolcenter.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
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
    private BluetoothSocket mBTSocket;
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

    private void checkPermissions() {
        int permission1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN);
        if (permission1 != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    1
            );
        } else if (permission2 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_LOCATION,
                    1
            );
        }
    }
    //List<Device> devices = new ArrayList<>();
    private DeviceAdapter arrayAdapter;
    private DeviceDatabase repository;
    private final static String EXTRA_NAME="name";
    private final static String EXTRA_TYPE="type";
    private final static String EXTRA_PIN="pin";
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        repository = DeviceDatabase.newInstance(getApplication());
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        arrayAdapter = new DeviceAdapter();
        repository.deviceDao().getDevices().observe(this, new Observer<List<Device>>() {
            @Override
            public void onChanged(List<Device> devices) {
                arrayAdapter.setDevices(devices);
            }
        });
        binding.recyclerView.setAdapter(arrayAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT|ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Device device = arrayAdapter.getDevices().get(position);
                showAlert(device);
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        repository.deviceDao().remove(device.getId());
//                    }
//                }).start();
            }
        });
        itemTouchHelper.attachToRecyclerView(binding.recyclerView);
        arrayAdapter.setOnItemDeviceClickListener(new DeviceAdapter.OnItemDeviceClickListener() {
            @Override
            public void onDeviceClick(int position) {
                Device device = arrayAdapter.getDevices().get(position);
                Log.d(TAG, device.getType());
                if (device.getType().equals("Датчик")) {
                    Intent intent = new Intent(getApplicationContext(), DeviceManagement.class);
                    intent.putExtra("device", device);
                    startActivity(intent);
                    finish();
                }
                if (device.getType().equals("Светодиод")) {
                    Intent intent = new Intent(getApplicationContext(), Device2Management.class);
                    intent.putExtra("device", device);
                    startActivity(intent);
                    finish();
                }
            }
        });

        Log.d(TAG, "Begin Execution");
        checkPermissions();
        addListData();
    }
    private void addListData(){
        binding.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = AddNewDeviceActivity.newIntent(MainActivity.this,String.valueOf(0),"","","");

                startActivity(intent);
            }
        });
    }
    public void showAlert(Device device){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Удаление элемента")
                .setMessage("Удалить элемент " + device.getName() + "?")
                .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                repository.deviceDao().remove(device.getId());
                            }
                        }).start();
                    }
                })
                .setNeutralButton("Изменить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = AddNewDeviceActivity.newIntent(MainActivity.this,
                                String.valueOf(device.getId()),
                                device.getName(),
                                device.getType(),
                                String.valueOf(device.getPin()));
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Отмена", null)
                .setIcon(android.R.drawable.ic_delete)
                .show();
        getOnBackPressedDispatcher(new V)
        repository.deviceDao().getDevices().observe(MainActivity.this, new Observer<List<Device>>() {
            @Override
            public void onChanged(List<Device> devices) {
                arrayAdapter.setDevices(devices);
            }
        });
    }
}
